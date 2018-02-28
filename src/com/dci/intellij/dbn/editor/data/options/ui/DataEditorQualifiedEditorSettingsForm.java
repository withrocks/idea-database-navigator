package com.dci.intellij.dbn.editor.data.options.ui;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.ui.list.CheckBoxList;
import com.dci.intellij.dbn.editor.data.options.DataEditorQualifiedEditorSettings;
import com.intellij.openapi.options.ConfigurationException;
import static com.dci.intellij.dbn.common.options.ui.ConfigurationEditorUtil.validateIntegerInputValue;

public class DataEditorQualifiedEditorSettingsForm extends ConfigurationEditorForm<DataEditorQualifiedEditorSettings> {
    private JPanel mainPanel;
    private JScrollPane listScrollPane;
    private JTextField textLengthThresholdTextField;
    private CheckBoxList checkBoxList;

    public DataEditorQualifiedEditorSettingsForm(DataEditorQualifiedEditorSettings settings) {
        super(settings);
        checkBoxList = new CheckBoxList(settings.getContentTypes());
        listScrollPane.setViewportView(checkBoxList);
        updateBorderTitleForeground(mainPanel);
        resetFormChanges();

        registerComponent(mainPanel);
    }

    @Override
    public void applyFormChanges() throws ConfigurationException {
        DataEditorQualifiedEditorSettings settings = getConfiguration();
        checkBoxList.applyChanges();
        settings.setTextLengthThreshold(validateIntegerInputValue(
                textLengthThresholdTextField,
                "Text Length Threshold", 0, 999999999, null));
    }

    @Override
    public void resetFormChanges() {
        DataEditorQualifiedEditorSettings settings = getConfiguration();
        textLengthThresholdTextField.setText(Integer.toString(settings.getTextLengthThreshold()));
    }

    public JComponent getComponent() {
        return mainPanel;
    }
}
