package com.dci.intellij.dbn.execution.compiler.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.execution.compiler.CompileTypeOption;
import com.dci.intellij.dbn.execution.compiler.CompilerAction;
import com.dci.intellij.dbn.execution.compiler.CompilerActionSource;
import com.dci.intellij.dbn.execution.compiler.DatabaseCompilerManager;
import com.dci.intellij.dbn.execution.compiler.options.CompilerSettings;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.common.status.DBObjectStatus;
import com.dci.intellij.dbn.object.common.status.DBObjectStatusHolder;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;

public class CompileObjectAction extends AnAction {
    private DBSchemaObject object;
    private DBContentType contentType;

    public CompileObjectAction(DBSchemaObject object, DBContentType contentType) {
        super("Compile");
        this.object = object;
        this.contentType = contentType;
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        DatabaseCompilerManager compilerManager = DatabaseCompilerManager.getInstance(object.getProject());
        CompileTypeOption compileType = getCompilerSettings(object.getProject()).getCompileTypeOption();
        CompilerAction compilerAction = new CompilerAction(CompilerActionSource.COMPILE, contentType);
        compilerManager.compileInBackground(object, compileType, compilerAction);
    }

    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();

        CompilerSettings compilerSettings = getCompilerSettings(object.getProject());
        CompileTypeOption compileType = compilerSettings.getCompileTypeOption();
        DBObjectStatusHolder status = object.getStatus();

        boolean isDebug = compileType == CompileTypeOption.DEBUG;
        if (compileType == CompileTypeOption.KEEP) {
            isDebug = status.is(contentType, DBObjectStatus.DEBUG);
        }

        boolean isPresent = status.is(contentType, DBObjectStatus.PRESENT);
        boolean isValid = status.is(contentType, DBObjectStatus.VALID);
        //boolean isDebug = status.is(contentType, DBObjectStatus.DEBUG);
        boolean isCompiling = status.is(contentType, DBObjectStatus.COMPILING);
        boolean isEnabled = isPresent && !isCompiling && (compilerSettings.alwaysShowCompilerControls() || !isValid/* || isDebug != isDebugActive*/);

        presentation.setEnabled(isEnabled);

        String text =
                contentType == DBContentType.CODE_SPEC ? "Compile Spec" :
                contentType == DBContentType.CODE_BODY ? "Compile Body" : "Compile";
        if (isDebug) text = text + " (Debug)";
        if (compileType == CompileTypeOption.ASK) text = text + "...";
        presentation.setText(text);
    }

    private static CompilerSettings getCompilerSettings(Project project) {
        return ExecutionEngineSettings.getInstance(project).getCompilerSettings();
    }
}
