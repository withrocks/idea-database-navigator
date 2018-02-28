package com.dci.intellij.dbn.editor.data.state.sorting.ui;

import javax.swing.Action;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.editor.data.DatasetEditor;
import com.intellij.openapi.project.Project;

public class DatasetEditorSortingDialog extends DBNDialog<DatasetEditorSortingForm> {
    private DatasetEditor datasetEditor;

    public DatasetEditorSortingDialog(Project project, DatasetEditor datasetEditor) {
        super(project, "Sorting", true);
        this.datasetEditor = datasetEditor;
        setModal(true);
        setResizable(true);
        component = new DatasetEditorSortingForm(this, datasetEditor);
        getCancelAction().putValue(Action.NAME, "Cancel");
        init();
    }

    @NotNull
    protected final Action[] createActions() {
        return new Action[]{
                getOKAction(),
                getCancelAction(),
                getHelpAction()
        };
    }

    @Override
    protected void doOKAction() {
        component.applyChanges();
        datasetEditor.getEditorTable().sort();
        super.doOKAction();
    }

    @Override
    public void dispose() {
        super.dispose();
        datasetEditor = null;
    }
}
