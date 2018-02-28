package com.dci.intellij.dbn.data.grid.options.ui;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorUtil;
import com.dci.intellij.dbn.common.ui.DBNComboBox;
import com.dci.intellij.dbn.data.grid.options.DataGridSortingSettings;
import com.dci.intellij.dbn.data.grid.options.NullSortingOption;
import com.intellij.openapi.options.ConfigurationException;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class DataGridSortingSettingsForm extends ConfigurationEditorForm<DataGridSortingSettings> {
    private JPanel mainPanel;
    private JCheckBox enableZoomingCheckBox;
    private JTextField maxSortingColumnsTextField;
    private DBNComboBox<NullSortingOption> nullsPositionComboBox;

    public DataGridSortingSettingsForm(DataGridSortingSettings settings) {
        super(settings);
        updateBorderTitleForeground(mainPanel);

        nullsPositionComboBox.setValues(NullSortingOption.values());

        resetFormChanges();
        registerComponent(mainPanel);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyFormChanges() throws ConfigurationException {
        DataGridSortingSettings settings = getConfiguration();
        settings.setNullsFirst(nullsPositionComboBox.getSelectedValue() == NullSortingOption.FIRST);
        int maxSortingColumns = ConfigurationEditorUtil.validateIntegerInputValue(maxSortingColumnsTextField, "Max sorting columns", 0, 100, "Use value 0 for unlimited number of sorting columns");
        settings.setMaxSortingColumns(maxSortingColumns);
    }

    public void resetFormChanges() {
        DataGridSortingSettings settings = getConfiguration();
        nullsPositionComboBox.setSelectedValue(settings.isNullsFirst() ? NullSortingOption.FIRST : NullSortingOption.LAST);
        maxSortingColumnsTextField.setText(Integer.toString(settings.getMaxSortingColumns()));
    }
}
