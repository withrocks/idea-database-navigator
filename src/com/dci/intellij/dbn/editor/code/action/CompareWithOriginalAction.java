package com.dci.intellij.dbn.editor.code.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;

public class CompareWithOriginalAction extends AbstractDiffAction {
    public CompareWithOriginalAction() {
        super("Compare with original", null, Icons.CODE_EDITOR_DIFF);
    }

    public void actionPerformed(AnActionEvent e) {
        Editor editor = getEditor(e);
        DBSourceCodeVirtualFile virtualFile = getSourcecodeFile(e);
        if (editor != null && virtualFile != null) {
            String content = editor.getDocument().getText();
            virtualFile.setContent(content);
            String referenceText = virtualFile.getOriginalContent();

            openDiffWindow(e, referenceText, "Original version", "Local version");
        }
    }

    public void update(AnActionEvent e) {
        DBSourceCodeVirtualFile virtualFile = getSourcecodeFile(e);
        e.getPresentation().setText("Compare with Original");
        e.getPresentation().setEnabled(virtualFile != null && (
                virtualFile.getOriginalContent() != null || virtualFile.isModified()));
    }
}
