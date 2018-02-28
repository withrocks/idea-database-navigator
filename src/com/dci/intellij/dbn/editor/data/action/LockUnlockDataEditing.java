package com.dci.intellij.dbn.editor.data.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.editor.data.DatasetEditor;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.DumbAware;

public class LockUnlockDataEditing extends ToggleAction implements DumbAware {

    public LockUnlockDataEditing() {
        super("Lock / Unlock Editing", null, Icons.DATA_EDITOR_LOCK_EDITING);
    }

    public boolean isSelected(AnActionEvent e) {
        DatasetEditor datasetEditor = getDatasetEditor(e);
        return datasetEditor != null && datasetEditor.isReadonly();
    }

    public void setSelected(AnActionEvent e, boolean selected) {
        DatasetEditor datasetEditor = getDatasetEditor(e);
        if (datasetEditor != null) datasetEditor.setReadonly(selected);
    }

    public void update(AnActionEvent e) {
        super.update(e);
        DatasetEditor datasetEditor = getDatasetEditor(e);
        Presentation presentation = e.getPresentation();
        if (datasetEditor == null) {
            presentation.setEnabled(false);
        } else {
            presentation.setVisible(!datasetEditor.isReadonlyData());
            presentation.setEnabled(datasetEditor.getActiveConnection().isConnected());
            presentation.setText(isSelected(e) ? "Unlock Editing" : "Lock Editing");
            boolean enabled =
                    datasetEditor.getEditorTable() != null &&
                            !datasetEditor.isInserting();
            presentation.setEnabled(enabled);
        }
    }

    private static DatasetEditor getDatasetEditor(AnActionEvent e) {
        FileEditor fileEditor = e.getData(PlatformDataKeys.FILE_EDITOR);
        return fileEditor instanceof DatasetEditor ? (DatasetEditor) fileEditor : null;
    }
}