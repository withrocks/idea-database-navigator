package com.dci.intellij.dbn.code.common.style.options.ui;

import com.dci.intellij.dbn.code.common.style.options.CodeStyleCase;
import com.dci.intellij.dbn.code.common.style.options.CodeStyleCaseSettings;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.ui.DBNComboBox;
import com.intellij.openapi.options.ConfigurationException;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CodeStyleCaseSettingsForm extends ConfigurationEditorForm<CodeStyleCaseSettings> {
    private JPanel mainPanel;
    private DBNComboBox<CodeStyleCase> keywordCaseComboBox;
    private DBNComboBox<CodeStyleCase> functionCaseComboBox;
    private DBNComboBox<CodeStyleCase> parameterCaseComboBox;
    private DBNComboBox<CodeStyleCase> datatypeCaseComboBox;
    private DBNComboBox<CodeStyleCase> objectCaseComboBox;
    private JCheckBox enableCheckBox;

    public static final CodeStyleCase[] OBJECT_STYLE_CASES = new CodeStyleCase[]{
            CodeStyleCase.PRESERVE,
            CodeStyleCase.UPPER,
            CodeStyleCase.LOWER,
            CodeStyleCase.CAPITALIZED};

    public static final CodeStyleCase[] KEYWORD_STYLE_CASES = new CodeStyleCase[]{
            CodeStyleCase.UPPER,
            CodeStyleCase.LOWER,
            CodeStyleCase.CAPITALIZED};

    public CodeStyleCaseSettingsForm(CodeStyleCaseSettings settings) {
        super(settings);
        updateBorderTitleForeground(mainPanel);

        keywordCaseComboBox.setValues(KEYWORD_STYLE_CASES);
        functionCaseComboBox.setValues(KEYWORD_STYLE_CASES);
        parameterCaseComboBox.setValues(KEYWORD_STYLE_CASES);
        datatypeCaseComboBox.setValues(KEYWORD_STYLE_CASES);
        objectCaseComboBox.setValues(OBJECT_STYLE_CASES);
        resetFormChanges();
        enableDisableOptions();

        registerComponent(mainPanel);
        enableCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enableDisableOptions();
            }
        });



        //Shortcut[] basicShortcuts = KeyUtil.getShortcuts("ReformatCode");
        //enableCheckBox.setText("Use on reformat code (" + KeymapUtil.getShortcutsText(basicShortcuts) + ")");
    }

    private void enableDisableOptions() {
        boolean enabled = enableCheckBox.isSelected();
        keywordCaseComboBox.setEnabled(enabled);
        functionCaseComboBox.setEnabled(enabled);
        parameterCaseComboBox.setEnabled(enabled);
        datatypeCaseComboBox.setEnabled(enabled);
        objectCaseComboBox.setEnabled(enabled);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyFormChanges() throws ConfigurationException {
        CodeStyleCaseSettings settings = getConfiguration();
        settings.getKeywordCaseOption().setStyleCase(keywordCaseComboBox.getSelectedValue());
        settings.getFunctionCaseOption().setStyleCase(functionCaseComboBox.getSelectedValue());
        settings.getParameterCaseOption().setStyleCase(parameterCaseComboBox.getSelectedValue());
        settings.getDatatypeCaseOption().setStyleCase(datatypeCaseComboBox.getSelectedValue());
        settings.getObjectCaseOption().setStyleCase(objectCaseComboBox.getSelectedValue());
        settings.setEnabled(enableCheckBox.isSelected());
    }

    public void resetFormChanges() {
        CodeStyleCaseSettings settings = getConfiguration();
        keywordCaseComboBox.setSelectedValue(settings.getKeywordCaseOption().getStyleCase());
        functionCaseComboBox.setSelectedValue(settings.getFunctionCaseOption().getStyleCase());
        parameterCaseComboBox.setSelectedValue(settings.getParameterCaseOption().getStyleCase());
        datatypeCaseComboBox.setSelectedValue(settings.getDatatypeCaseOption().getStyleCase());
        objectCaseComboBox.setSelectedValue(settings.getObjectCaseOption().getStyleCase());
        enableCheckBox.setSelected(settings.isEnabled());
    }
}
