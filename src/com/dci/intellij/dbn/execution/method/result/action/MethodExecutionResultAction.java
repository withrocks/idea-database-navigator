package com.dci.intellij.dbn.execution.method.result.action;

import javax.swing.*;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.execution.method.result.MethodExecutionResult;
import com.dci.intellij.dbn.execution.method.result.ui.MethodExecutionResultForm;
import com.intellij.openapi.project.DumbAwareAction;

public abstract class MethodExecutionResultAction extends DumbAwareAction {
    private MethodExecutionResultForm executionResultForm;

    public MethodExecutionResultAction(MethodExecutionResultForm executionResultForm, String text, Icon icon) {
        super(text, null, icon);
        this.executionResultForm = executionResultForm;
    }

    @Nullable
    public MethodExecutionResult getExecutionResult() {
        return executionResultForm.getExecutionResult();
    }
}
