package com.dci.intellij.dbn.execution.method.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBProgram;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;

public class RunMethodAction extends DumbAwareAction {
    private DBMethod method;
    public RunMethodAction(DBMethod method) {
        super("Run", "", Icons.METHOD_EXECUTION_RUN);
        this.method = method;
    }

    public RunMethodAction(DBProgram program, DBMethod method) {
        super(NamingUtil.enhanceUnderscoresForDisplay(method.getName()), "", method.getIcon());
        this.method = method;
    }

    public void actionPerformed(AnActionEvent e) {
        MethodExecutionManager executionManager = MethodExecutionManager.getInstance(method.getProject());
        if (executionManager.promptExecutionDialog(method, false)) {
            executionManager.execute(method);
        }
    }
}
