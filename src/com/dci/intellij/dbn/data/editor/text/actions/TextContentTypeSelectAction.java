package com.dci.intellij.dbn.data.editor.text.actions;

import com.dci.intellij.dbn.data.editor.text.TextContentType;
import com.dci.intellij.dbn.data.editor.text.ui.TextEditorForm;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class TextContentTypeSelectAction extends AnAction {
    private TextEditorForm editorForm;
    private TextContentType contentType;

    public TextContentTypeSelectAction(TextEditorForm editorForm, TextContentType contentType) {
        super(contentType.getName(), null, contentType.getIcon());
        this.contentType = contentType;
        this.editorForm = editorForm;
    }

    public TextContentType getContentType() {
        return contentType;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        editorForm.setContentType(contentType);

    }
}
