package com.dci.intellij.dbn.editor.data.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.editor.data.DatasetEditor;
import com.dci.intellij.dbn.editor.data.ui.table.DatasetEditorTable;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;

public class DuplicateRecordAction extends AbstractDataEditorAction {

    public DuplicateRecordAction() {
        super("Duplicate record", Icons.DATA_EDITOR_DUPLICATE_RECORD);
    }

    public void actionPerformed(AnActionEvent e) {
        DatasetEditor datasetEditor = getDatasetEditor(e);
        if (datasetEditor != null) {
            datasetEditor.duplicateRecord();
        }
    }

    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setText("Duplicate Record");
        DatasetEditor datasetEditor = getDatasetEditor(e);

        if (datasetEditor == null ||!datasetEditor.getActiveConnection().isConnected()) {
            presentation.setEnabled(false);
        } else {
            presentation.setEnabled(true);
            presentation.setVisible(!datasetEditor.isReadonlyData());
            if (datasetEditor.isInserting() || datasetEditor.isLoading() || datasetEditor.isReadonly()) {
                presentation.setEnabled(false);
            } else {
                DatasetEditorTable editorTable = datasetEditor.getEditorTable();
                int[] selectedrows =
                        editorTable == null ? null :
                        editorTable.getSelectedRows();
                presentation.setEnabled(selectedrows != null && selectedrows.length == 1 && selectedrows[0] < editorTable.getModel().getSize());
            }
        }
    }
}