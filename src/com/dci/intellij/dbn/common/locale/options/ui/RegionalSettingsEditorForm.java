package com.dci.intellij.dbn.common.locale.options.ui;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Date;
import java.util.Locale;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.locale.DBDateFormat;
import com.dci.intellij.dbn.common.locale.DBNumberFormat;
import com.dci.intellij.dbn.common.locale.Formatter;
import com.dci.intellij.dbn.common.locale.options.RegionalSettings;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.ui.DBNComboBox;
import com.dci.intellij.dbn.common.ui.ValueSelectorListener;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.ui.UIUtil;

public class RegionalSettingsEditorForm extends ConfigurationEditorForm<RegionalSettings> {
    private JPanel mainPanel;
    private DBNComboBox<LocaleOption> localeComboBox;
    private JLabel numberPreviewLabel;
    private JLabel integerPreviewLabel;
    private JLabel datePreviewLabel;
    private JLabel timePreviewLabel;
    private JPanel previewPanel;
    private JTextField customNumberFormatTextField;
    private JTextField customDateFormatTextField;
    private JTextField customTimeFormatTextField;
    private JLabel errorLabel;
    private JRadioButton presetPatternsRadioButton;
    private JRadioButton customPatternsRadioButton;
    private DBNComboBox<DBNumberFormat> numberFormatComboBox;
    private DBNComboBox<DBDateFormat> dateFormatComboBox;

    boolean isUpdating = false;


    private Date previewDate = new Date();
    private double previewNumber = ((double)(System.currentTimeMillis()/1000))/1000;

    public RegionalSettingsEditorForm(RegionalSettings regionalSettings) {
        super(regionalSettings);
        previewPanel.setBorder(UIUtil.getTextFieldBorder());
        previewPanel.setBackground(UIUtil.getToolTipBackground());
        errorLabel.setVisible(false);
        updateBorderTitleForeground(mainPanel);
        localeComboBox.setValues(LocaleOption.ALL);

        numberFormatComboBox.setValues(
                DBNumberFormat.UNGROUPED,
                DBNumberFormat.GROUPED);

        dateFormatComboBox.setValues(
                DBDateFormat.SHORT,
                DBDateFormat.MEDIUM,
                DBDateFormat.LONG);

        ValueSelectorListener previewListener = new ValueSelectorListener() {
            @Override
            public void valueSelected(Object value) {
                updatePreview();
            }
        };
        numberFormatComboBox.addListener(previewListener);
        dateFormatComboBox.addListener(previewListener);

        resetFormChanges();
        updatePreview();

        registerComponent(mainPanel);
    }

    @Override
    protected ItemListener createItemListener() {
        return new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                getConfiguration().setModified(true);
                updatePreview();
            }
        };
    }

    protected ActionListener createActionListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getConfiguration().setModified(true);
                updatePreview();
            }
        };
    }

    @Override
    protected DocumentListener createDocumentListener() {
        return new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent e) {
                getConfiguration().setModified(true);
                updatePreview();
            }
        };
    }

    public Locale getSelectedLocale() {
        LocaleOption localeOption = localeComboBox.getSelectedValue();
        return localeOption == null ? null : localeOption.getLocale();
    }

    public void setSelectedLocale(Locale locale) {
        LocaleOption localeOption = LocaleOption.get(locale);
        localeComboBox.setSelectedValue(localeOption);
    }

    private void updatePreview() {
        if (isUpdating) return;
        isUpdating = true;
        try {
            Locale locale = getSelectedLocale();
            DBDateFormat dateFormat = dateFormatComboBox.getSelectedValue();
            DBNumberFormat numberFormat = numberFormatComboBox.getSelectedValue();
            boolean customSettings = customPatternsRadioButton.isSelected();
            Formatter formatter = null;
            if (customSettings) {
                try {
                    formatter = new Formatter(
                            locale,
                            customDateFormatTextField.getText(),
                            customTimeFormatTextField.getText(),
                            customNumberFormatTextField.getText());
                    errorLabel.setVisible(false);
                } catch (Exception e) {
                    errorLabel.setText("Invalid pattern: " + e.getMessage());
                    errorLabel.setIcon(Icons.STMT_EXECUTION_ERROR);
                    errorLabel.setVisible(true);
                }
            } else {
                formatter = new Formatter(locale, dateFormat, numberFormat);
                customNumberFormatTextField.setText(formatter.getNumberFormatPattern());
                customDateFormatTextField.setText(formatter.getDateFormatPattern());
                customTimeFormatTextField.setText(formatter.getTimeFormatPattern());
            }

            if (formatter != null) {
                datePreviewLabel.setText(formatter.formatDate(previewDate));
                timePreviewLabel.setText(formatter.formatTime(previewDate));
                numberPreviewLabel.setText(formatter.formatNumber(previewNumber));
                integerPreviewLabel.setText(formatter.formatInteger(previewNumber));
            }

            numberFormatComboBox.setEnabled(!customSettings);
            dateFormatComboBox.setEnabled(!customSettings);

            customNumberFormatTextField.setEnabled(customSettings);
            customDateFormatTextField.setEnabled(customSettings);
            customTimeFormatTextField.setEnabled(customSettings);
        } finally {
            isUpdating = false;
        }
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyFormChanges() throws ConfigurationException {
        RegionalSettings regionalSettings = getConfiguration();

        Locale locale = getSelectedLocale();
        regionalSettings.setLocale(locale);

        DBDateFormat dateFormat = dateFormatComboBox.getSelectedValue();
        regionalSettings.setDateFormatOption(dateFormat);

        DBNumberFormat numberFormat = numberFormatComboBox.getSelectedValue();
        regionalSettings.setNumberFormatOption(numberFormat);
        
        regionalSettings.getUseCustomFormats().applyChanges(customPatternsRadioButton);
        regionalSettings.getCustomDateFormat().applyChanges(customDateFormatTextField);
        regionalSettings.getCustomTimeFormat().applyChanges(customTimeFormatTextField);
        regionalSettings.getCustomNumberFormat().applyChanges(customNumberFormatTextField);
    }

    public void resetFormChanges() {
        RegionalSettings regionalSettings = getConfiguration();
        setSelectedLocale(regionalSettings.getLocale());

        Boolean useCustomFormats = regionalSettings.getUseCustomFormats().value();
        customPatternsRadioButton.setSelected(useCustomFormats);
        presetPatternsRadioButton.setSelected(!useCustomFormats);
        if (customPatternsRadioButton.isSelected()) {
            regionalSettings.getCustomDateFormat().resetChanges(customDateFormatTextField);
            regionalSettings.getCustomTimeFormat().resetChanges(customTimeFormatTextField);
            regionalSettings.getCustomNumberFormat().resetChanges(customNumberFormatTextField);
        }

        DBDateFormat dateFormat = regionalSettings.getDateFormatOption();
        dateFormatComboBox.setSelectedValue(dateFormat);

        DBNumberFormat numberFormat = regionalSettings.getNumberFormatOption();
        numberFormatComboBox.setSelectedValue(numberFormat);
    }
}
