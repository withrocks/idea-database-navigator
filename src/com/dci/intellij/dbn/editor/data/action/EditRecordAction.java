package com.dci.intellij.dbn.editor.data.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.editor.data.DatasetEditor;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;

public class EditRecordAction extends AbstractDataEditorAction {

    public EditRecordAction() {
        super("Edit Record", Icons.DATA_EDITOR_EDIT_RECORD);
    }

    public void actionPerformed(AnActionEvent e) {
        DatasetEditor datasetEditor = getDatasetEditor(e);
        if (datasetEditor != null) {
            datasetEditor.openRecordEditor();
        }
    }

    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setText("Edit Record");
        DatasetEditor datasetEditor = getDatasetEditor(e);

        boolean enabled =
                datasetEditor != null &&
                datasetEditor.getActiveConnection().isConnected() &&
                datasetEditor.getEditorTable() != null &&
                datasetEditor.getEditorTable().getSelectedRow() != -1 &&
                !datasetEditor.isInserting() &&
                !datasetEditor.isLoading();
        presentation.setEnabled(enabled);

    }
}