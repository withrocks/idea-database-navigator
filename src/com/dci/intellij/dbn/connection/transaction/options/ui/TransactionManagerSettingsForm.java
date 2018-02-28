package com.dci.intellij.dbn.connection.transaction.options.ui;

import javax.swing.JPanel;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.ui.DBNComboBox;
import com.dci.intellij.dbn.connection.transaction.TransactionOption;
import com.dci.intellij.dbn.connection.transaction.options.TransactionManagerSettings;
import com.intellij.openapi.options.ConfigurationException;

public class TransactionManagerSettingsForm extends ConfigurationEditorForm<TransactionManagerSettings> {
    private JPanel mainPanel;
    private DBNComboBox<TransactionOption> uncommittedChangesOnProjectCloseComboBox;
    private DBNComboBox<TransactionOption> uncommittedChangesOnSwitchComboBox;
    private DBNComboBox<TransactionOption> uncommittedChangesOnDisconnectComboBox;
    private DBNComboBox<TransactionOption> multipleChangesOnCommitComboBox;
    private DBNComboBox<TransactionOption> multipleChangesOnRollbackComboBox;

    public TransactionManagerSettingsForm(TransactionManagerSettings settings) {
        super(settings);

        updateBorderTitleForeground(mainPanel);
        uncommittedChangesOnProjectCloseComboBox.setValues(
                TransactionOption.ASK,
                TransactionOption.COMMIT,
                TransactionOption.ROLLBACK,
                TransactionOption.REVIEW_CHANGES);

        uncommittedChangesOnSwitchComboBox.setValues(
                TransactionOption.ASK,
                TransactionOption.COMMIT,
                TransactionOption.ROLLBACK,
                TransactionOption.REVIEW_CHANGES);

        uncommittedChangesOnDisconnectComboBox.setValues(
                TransactionOption.ASK,
                TransactionOption.COMMIT,
                TransactionOption.ROLLBACK,
                TransactionOption.REVIEW_CHANGES);

        multipleChangesOnCommitComboBox.setValues(
                TransactionOption.ASK,
                TransactionOption.COMMIT,
                TransactionOption.REVIEW_CHANGES);

        multipleChangesOnRollbackComboBox.setValues(
                TransactionOption.ASK,
                TransactionOption.COMMIT,
                TransactionOption.REVIEW_CHANGES);

        resetFormChanges();
        registerComponent(mainPanel);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyFormChanges() throws ConfigurationException {
        TransactionManagerSettings settings = getConfiguration();
        settings.getCloseProjectOptionHandler().setSelectedOption(uncommittedChangesOnProjectCloseComboBox.getSelectedValue());
        settings.getToggleAutoCommitOptionHandler().setSelectedOption(uncommittedChangesOnSwitchComboBox.getSelectedValue());
        settings.getDisconnectOptionHandler().setSelectedOption(uncommittedChangesOnDisconnectComboBox.getSelectedValue());
        settings.getCommitMultipleChangesOptionHandler().setSelectedOption(multipleChangesOnCommitComboBox.getSelectedValue());
        settings.getRollbackMultipleChangesOptionHandler().setSelectedOption(multipleChangesOnRollbackComboBox.getSelectedValue());
    }

    public void resetFormChanges() {
        TransactionManagerSettings settings = getConfiguration();
        uncommittedChangesOnProjectCloseComboBox.setSelectedValue(settings.getCloseProjectOptionHandler().getSelectedOption());
        uncommittedChangesOnSwitchComboBox.setSelectedValue(settings.getToggleAutoCommitOptionHandler().getSelectedOption());
        uncommittedChangesOnDisconnectComboBox.setSelectedValue(settings.getDisconnectOptionHandler().getSelectedOption());
        multipleChangesOnCommitComboBox.setSelectedValue(settings.getCommitMultipleChangesOptionHandler().getSelectedOption());
        multipleChangesOnRollbackComboBox.setSelectedValue(settings.getRollbackMultipleChangesOptionHandler().getSelectedOption());

    }
}
