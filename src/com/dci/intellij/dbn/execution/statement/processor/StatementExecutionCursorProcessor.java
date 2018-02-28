package com.dci.intellij.dbn.execution.statement.processor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.execution.statement.StatementExecutionInput;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionCursorResult;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionResult;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionStatus;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.psi.ExecutablePsiElement;
import com.intellij.openapi.fileEditor.FileEditor;

public class StatementExecutionCursorProcessor extends StatementExecutionBasicProcessor {

    public StatementExecutionCursorProcessor(FileEditor fileEditor, ExecutablePsiElement psiElement, int index) {
        super(fileEditor, psiElement, index);
    }

    public StatementExecutionCursorProcessor(FileEditor fileEditor, DBLanguagePsiFile file, String sqlStatement, int index) {
        super(fileEditor, file, sqlStatement,  index);
    }

    protected StatementExecutionResult createExecutionResult(Statement statement, StatementExecutionInput executionInput) throws SQLException {
        ResultSet resultSet = statement.getResultSet();
        int updateCount = statement.getUpdateCount();
        String resultName = getResultName();
        if (resultSet == null) {
            statement.close();

            StatementExecutionResult executionResult = new StatementExecutionCursorResult(this, resultName, updateCount);
            executionResult.updateExecutionMessage(MessageType.INFO, getStatementName() + " executed successfully.");
            executionResult.setExecutionStatus(StatementExecutionStatus.SUCCESS);
            return executionResult;
        } else {
            StatementExecutionResult executionResult = getExecutionResult();
            if (executionResult == null) {
                executionResult = new StatementExecutionCursorResult(this, resultName, resultSet, updateCount);
                executionResult.setExecutionStatus(StatementExecutionStatus.SUCCESS);
                return executionResult;
            } else {
                // if executionResult exists, just update it with the new resultSet data
                if (executionResult instanceof StatementExecutionCursorResult){
                    StatementExecutionCursorResult executionCursorResult = (StatementExecutionCursorResult) executionResult;
                    executionCursorResult.loadResultSet(resultSet);
                    return executionResult;
                } else {
                    return new StatementExecutionCursorResult(this, resultName, resultSet, updateCount);
                }
            }
        }

    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean canExecute() {
        if (super.canExecute()) {
            StatementExecutionResult executionResult = getExecutionResult();
            return executionResult == null ||
                    executionResult.getExecutionStatus() == StatementExecutionStatus.ERROR ||
                    executionResult.getExecutionProcessor().isDirty();
        }
        return false;
    }

    public void navigateToResult() {
        StatementExecutionResult executionResult = getExecutionResult();
        if (executionResult instanceof StatementExecutionCursorResult) {
            StatementExecutionCursorResult executionCursorResult = (StatementExecutionCursorResult) executionResult;
            executionCursorResult.navigateToResult();
        }

    }

    @Override
    public boolean isQuery() {
         return true;
    }
}
