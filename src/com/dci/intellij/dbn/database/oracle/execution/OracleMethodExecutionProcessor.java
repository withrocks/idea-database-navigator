package com.dci.intellij.dbn.database.oracle.execution;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.data.type.DBNativeDataType;
import com.dci.intellij.dbn.data.type.GenericDataType;
import com.dci.intellij.dbn.database.common.execution.MethodExecutionProcessorImpl;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.execution.method.result.MethodExecutionResult;
import com.dci.intellij.dbn.object.DBArgument;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBType;
import com.dci.intellij.dbn.object.DBTypeAttribute;
import oracle.jdbc.OracleTypes;

public class OracleMethodExecutionProcessor extends MethodExecutionProcessorImpl<DBMethod> {
    public OracleMethodExecutionProcessor(DBMethod method) {
        super(method);
    }

    protected void preHookExecutionCommand(StringBuilder buffer) {}
    protected void postHookExecutionCommand(StringBuilder buffer) {}


    @Override
    public String buildExecutionCommand(MethodExecutionInput executionInput) throws SQLException {
        DBArgument returnArgument = getReturnArgument();

        StringBuilder buffer = new StringBuilder();
        buffer.append("declare\n");
        buffer.append("     v_timeout BINARY_INTEGER;\n");

        // variable declarations
        List<DBArgument> arguments = getArguments();
        for (DBArgument argument : arguments) {
            DBDataType dataType = argument.getDataType();
            if (dataType.isPurelyDeclared()) {
                buffer.append("    ");
                appendVariableName(buffer, argument);
                buffer.append(" ").append(dataType.getDeclaredType().getQualifiedName()).append(";\n");
            } else if (isBoolean(dataType)) {
                appendVariableName(buffer, argument);
                buffer.append(" boolean;\n");
            }
        }

        buffer.append("begin\n");

        preHookExecutionCommand(buffer);

        // input variable initialization
        for (DBArgument argument : arguments) {
            DBDataType dataType = argument.getDataType();

            if (argument.isInput()) {
                if (dataType.isPurelyDeclared()) {
                    List<DBTypeAttribute> attributes = dataType.getDeclaredType().getAttributes();
                    for (DBTypeAttribute attribute : attributes) {
                        buffer.append("    ");
                        appendVariableName(buffer, argument);
                        buffer.append(".").append(attribute.getName()).append(" := ?;\n");
                    }
                } else if(isBoolean(dataType)) {
                    String stringValue = parseBoolean(argument.getName(), executionInput.getInputValue(argument));

                    buffer.append("    ");
                    appendVariableName(buffer, argument);
                    buffer.append(" := ").append(stringValue).append(";\n");
                }
            }
        }

        // method call
        buffer.append("\n    ");
        if (returnArgument != null) {
            DBDataType dataType = returnArgument.getDataType();
            if (dataType.isPurelyDeclared() || isBoolean(dataType))
                appendVariableName(buffer, returnArgument); else
                buffer.append("?");

            buffer.append(" := ");
        }

        // method parameters
        buffer.append(getMethod().getQualifiedName()).append("(");
        for (DBArgument argument : arguments) {
            if (argument != returnArgument) {
                DBDataType dataType = argument.getDataType();
                if (dataType.isPurelyDeclared() || isBoolean(dataType))
                    appendVariableName(buffer, argument); else
                    buffer.append("?");
                boolean isLast = arguments.indexOf(argument) == arguments.size() - 1;
                if (!isLast) buffer.append(", ");
            }
        }
        buffer.append(");\n\n");


        // output variable initialization
        for (DBArgument argument : arguments) {
            if (argument.isOutput()) {
                DBDataType dataType = argument.getDataType();
                if (dataType.isPurelyDeclared()) {
                    if (dataType.getDeclaredType().isCollection()) {
                        buffer.append("    open ? for select * from table (");
                        appendVariableName(buffer, argument);
                        buffer.append(");");
                    } else {
                        List<DBTypeAttribute> attributes = dataType.getDeclaredType().getAttributes();
                        for (DBTypeAttribute attribute : attributes) {
                            buffer.append("    ? := ");
                            appendVariableName(buffer, argument);
                            buffer.append(".").append(attribute.getName()).append(";\n");
                        }
                    }
                } else if (isBoolean(dataType)) {
                    buffer.append("    ? := case when ");
                    appendVariableName(buffer, argument);
                    buffer.append(" then 'true' else 'false' end;\n");
                }
            }
        }


        postHookExecutionCommand(buffer);
        buffer.append("\nend;\n");
        return buffer.toString();
    }

    private static StringBuilder appendVariableName(StringBuilder buffer, DBArgument argument) {
        return buffer.append("var_").append(argument.getPosition());
    }


    @Override
    protected void bindParameters(MethodExecutionInput executionInput, PreparedStatement preparedStatement) throws SQLException {
        CallableStatement callableStatement = (CallableStatement) preparedStatement;
        DBArgument returnArgument = getReturnArgument();

        // bind input variables
        int parameterIndex = 1;
        for (DBArgument argument : getArguments()) {
            if (argument.isInput()) {
                DBDataType dataType = argument.getDataType();
                DBType type = dataType.getDeclaredType();
                if (dataType.isPurelyDeclared()) {
                    List<DBTypeAttribute> attributes = type.getAttributes();
                    for (DBTypeAttribute attribute : attributes) {
                        String stringValue = executionInput.getInputValue(argument, attribute);
                        setParameterValue(callableStatement, parameterIndex, attribute.getDataType(), stringValue);
                        parameterIndex++;
                    }
                }
            }
        }

        // bind return variable (functions only)
        if (returnArgument != null) {
            DBDataType dataType = returnArgument.getDataType();
            if(!dataType.isPurelyDeclared() && !isBoolean(dataType)) {
                callableStatement.registerOutParameter(parameterIndex, returnArgument.getDataType().getSqlType());
                parameterIndex++;
            }
        }

        // bind input/output parameters
        for (DBArgument argument : getArguments()) {
            DBDataType dataType = argument.getDataType();
            if (!argument.equals(returnArgument) && dataType.isNative() && !isBoolean(dataType)) {
                if (argument.isInput()) {
                    String stringValue = executionInput.getInputValue(argument);
                    setParameterValue(callableStatement, parameterIndex, dataType, stringValue);
                }
                if (argument.isOutput()){
                    callableStatement.registerOutParameter(parameterIndex, dataType.getSqlType());
                }
                parameterIndex++;
            }
        }

        // bind output variables
        for (DBArgument argument : getArguments()) {
            DBDataType dataType = argument.getDataType();
            if (argument.isOutput()) {
                if (dataType.isPurelyDeclared()) {
                    DBType declaredType = dataType.getDeclaredType();
                    if (declaredType.isCollection()) {
                        callableStatement.registerOutParameter(parameterIndex, OracleTypes.CURSOR);
                        parameterIndex++;
                    } else {
                        List<DBTypeAttribute> attributes = declaredType.getAttributes();
                        for (DBTypeAttribute attribute : attributes) {
                            callableStatement.registerOutParameter(parameterIndex, attribute.getDataType().getSqlType());
                            parameterIndex++;
                        }
                    }
                } else if (isBoolean(dataType)){
                    callableStatement.registerOutParameter(parameterIndex, dataType.getSqlType());
                    parameterIndex++;
                }
            }
        }
    }

    @Override
    public void loadValues(MethodExecutionResult executionResult, PreparedStatement preparedStatement) throws SQLException {
        CallableStatement callableStatement = (CallableStatement) preparedStatement;
        DBArgument returnArgument = getReturnArgument();

        // increment parameter index for input variables
        int parameterIndex = 1;
        for (DBArgument argument : getArguments()) {
            DBDataType dataType = argument.getDataType();
            if (dataType.isPurelyDeclared()) {
                if (argument.isInput()) {
                    parameterIndex = parameterIndex + dataType.getDeclaredType().getAttributes().size();
                }
            }
        }

        // get return value (functions only)
        if (returnArgument != null) {
            DBDataType dataType = returnArgument.getDataType();
            if (!dataType.isPurelyDeclared() && !isBoolean(dataType)) {
                DBNativeDataType nativeDataType = dataType.getNativeDataType();
                Object result = nativeDataType.getValueFromStatement(callableStatement, parameterIndex);
                executionResult.addArgumentValue(returnArgument, result);
                parameterIndex++;

            }
        }

        // get output parameter values
        for (DBArgument argument : getArguments()) {
            if (!argument.equals(returnArgument)) {
                DBDataType dataType = argument.getDataType();
                if (dataType.isNative() && !isBoolean(dataType)) {
                    if (argument.isOutput()){
                        DBNativeDataType nativeDataType = dataType.getNativeDataType();
                        Object result = nativeDataType.getValueFromStatement(callableStatement, parameterIndex);
                        executionResult.addArgumentValue(argument, result);
                    }
                    parameterIndex++;
                }
            }
        }

        // get output variable values
        for (DBArgument argument : getArguments()) {
            DBDataType dataType = argument.getDataType();
            if (argument.isOutput()) {
                if (dataType.isPurelyDeclared()) {
                    if (dataType.getDeclaredType().isCollection()) {
                        DBNativeDataType nativeDataType = dataType.getNativeDataType();
                        Object result = nativeDataType.getValueFromStatement(callableStatement, parameterIndex);
                        executionResult.addArgumentValue(argument, result);
                        parameterIndex++;
                    } else {
                        executionResult.addArgumentValue(argument, null);
                        List<DBTypeAttribute> attributes = dataType.getDeclaredType().getAttributes();
                        for (DBTypeAttribute attribute : attributes) {
                            // TODO assuming type attributes are all native
                            DBNativeDataType nativeDataType = attribute.getDataType().getNativeDataType();
                            Object result = nativeDataType.getValueFromStatement(callableStatement, parameterIndex);
                            executionResult.addArgumentValue(argument, attribute, result);
                            parameterIndex++;
                        }
                    }
                } else if (isBoolean(dataType)) {
                    Object result = dataType.getNativeDataType().getValueFromStatement(callableStatement, parameterIndex);
                    executionResult.addArgumentValue(argument, result);
                    parameterIndex++;

                }
            }
        }
    }

    private static boolean isBoolean(DBDataType dataType) {
        return dataType.getGenericDataType() == GenericDataType.BOOLEAN;
    }

    private static String parseBoolean(String argumentName, String booleanString) throws SQLException {
        if (booleanString != null && !booleanString.equalsIgnoreCase("true") && !booleanString.equalsIgnoreCase("false")) {
            throw new SQLException("Invalid boolean value for argument '" + argumentName + "'. true / false expected");
        }
        return Boolean.toString(Boolean.parseBoolean(booleanString));
    }

}