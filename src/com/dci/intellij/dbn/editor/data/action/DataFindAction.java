package com.dci.intellij.dbn.editor.data.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.editor.data.DatasetEditor;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;

public class DataFindAction extends AbstractDataEditorAction {
    public DataFindAction() {
        super("Find...", Icons.ACTION_FIND);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        DatasetEditor datasetEditor = getDatasetEditor(e);
        if (datasetEditor != null) {
            datasetEditor.showSearchHeader();
/*
            FindModel findModel = findManager.getFindInFileModel();

            findManager.showFindDialog(findModel, new Runnable() {
                @Override
                public void run() {
                    datasetEditor.getEditorForm().showSearchPanel();
                }
            });
*/

        }
    }

    public void update(AnActionEvent e) {
        DatasetEditor datasetEditor = getDatasetEditor(e);

        Presentation presentation = e.getPresentation();
        presentation.setText("Find Data...");

        if (datasetEditor == null) {
            presentation.setEnabled(false);
        } else {
            presentation.setEnabled(true);
            if (datasetEditor.isInserting() || datasetEditor.isLoading()) {
                presentation.setEnabled(false);
            }
        }

    }
}
