package com.dci.intellij.dbn.common.editor.document;

import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.ReadOnlyFragmentModificationException;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.editor.actionSystem.ReadonlyFragmentModificationHandler;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;

public class OverrideReadonlyFragmentModificationHandler implements
        ReadonlyFragmentModificationHandler {

    public static final Key<String> GUARDED_BLOCK_REASON = Key.create("GUARDED_BLOCK_REASON");

    private static ReadonlyFragmentModificationHandler originalHandler = EditorActionManager.getInstance().getReadonlyFragmentModificationHandler();
    public static final ReadonlyFragmentModificationHandler INSTANCE = new OverrideReadonlyFragmentModificationHandler();
    private OverrideReadonlyFragmentModificationHandler() {

    }

    public void handle(ReadOnlyFragmentModificationException e) {
        Document document = e.getGuardedBlock().getDocument();
        String message = document.getUserData(GUARDED_BLOCK_REASON);
        if (message != null) {
            MessageUtil.showErrorDialog(null, "Action denied", message);
        } else {
            VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
            if (virtualFile instanceof DBSourceCodeVirtualFile || virtualFile instanceof LightVirtualFile) {
                //Messages.showErrorDialog("You're not allowed to change name and type of the edited component.", "Action denied");
            } else {
                originalHandler.handle(e);
            }
        }
    }
}
