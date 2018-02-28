package com.dci.intellij.dbn.editor.data.filter.ui;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilter;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilterGroup;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilterImpl;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilterManager;
import com.dci.intellij.dbn.editor.data.filter.action.CreateFilterAction;
import com.dci.intellij.dbn.editor.data.filter.action.DeleteFilterAction;
import com.dci.intellij.dbn.editor.data.filter.action.MoveFilterDownAction;
import com.dci.intellij.dbn.editor.data.filter.action.MoveFilterUpAction;
import com.dci.intellij.dbn.object.DBDataset;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.ui.GuiUtils;
import com.intellij.util.ui.UIUtil;

public class DatasetFilterForm extends ConfigurationEditorForm<DatasetFilterGroup> implements ListSelectionListener {
    private Map<String, ConfigurationEditorForm> filterDetailPanels = new HashMap<String, ConfigurationEditorForm>();
    private static final String BLANK_PANEL_ID = "BLANK_PANEL";

    private JPanel mainPanel;
    private JList filtersList;
    private JPanel filterDetailsPanel;
    private JPanel actionsPanel;
    private JPanel headerPanel;

    private DBDataset dataset;

    public DatasetFilterForm(DatasetFilterGroup filterGroup, DBDataset dataset) {
        super(filterGroup);
        filtersList.setModel(filterGroup);
        filtersList.setFont(UIUtil.getLabelFont());
        this.dataset = dataset;
        Project project = dataset.getProject();

        DBNHeaderForm headerForm = new DBNHeaderForm(dataset);
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);

        DatasetFilterList filters = getFilterList();
        ActionToolbar actionToolbar = ActionUtil.createActionToolbar(
                "DBNavigator.DataEditor.FiltersList", true,
                new CreateFilterAction(filters),
                new DeleteFilterAction(filters),
                new MoveFilterUpAction(filters),
                new MoveFilterDownAction(filters));
        actionsPanel.add(actionToolbar.getComponent(), BorderLayout.CENTER);
        filterDetailsPanel.add(new JPanel(), BLANK_PANEL_ID);

        DatasetFilterManager filterManager = DatasetFilterManager.getInstance(project);
        DatasetFilter filter = filterManager.getActiveFilter(dataset);
        if (filter != null) {
            filtersList.setSelectedValue(filter, true);
        }
        valueChanged(null);
        GuiUtils.replaceJSplitPaneWithIDEASplitter(mainPanel);
        filtersList.addListSelectionListener(this);
    }

    public DBDataset getDataset() {
        return dataset;
    }

    public DatasetFilterList getFilterList() {
        return (DatasetFilterList) filtersList;
    }

    public DatasetFilter getSelectedFilter() {
        return (DatasetFilter) filtersList.getSelectedValue();
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyFormChanges() throws ConfigurationException {
        getFilterList().getFilterGroup().apply();
    }

    public void resetFormChanges() {
        getFilterList().getFilterGroup().reset();
    }

    private void createUIComponents() {
        filtersList = new DatasetFilterList();
    }

    public void dispose() {
        for (ConfigurationEditorForm configurationEditorForm : filterDetailPanels.values()) {
            configurationEditorForm.dispose();
        }
        filterDetailPanels.clear();
        super.dispose();
    }

    public void valueChanged(ListSelectionEvent e) {
        DatasetFilterGroup configuration = getConfiguration();
        if (configuration != null && (e == null || !e.getValueIsAdjusting())) {
            int[] indices = filtersList.getSelectedIndices();
            List<DatasetFilter> filters = configuration.getFilters();
            DatasetFilterImpl filter = filters.size() > 0 && indices.length == 1 ? (DatasetFilterImpl) filters.get(indices[0]) : null;

            CardLayout cardLayout = (CardLayout) filterDetailsPanel.getLayout();
            if (filter == null) {
                cardLayout.show(filterDetailsPanel, BLANK_PANEL_ID);
            } else {
                String id = filter.getId();
                ConfigurationEditorForm configurationEditorForm = filterDetailPanels.get(id);
                if (configurationEditorForm == null) {
                    filterDetailsPanel.add(filter.createComponent(), id);
                    filterDetailPanels.put(id, filter.getSettingsEditor());
                }
                cardLayout.show(filterDetailsPanel, id);
                filter.getSettingsEditor().focus();
            }
        }
    }
}
