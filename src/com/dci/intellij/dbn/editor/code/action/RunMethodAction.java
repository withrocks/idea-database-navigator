package com.dci.intellij.dbn.editor.code.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.execution.compiler.options.CompilerSettings;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;

public class RunMethodAction extends AbstractSourceCodeEditorAction {
    public RunMethodAction() {
        super("Run Method", "", Icons.METHOD_EXECUTION_RUN);
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        DBSourceCodeVirtualFile virtualFile = getSourcecodeFile(e);
        FileEditor fileEditor = getFileEditor(e);
        if (virtualFile != null && fileEditor != null) {
            Project project = virtualFile.getProject();

            DBMethod method = (DBMethod) virtualFile.getObject();
            MethodExecutionManager executionManager = MethodExecutionManager.getInstance(project);
            if (executionManager.promptExecutionDialog(method, false)) {
                executionManager.execute(method);
            }
        }
    }

    public void update(@NotNull AnActionEvent e) {
        DBSourceCodeVirtualFile virtualFile = getSourcecodeFile(e);
        Presentation presentation = e.getPresentation();
        boolean visible = false;
        if (virtualFile != null) {
            DBSchemaObject schemaObject = virtualFile.getObject();
            if (schemaObject != null && schemaObject.getObjectType().matches(DBObjectType.METHOD)) {
                visible = true;
            }
        }

        presentation.setVisible(visible);
        presentation.setText("Run Method");
    }

    private static CompilerSettings getCompilerSettings(Project project) {
        return ExecutionEngineSettings.getInstance(project).getCompilerSettings();
    }
}
