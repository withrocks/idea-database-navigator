package com.dci.intellij.dbn.debugger.frame;

import java.sql.SQLException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.database.common.debug.BasicOperationInfo;
import com.dci.intellij.dbn.debugger.DBProgramDebugProcess;
import com.intellij.xdebugger.frame.XValueModifier;

public class DBProgramDebugValueModifier extends XValueModifier {
    private DBProgramDebugValue value;

    public DBProgramDebugValueModifier(DBProgramDebugValue value) {
        this.value = value;
    }

    @Override
    public void setValue(@NotNull String expression, @NotNull XModificationCallback callback) {
        DBProgramDebugProcess debugProcess = value.getDebugProcess();
        try {
            BasicOperationInfo operationInfo = debugProcess.getDebuggerInterface().setVariableValue(
                    value.getVariableName(),
                    0,
                    expression,
                    debugProcess.getDebugConnection());

            if (operationInfo.getError() != null) {
                callback.errorOccurred("Could not change value. " + operationInfo.getError());
            } else {
                callback.valueModified();
            }
        } catch (SQLException e) {
            callback.errorOccurred(e.getMessage());
        }
    }

    @Nullable
    @Override
    public String getInitialValueEditorText() {
        return value == null ? null : value.getValue();
    }
}
