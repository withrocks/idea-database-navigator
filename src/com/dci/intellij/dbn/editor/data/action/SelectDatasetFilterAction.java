package com.dci.intellij.dbn.editor.data.action;

import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.editor.data.DatasetEditorManager;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilter;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilterManager;
import com.dci.intellij.dbn.object.DBDataset;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;

public class SelectDatasetFilterAction extends DumbAwareAction {
    private DBDataset dataset;
    private DatasetFilter filter;

    protected SelectDatasetFilterAction(DBDataset dataset, DatasetFilter filter) {
        this.dataset = dataset;
        this.filter = filter;
    }

    public void actionPerformed(AnActionEvent e) {
        final Project project = dataset.getProject();
        DatasetFilterManager filterManager = DatasetFilterManager.getInstance(project);
        DatasetFilter activeFilter = filterManager.getActiveFilter(dataset);
        if (activeFilter != filter) {
            filterManager.setActiveFilter(dataset, filter);
            DatasetEditorManager.getInstance(project).reloadEditorData(dataset);
        }
    }

    @Override
    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setIcon(filter.getIcon());
        presentation.setText(NamingUtil.enhanceNameForDisplay(filter.getName()));
        //presentation.setEnabled(dataset.getConnectionHandler().isConnected());
        //e.getPresentation().setText(filter.getName());
    }
}
