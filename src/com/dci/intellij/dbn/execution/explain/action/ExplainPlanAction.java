package com.dci.intellij.dbn.execution.explain.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.execution.explain.ExplainPlanManager;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.ExecutablePsiElement;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;

public class ExplainPlanAction extends AnAction {
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (project != null && editor != null) {
            FileEditor fileEditor = EditorUtil.getFileEditor(editor);
            ExecutablePsiElement executable = PsiUtil.lookupExecutableAtCaret(editor, true);
            if (fileEditor != null && executable != null && executable.is(ElementTypeAttribute.DATA_MANIPULATION)) {
                ExplainPlanManager explainPlanManager = ExplainPlanManager.getInstance(project);
                explainPlanManager.explainPlan(executable, null);
            }
        }
    }

    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setIcon(Icons.STMT_EXECUTION_EXPLAIN);
        presentation.setText("Explain Plan for Statement");

        boolean visible = false;
        boolean enabled = false;

        Project project = ActionUtil.getProject(e);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (project != null && editor != null) {
            PsiFile psiFile = PsiUtil.getPsiFile(project, editor.getDocument());
            if (psiFile instanceof DBLanguagePsiFile) {
                DBLanguagePsiFile languagePsiFile = (DBLanguagePsiFile) psiFile;

                ConnectionHandler activeConnection = languagePsiFile.getActiveConnection();
                visible = DatabaseFeature.EXPLAIN_PLAN.isSupported(activeConnection);

                ExecutablePsiElement executable = PsiUtil.lookupExecutableAtCaret(editor, true);
                if (executable != null && executable.is(ElementTypeAttribute.DATA_MANIPULATION)) {
                    enabled = true;
                }
            }
        }
        presentation.setEnabled(enabled);
        presentation.setVisible(visible);
    }

    private boolean isEnabled(AnActionEvent e) {
        return false;
    }
}
