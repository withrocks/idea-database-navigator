package com.dci.intellij.dbn.data.record.navigation.action;

import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.editor.data.DatasetEditorManager;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilterInput;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

public class OpenRecordEditorAction extends AnAction{
    private DatasetFilterInput filterInput;

    public OpenRecordEditorAction(DatasetFilterInput filterInput) {
        super("Editor");
        this.filterInput = filterInput;
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = ActionUtil.getProject(event);
        if (project != null) {
            DatasetEditorManager datasetEditorManager = DatasetEditorManager.getInstance(project);
            datasetEditorManager.openDataEditor(filterInput);
        }
    }
}
