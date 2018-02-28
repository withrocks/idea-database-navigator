package com.dci.intellij.dbn.data.editor.text.actions;

import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.ui.DBNComboBoxAction;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.data.editor.text.TextContentType;
import com.dci.intellij.dbn.data.editor.text.ui.TextEditorForm;
import com.dci.intellij.dbn.editor.data.options.DataEditorQualifiedEditorSettings;
import com.dci.intellij.dbn.editor.data.options.DataEditorSettings;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;

public class TextContentTypeComboBoxAction extends DBNComboBoxAction {
    private TextEditorForm editorForm;

    public TextContentTypeComboBoxAction(TextEditorForm editorForm) {
        this.editorForm = editorForm;
        Presentation presentation = getTemplatePresentation();
        TextContentType contentType = editorForm.getContentType();
        presentation.setText(contentType.getName());
        presentation.setIcon(contentType.getIcon());
    }

    @NotNull
    protected DefaultActionGroup createPopupActionGroup(JComponent button) {
        Project project = ActionUtil.getProject(button);
        DataEditorQualifiedEditorSettings qualifiedEditorSettings = DataEditorSettings.getInstance(project).getQualifiedEditorSettings();
        
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        for (TextContentType contentType : qualifiedEditorSettings.getContentTypes()) {
            if (contentType.isSelected()) {
                actionGroup.add(new TextContentTypeSelectAction(editorForm, contentType));
            }

        }
        return actionGroup;
    }

    @Override
    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        TextContentType contentType = editorForm.getContentType();
        presentation.setText(contentType.getName());
        presentation.setIcon(contentType.getIcon());
    }
}
