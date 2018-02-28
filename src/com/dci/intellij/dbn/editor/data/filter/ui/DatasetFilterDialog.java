package com.dci.intellij.dbn.editor.data.filter.ui;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.editor.data.DatasetEditorManager;
import com.dci.intellij.dbn.editor.data.filter.DatasetBasicFilter;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilter;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilterGroup;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilterManager;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilterType;
import com.dci.intellij.dbn.object.DBDataset;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;

public class DatasetFilterDialog extends DBNDialog<DatasetFilterForm> {
    private boolean isAutomaticPrompt;
    private DBObjectRef<DBDataset> datasetRef;
    private DatasetFilterGroup filterGroup;

    public DatasetFilterDialog(DBDataset dataset, boolean isAutomaticPrompt, boolean createNewFilter, DatasetFilterType defaultFilterType) {
        super(dataset.getProject(), "Data Filters", true);
        construct(dataset, isAutomaticPrompt);
        if ((createNewFilter || filterGroup.getFilters().isEmpty()) && defaultFilterType != DatasetFilterType.NONE) {
            DatasetFilter filter =
                    defaultFilterType == DatasetFilterType.BASIC ? filterGroup.createBasicFilter(true) :
                    defaultFilterType == DatasetFilterType.CUSTOM ? filterGroup.createCustomFilter(true) : null;

            component.getFilterList().setSelectedValue(filter, true);
        }
        init();
    }

    private DBDataset getDataset() {
        return DBObjectRef.get(datasetRef);
    }

    public DatasetFilterDialog(DBDataset dataset, DatasetBasicFilter basicFilter) {
        super(dataset.getProject(), "Data filters", true);
        construct(dataset, false);
        component.getFilterList().setSelectedValue(basicFilter, true);
        init();
    }

    private void construct(DBDataset dataset, boolean isAutomaticPrompt) {
        this.datasetRef = DBObjectRef.from(dataset);
        this.isAutomaticPrompt = isAutomaticPrompt;
        setModal(true);
        setResizable(true);
        DatasetFilterManager filterManager = DatasetFilterManager.getInstance(dataset.getProject());
        filterGroup = filterManager.getFilterGroup(dataset);
        component = filterGroup.createConfigurationEditor();
    }

    public DatasetFilterGroup getFilterGroup() {
        return filterGroup;
    }

    @NotNull
    protected final Action[] createActions() {
        if (isAutomaticPrompt) {
            return new Action[]{
                    getOKAction(),
                    new NoFilterAction(),
                    getCancelAction(),
                    getHelpAction()
            };
        } else {
            return new Action[]{
                    getOKAction(),
                    getCancelAction(),
                    getHelpAction()
            };
        }
    }

    private class NoFilterAction extends AbstractAction {
        public NoFilterAction() {
            super("No Filter");
            //putValue(DEFAULT_ACTION, Boolean.FALSE);
        }

        public void actionPerformed(ActionEvent e) {
            doNoFilterAction();
        }
    }

    public void doOKAction() {
        Project project = getProject();
        DBDataset dataset = getDataset();
        try {
            component.applyFormChanges();
            DatasetFilterManager filterManager = DatasetFilterManager.getInstance(project);
            DatasetFilter activeFilter = component.getSelectedFilter();
            if (activeFilter == null) {
                activeFilter = DatasetFilterManager.EMPTY_FILTER;
            }
            filterManager.setActiveFilter(dataset, activeFilter);
        } catch (ConfigurationException e) {
            e.printStackTrace(); 
        }
        super.doOKAction();
        if (!isAutomaticPrompt) DatasetEditorManager.getInstance(project).reloadEditorData(dataset);
    }

    public void doCancelAction() {
        component.resetFormChanges();
        super.doCancelAction();
    }

    public void doNoFilterAction() {
        component.resetFormChanges();
        DBDataset dataset = getDataset();
        Project project = getProject();
        DatasetFilterManager filterManager = DatasetFilterManager.getInstance(project);
        DatasetFilter activeFilter = filterManager.getActiveFilter(dataset);
        if (activeFilter == null) {
            activeFilter = DatasetFilterManager.EMPTY_FILTER;
            filterManager.setActiveFilter(dataset, activeFilter);
        }
        close(OK_EXIT_CODE);
    }
}
