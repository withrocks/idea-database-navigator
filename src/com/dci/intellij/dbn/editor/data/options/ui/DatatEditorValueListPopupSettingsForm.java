package com.dci.intellij.dbn.editor.data.options.ui;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.editor.data.options.DataEditorValueListPopupSettings;
import com.intellij.openapi.options.ConfigurationException;
import static com.dci.intellij.dbn.common.options.ui.ConfigurationEditorUtil.validateIntegerInputValue;

public class DatatEditorValueListPopupSettingsForm extends ConfigurationEditorForm<DataEditorValueListPopupSettings> {
    private JTextField elementCountThresholdTextBox;
    private JTextField dataLengthThresholdTextBox;
    private JCheckBox showPopupButtonCheckBox;
    private JPanel mainPanel;

    public DatatEditorValueListPopupSettingsForm(DataEditorValueListPopupSettings settings) {
        super(settings);
        updateBorderTitleForeground(mainPanel);
        resetFormChanges();
        registerComponent(mainPanel);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyFormChanges() throws ConfigurationException {
        DataEditorValueListPopupSettings settings = getConfiguration();
        settings.setShowPopupButton(showPopupButtonCheckBox.isSelected());
        settings.setElementCountThreshold(validateIntegerInputValue(elementCountThresholdTextBox, "Element count threshold", 0, 10000, null));
        settings.setDataLengthThreshold(validateIntegerInputValue(dataLengthThresholdTextBox, "Data length threshold", 0, 1000, null));
    }

    public void resetFormChanges() {
        DataEditorValueListPopupSettings settings = getConfiguration();
        showPopupButtonCheckBox.setSelected(settings.isShowPopupButton());
        elementCountThresholdTextBox.setText(Integer.toString(settings.getElementCountThreshold()));
        dataLengthThresholdTextBox.setText(Integer.toString(settings.getDataLengthThreshold()));
    }
}
