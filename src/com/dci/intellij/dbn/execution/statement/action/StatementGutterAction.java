package com.dci.intellij.dbn.execution.statement.action;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.execution.statement.StatementExecutionManager;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionCursorProcessor;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionResult;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionStatus;
import com.dci.intellij.dbn.language.common.psi.ExecutablePsiElement;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;

public class StatementGutterAction extends AnAction {
    final ExecutablePsiElement executablePsiElement;

    public StatementGutterAction(ExecutablePsiElement executablePsiElement) {
        this.executablePsiElement = executablePsiElement;
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        StatementExecutionManager executionManager = getExecutionManager();
        if (executionManager != null) {
            StatementExecutionProcessor executionProcessor = getExecutionProcessor(false);

            if (executionProcessor == null) {
                executionProcessor = getExecutionProcessor(true);
                executionManager.executeStatement(executionProcessor);
            } else {
                StatementExecutionResult executionResult = executionProcessor.getExecutionResult();
                if (executionResult == null || !(executionProcessor instanceof StatementExecutionCursorProcessor) || executionProcessor.isDirty()) {
                    executionManager.executeStatement(executionProcessor);
                } else {
                    executionProcessor.navigateToResult();
                }
            }
        }
    }

    @NotNull
    public Icon getIcon() {
        StatementExecutionProcessor executionProcessor = getExecutionProcessor(false);
        if (executionProcessor != null) {
            StatementExecutionResult executionResult = executionProcessor.getExecutionResult();
            if (executionResult == null) {
                return Icons.STMT_EXECUTION_RUN;
            } else {
                StatementExecutionStatus executionStatus = executionResult.getExecutionStatus();
                if (executionStatus == StatementExecutionStatus.SUCCESS){
                    if (executionProcessor instanceof StatementExecutionCursorProcessor) {
                        return executionProcessor.isDirty() ?
                                Icons.STMT_EXEC_RESULTSET_RERUN :
                                Icons.STMT_EXEC_RESULTSET;
                    } else {
                        return Icons.STMT_EXECUTION_INFO_RERUN;
                    }
                } else if (executionStatus == StatementExecutionStatus.ERROR){
                    return Icons.STMT_EXECUTION_ERROR_RERUN;
                } else if (executionStatus == StatementExecutionStatus.WARNING){
                    return Icons.STMT_EXECUTION_WARNING_RERUN;
                }
            }
        }


        return Icons.STMT_EXECUTION_RUN;
    }

    @Nullable
    private StatementExecutionManager getExecutionManager() {
        if (executablePsiElement.isValid()) {
            Project project = executablePsiElement.getProject();
            return StatementExecutionManager.getInstance(project);
        } else {
            return null;
        }
    }

    @Nullable
    private StatementExecutionProcessor getExecutionProcessor(boolean create) {
        StatementExecutionManager executionManager = getExecutionManager();
        if (executionManager != null) {
            Document document = DocumentUtil.getDocument(executablePsiElement.getFile());
            FileEditorManager fileEditorManager = FileEditorManager.getInstance(executablePsiElement.getProject());
            FileEditor[] selectedEditors = fileEditorManager.getSelectedEditors();
            for (FileEditor fileEditor : selectedEditors) {
                Editor editor = EditorUtil.getEditor(fileEditor);
                if (editor != null) {
                    if (editor.getDocument() == document) {
                        return executionManager.getExecutionProcessor(fileEditor, executablePsiElement, create);
                    }
                }

            }
        }
        return null;
    }


    @Nullable
    public String getTooltipText() {
        StatementExecutionProcessor executionProcessor = getExecutionProcessor(false);
        if (executionProcessor!= null && !executionProcessor.isDisposed()) {
            StatementExecutionResult executionResult = executionProcessor.getExecutionResult();
            if (executionResult != null) {
                StatementExecutionStatus executionStatus = executionResult.getExecutionStatus();
                if (executionStatus == StatementExecutionStatus.SUCCESS) {
                    return "Statement executed successfully. Execute again?";
                } else if (executionStatus == StatementExecutionStatus.ERROR) {
                    return "Statement executed with errors. Execute again?";
                } else if (executionStatus == StatementExecutionStatus.WARNING) {
                    return "Statement executed with warnings. Execute again?";
                }
            }
        }
        return null;//"Execute statement";
    }

}
