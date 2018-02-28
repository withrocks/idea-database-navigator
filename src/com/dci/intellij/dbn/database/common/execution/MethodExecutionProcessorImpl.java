package com.dci.intellij.dbn.database.common.execution;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.locale.Formatter;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.execution.logging.DatabaseLoggingManager;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.execution.method.options.MethodExecutionSettings;
import com.dci.intellij.dbn.execution.method.result.MethodExecutionResult;
import com.dci.intellij.dbn.object.DBArgument;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

public abstract class MethodExecutionProcessorImpl<T extends DBMethod> implements MethodExecutionProcessor<T> {
    private static final Logger LOGGER = LoggerFactory.createLogger();
    private DBObjectRef<T> method;

    protected MethodExecutionProcessorImpl(T method) {
        this.method = new DBObjectRef<T>(method);
    }

    @Nullable
    public T getMethod() {
        return method.get();
    }

    public List<DBArgument> getArguments() {
        T method = getMethod();
        return method == null ? new ArrayList<DBArgument>() : method.getArguments();
    }

    protected int getArgumentsCount() {
        return getArguments().size();
    }

    protected DBArgument getReturnArgument() {
        DBMethod method = getMethod();
        return method == null ? null : method.getReturnArgument();
    }


    public void execute(MethodExecutionInput executionInput, boolean debug) throws SQLException {
        T method = getMethod();
        if (method != null) {
            boolean usePoolConnection = executionInput.isUsePoolConnection();
            ConnectionHandler connectionHandler = method.getConnectionHandler();
            if (connectionHandler != null) {
                DBSchema executionSchema = executionInput.getExecutionSchema();
                Connection connection = usePoolConnection ?
                        connectionHandler.getPoolConnection(executionSchema) :
                        connectionHandler.getStandaloneConnection(executionSchema);
                if (usePoolConnection) {
                    connection.setAutoCommit(false);
                }

                execute(executionInput, connection, debug);
            }
        }
    }

    public void execute(MethodExecutionInput executionInput, Connection connection, boolean debug) throws SQLException {
        ConnectionHandler connectionHandler = null;
        boolean usePoolConnection = false;
        boolean loggingEnabled = !debug && executionInput.isEnableLogging();
        Project project = getProject();
        DatabaseLoggingManager loggingManager = DatabaseLoggingManager.getInstance(project);
        try {
            long startTime = System.currentTimeMillis();

            String command = buildExecutionCommand(executionInput);
            T method = getMethod();
            if (method != null) {
                connectionHandler = method.getConnectionHandler();
                if (connectionHandler != null) {
                    usePoolConnection = executionInput.isUsePoolConnection();
                    loggingEnabled = loggingEnabled && loggingManager.supportsLogging(connectionHandler);
                    if (loggingEnabled) {
                        loggingEnabled = loggingManager.enableLogger(connectionHandler, connection);
                    }

                    PreparedStatement preparedStatement = isQuery() ?
                            connection.prepareStatement(command) :
                            connection.prepareCall(command);

                    bindParameters(executionInput, preparedStatement);

                    MethodExecutionSettings methodExecutionSettings = ExecutionEngineSettings.getInstance(project).getMethodExecutionSettings();
                    int timeout = debug ?
                            methodExecutionSettings.getDebugExecutionTimeout() :
                            methodExecutionSettings.getExecutionTimeout();

                    preparedStatement.setQueryTimeout(timeout);
                    preparedStatement.execute();

                    MethodExecutionResult executionResult = executionInput.getExecutionResult();
                    if (executionResult != null) {
                        loadValues(executionResult, preparedStatement);
                        executionResult.setExecutionDuration((int) (System.currentTimeMillis() - startTime));

                        if (loggingEnabled) {
                            String logOutput = loggingManager.readLoggerOutput(connectionHandler, connection);
                            executionResult.setLogOutput(logOutput);
                        }
                    }

                    if (!usePoolConnection) connectionHandler.notifyChanges(method.getVirtualFile());
                }
            }

        } finally {
            if (loggingEnabled) {
                loggingManager.disableLogger(connectionHandler, connection);
            }

            if (executionInput.isCommitAfterExecution()) {
                if (usePoolConnection) {
                    connection.commit();
                } else {
                    if (connectionHandler != null) connectionHandler.commit();
                }
            }
            if (connectionHandler != null && usePoolConnection) connectionHandler.freePoolConnection(connection);
        }
    }

    protected boolean isQuery() {
        return false;
    }

    protected void bindParameters(MethodExecutionInput executionInput, PreparedStatement preparedStatement) throws SQLException {
        for (DBArgument argument : getArguments()) {
            DBDataType dataType = argument.getDataType();
            if (argument.isInput()) {
                String stringValue = executionInput.getInputValue(argument);
                setParameterValue(preparedStatement, argument.getPosition(), dataType, stringValue);
            }
            if (argument.isOutput() && preparedStatement instanceof CallableStatement) {
                CallableStatement callableStatement = (CallableStatement) preparedStatement;
                callableStatement.registerOutParameter(argument.getPosition(), dataType.getSqlType());
            }
        }
    }

    public void loadValues(MethodExecutionResult executionResult, PreparedStatement preparedStatement) throws SQLException {
        for (DBArgument argument : getArguments()) {
            if (argument.isOutput() && preparedStatement instanceof CallableStatement) {
                CallableStatement callableStatement = (CallableStatement) preparedStatement;
                Object result = callableStatement.getObject(argument.getPosition());
                executionResult.addArgumentValue(argument, result);
            }
        }
    }

    private Project getProject() {
        T method = getMethod();
        return method == null ? null : method.getProject();
    }

    protected void setParameterValue(PreparedStatement preparedStatement, int parameterIndex, DBDataType dataType, String stringValue) throws SQLException {
        try {
            Object value = null;
            if (StringUtil.isNotEmptyOrSpaces(stringValue))  {
                Formatter formatter = Formatter.getInstance(getProject());
                value = formatter.parseObject(dataType.getTypeClass(), stringValue);
                value = dataType.getNativeDataType().getDataTypeDefinition().convert(value);
            }
            dataType.setValueToPreparedStatement(preparedStatement, parameterIndex, value);

        } catch (SQLException e) {
            throw e;
        }  catch (Exception e) {
            throw new SQLException("Invalid value for data type " + dataType.getName() + " provided: \"" + stringValue + "\"");
        }
    }

    public abstract String buildExecutionCommand(MethodExecutionInput executionInput) throws SQLException;
}
