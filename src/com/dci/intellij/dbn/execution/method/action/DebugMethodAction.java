package com.dci.intellij.dbn.execution.method.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBProgram;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;

public class DebugMethodAction extends DumbAwareAction {
    private DBMethod method;
    public DebugMethodAction(DBMethod method) {
        super("Debug...", "", Icons.METHOD_EXECUTION_DEBUG);
        this.method = method;
    }

    public DebugMethodAction(DBProgram program, DBMethod method) {
        super(NamingUtil.enhanceUnderscoresForDisplay(method.getName()), "", method.getIcon());
        this.method = method;
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        DatabaseDebuggerManager executionManager = DatabaseDebuggerManager.getInstance(method.getProject());
        executionManager.createDebugConfiguration(method);
    }
}
