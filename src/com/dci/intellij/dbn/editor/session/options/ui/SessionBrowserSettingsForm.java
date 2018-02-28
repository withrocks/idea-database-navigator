package com.dci.intellij.dbn.editor.session.options.ui;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.ui.DBNComboBox;
import com.dci.intellij.dbn.editor.session.options.SessionBrowserSettings;
import com.dci.intellij.dbn.editor.session.options.SessionInterruptionOption;
import com.intellij.openapi.options.ConfigurationException;

public class SessionBrowserSettingsForm extends ConfigurationEditorForm<SessionBrowserSettings> {
    private JPanel mainPanel;
    private DBNComboBox<SessionInterruptionOption> disconnectSessionComboBox;
    private DBNComboBox<SessionInterruptionOption> killSessionComboBox;
    private JCheckBox reloadOnFilterChangeCheckBox;

    public SessionBrowserSettingsForm(SessionBrowserSettings settings) {
        super(settings);

        updateBorderTitleForeground(mainPanel);
        disconnectSessionComboBox.setValues(
                SessionInterruptionOption.ASK,
                SessionInterruptionOption.IMMEDIATE,
                SessionInterruptionOption.POST_TRANSACTION);

        killSessionComboBox.setValues(
                SessionInterruptionOption.ASK,
                SessionInterruptionOption.NORMAL,
                SessionInterruptionOption.IMMEDIATE);

        resetFormChanges();
        registerComponent(mainPanel);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyFormChanges() throws ConfigurationException {
        SessionBrowserSettings settings = getConfiguration();
        settings.getDisconnectSessionOptionHandler().setSelectedOption(disconnectSessionComboBox.getSelectedValue());
        settings.getKillSessionOptionHandler().setSelectedOption(killSessionComboBox.getSelectedValue());
        settings.setReloadOnFilterChange(reloadOnFilterChangeCheckBox.isSelected());
    }

    public void resetFormChanges() {
        SessionBrowserSettings settings = getConfiguration();
        disconnectSessionComboBox.setSelectedValue(settings.getDisconnectSessionOptionHandler().getSelectedOption());
        killSessionComboBox.setSelectedValue(settings.getKillSessionOptionHandler().getSelectedOption());
        reloadOnFilterChangeCheckBox.setSelected(settings.isReloadOnFilterChange());
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
