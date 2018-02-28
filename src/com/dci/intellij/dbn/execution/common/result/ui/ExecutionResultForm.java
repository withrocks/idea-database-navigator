package com.dci.intellij.dbn.execution.common.result.ui;

import com.dci.intellij.dbn.common.ui.DBNForm;
import com.dci.intellij.dbn.execution.ExecutionResult;

public interface ExecutionResultForm<E extends ExecutionResult> extends DBNForm {
    void setExecutionResult(E executionResult);
    E getExecutionResult();
}
