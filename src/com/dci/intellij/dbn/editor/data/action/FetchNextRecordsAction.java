package com.dci.intellij.dbn.editor.data.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.editor.data.DatasetEditor;
import com.dci.intellij.dbn.editor.data.options.DataEditorSettings;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;

public class FetchNextRecordsAction extends AbstractDataEditorAction {

    public FetchNextRecordsAction() {
        super("Fetch next records", Icons.DATA_EDITOR_FETCH_NEXT_RECORDS);
    }

    public void actionPerformed(AnActionEvent e) {
        DatasetEditor datasetEditor = getDatasetEditor(e);
        if (datasetEditor != null) {
            DataEditorSettings settings = DataEditorSettings.getInstance(datasetEditor.getProject());
            datasetEditor.fetchNextRecords(settings.getGeneralSettings().getFetchBlockSize().value());
        }
    }

    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setText("Fetch Next Records");

        DatasetEditor datasetEditor = getDatasetEditor(e);
        Project project = ActionUtil.getProject(e);
        if (project != null) {
            DataEditorSettings settings = DataEditorSettings.getInstance(project);
            presentation.setText("Fetch Next " + settings.getGeneralSettings().getFetchBlockSize().value() + " Records");
        }
        boolean enabled =
                datasetEditor != null &&
                datasetEditor.getEditorTable() != null &&
                datasetEditor.getActiveConnection().isConnected() &&
                !datasetEditor.isInserting() &&
                !datasetEditor.isLoading() &&        
                !datasetEditor.getEditorTable().getModel().isResultSetExhausted();
        presentation.setEnabled(enabled);

    }
}