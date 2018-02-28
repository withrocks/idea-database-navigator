package com.dci.intellij.dbn.editor.data.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.editor.data.DatasetEditorManager;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilterInput;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;

public abstract class ShowRecordsAction extends DumbAwareAction{
    private DatasetFilterInput filterInput;

    public ShowRecordsAction(String text, DatasetFilterInput filterInput) {
        super(text, null, Icons.DBO_TABLE);
        this.filterInput = filterInput;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = filterInput.getDataset().getProject();
        DatasetEditorManager datasetEditorManager = DatasetEditorManager.getInstance(project);
        datasetEditorManager.openDataEditor(filterInput);
    }
}
