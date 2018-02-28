package com.dci.intellij.dbn.database.common.statement;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class StatementDefinition {
    public static final String DBN_PARAM_PLACEHOLDER = "DBN_PARAM_PLACEHOLDER";
    protected String statementText;
    protected Integer[] placeholderIndexes;

    private int connectionSignature;
    private int executionTrials;
    private long lastExecutionTimestamp;
    private boolean hasFallback;
    private boolean disabled;
    private boolean isPreparedStatement;

    public StatementDefinition(String statementText, String prefix, boolean isPreparedStatement, boolean hasFallback) {
        this.hasFallback = hasFallback;
        this.isPreparedStatement = isPreparedStatement;
        statementText = statementText.replaceAll("\\s+", " ").trim();
        if (prefix != null) {
            statementText = statementText.replaceAll("\\[PREFIX\\]", prefix);
        }

        StringBuilder buffer = new StringBuilder();
        List<Integer> placeholders = new ArrayList<Integer>();
        int startIndex = statementText.indexOf('{');
        if (startIndex == -1) {
            buffer.append(statementText);
        } else {
            int endIndex = 0;
            while (startIndex > -1) {
                String segment = statementText.substring(endIndex, startIndex);
                buffer.append(segment).append(isPreparedStatement ? "?" : DBN_PARAM_PLACEHOLDER);
                endIndex = statementText.indexOf('}', startIndex);
                String placeholder = statementText.substring(startIndex + 1, endIndex);

                placeholders.add(new Integer(placeholder));
                startIndex = statementText.indexOf('{', endIndex);
                endIndex = endIndex + 1;
            }
            if (endIndex < statementText.length()) {
                buffer.append(statementText.substring(endIndex));
            }
        }
        this.statementText = buffer.toString();
        this.placeholderIndexes = placeholders.toArray(new Integer[placeholders.size()]);
    }

    public PreparedStatement prepareStatement(Connection connection, Object[] arguments) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(statementText);
        for (int i = 0; i < placeholderIndexes.length; i++) {
            Integer argumentIndex = placeholderIndexes[i];
            Object argumentValue = arguments[argumentIndex];
            preparedStatement.setObject(i + 1, argumentValue);
        }
        return preparedStatement;
    }

    public String prepareStatementText(Object ... arguments) {
        String statementText = this.statementText;
        for (Integer argumentIndex : placeholderIndexes) {
            String argumentValue = Matcher.quoteReplacement(arguments[argumentIndex].toString());
            statementText = statementText.replaceFirst(isPreparedStatement ? "\\?" : DBN_PARAM_PLACEHOLDER, argumentValue);
        }
        return statementText;
    }

    public boolean canExecute(Connection connection) throws SQLException {
        // do not allow more then three calls
        int newConnectionSignature = createConnectionSignature(connection);
        long currentTimestamp = System.currentTimeMillis();
        boolean allowRetrial = !disabled && !hasFallback && currentTimestamp - lastExecutionTimestamp > 5000;
        if (executionTrials < 3 || newConnectionSignature != connectionSignature || allowRetrial) {
            if (connectionSignature != newConnectionSignature || allowRetrial) {
                connectionSignature = newConnectionSignature;
                lastExecutionTimestamp = currentTimestamp;
                executionTrials = 0;
            }
            return true;
        }
        return false;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public void updateExecutionStatus(boolean success) {
        if (success) {
            executionTrials = 0;
        } else {
            executionTrials++;
        }
    }

    private static int createConnectionSignature(Connection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        String driver = metaData.getDriverName();
        String url = metaData.getURL();
        String userName = metaData.getUserName();
        return (driver + url + userName).hashCode();
    }

    @Override
    public String toString() {
        return statementText;
    }
}
