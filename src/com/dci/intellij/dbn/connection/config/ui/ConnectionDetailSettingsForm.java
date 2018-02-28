package com.dci.intellij.dbn.connection.config.ui;

import com.dci.intellij.dbn.common.environment.EnvironmentType;
import com.dci.intellij.dbn.common.environment.EnvironmentTypeBundle;
import com.dci.intellij.dbn.common.environment.options.EnvironmentSettings;
import com.dci.intellij.dbn.common.environment.options.listener.EnvironmentChangeListener;
import com.dci.intellij.dbn.common.environment.options.listener.EnvironmentConfigLocalListener;
import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.options.SettingsChangeNotifier;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorUtil;
import com.dci.intellij.dbn.common.properties.ui.PropertiesEditorForm;
import com.dci.intellij.dbn.common.ui.DBNComboBox;
import com.dci.intellij.dbn.common.ui.DBNHintForm;
import com.dci.intellij.dbn.common.ui.ValueSelectorListener;
import com.dci.intellij.dbn.connection.ConnectionStatusListener;
import com.dci.intellij.dbn.connection.config.ConnectionDetailSettings;
import com.dci.intellij.dbn.options.general.GeneralProjectSettings;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectionDetailSettingsForm extends ConfigurationEditorForm<ConnectionDetailSettings>{
    private JPanel mainPanel;
    private DBNComboBox<CharsetOption> encodingComboBox;
    private JCheckBox autoCommitCheckBox;
    private JPanel propertiesPanel;
    private DBNComboBox<EnvironmentType> environmentTypesComboBox;
    private JPanel generalGroupPanel;
    private JPanel propertiesGroupPanel;
    private JTextField maxPoolSizeTextField;
    private JTextField idleTimeTextField;
    private JCheckBox ddlFileBindingCheckBox;
    private JTextField alternativeStatementDelimiterTextField;
    private JCheckBox autoConnectCheckBox;
    private JPanel autoConnectHintPanel;
    private JCheckBox databaseLoggingCheckBox;

    private PropertiesEditorForm propertiesEditorForm;

    public ConnectionDetailSettingsForm(final ConnectionDetailSettings configuration) {
        super(configuration);

        Map<String, String> properties = new HashMap<String, String>();
        properties.putAll(configuration.getProperties());
        updateBorderTitleForeground(generalGroupPanel);
        updateBorderTitleForeground(propertiesGroupPanel);

        propertiesEditorForm = new PropertiesEditorForm(this, properties, true);
        propertiesPanel.add(propertiesEditorForm.getComponent(), BorderLayout.CENTER);

        encodingComboBox.setValues(CharsetOption.ALL);

        List<EnvironmentType> environmentTypes = new ArrayList<EnvironmentType>(getEnvironmentTypes());
        environmentTypes.add(0, EnvironmentType.DEFAULT);
        environmentTypesComboBox.setValues(environmentTypes);
        resetFormChanges();

        registerComponent(mainPanel);

        environmentTypesComboBox.addListener(new ValueSelectorListener<EnvironmentType>() {
            @Override
            public void valueSelected(EnvironmentType value) {
                notifyPresentationChanges();
            }
        });

        String autoConnectHintText = "NOTE: If \"Connect automatically\" is not selected, the system will not restore the entire workspace the next time you open the project (i.e. all open editors for this connection will not be reopened automatically).";
        DBNHintForm autoConnectHintForm = new DBNHintForm(autoConnectHintText);
        autoConnectHintPanel.add(autoConnectHintForm.getComponent());

        boolean visibleHint = !autoConnectCheckBox.isSelected();
        autoConnectHintPanel.setVisible(visibleHint);


        EventManager.subscribe(configuration.getProject(), EnvironmentConfigLocalListener.TOPIC, presentationChangeListener);
    }

    public void notifyPresentationChanges() {
        Project project = getConfiguration().getProject();
        ConnectionPresentationChangeListener listener = EventManager.notify(project, ConnectionPresentationChangeListener.TOPIC);
        EnvironmentType environmentType = environmentTypesComboBox.getSelectedValue();
        Color color = environmentType == null ? null : environmentType.getColor();
        listener.presentationChanged(null, null, color, getConfiguration().getConnectionId(), null);
    }

    protected ActionListener createActionListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Object source = e.getSource();
                if (source == autoConnectCheckBox){
                    boolean visibleHint = !autoConnectCheckBox.isSelected();
                    autoConnectHintPanel.setVisible(visibleHint);
                }
                getConfiguration().setModified(true);
            }
        };
    }

    private List<EnvironmentType> getEnvironmentTypes() {
        Project project = getConfiguration().getProject();
        EnvironmentSettings environmentSettings = GeneralProjectSettings.getInstance(project).getEnvironmentSettings();
        return environmentSettings.getEnvironmentTypes().getEnvironmentTypes();
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    @Override
    public void applyFormChanges() throws ConfigurationException {
        final ConnectionDetailSettings configuration = getConfiguration();

        Map<String, String> newProperties = propertiesEditorForm.getProperties();
        Charset newCharset = encodingComboBox.getSelectedValue().getCharset();
        boolean newAutoCommit = autoCommitCheckBox.isSelected();
        boolean newDdlFileBinding = ddlFileBindingCheckBox.isSelected();
        boolean newDatabaseLogging = databaseLoggingCheckBox.isSelected();
        EnvironmentType newEnvironmentType = environmentTypesComboBox.getSelectedValue();
        final String newEnvironmentTypeId = newEnvironmentType.getId();

        final boolean settingsChanged =
                !configuration.getProperties().equals(newProperties) ||
                !configuration.getCharset().equals(newCharset) ||
                configuration.isEnableAutoCommit() != newAutoCommit ||
                configuration.isEnableDdlFileBinding() != newDdlFileBinding ||
                configuration.isEnableDatabaseLogging() != newDatabaseLogging;

        final boolean environmentChanged =
                !configuration.getEnvironmentType().getId().equals(newEnvironmentTypeId);


        configuration.setEnvironmentTypeId(newEnvironmentTypeId);
        configuration.setProperties(newProperties);
        configuration.setCharset(newCharset);
        configuration.setEnableAutoCommit(newAutoCommit);
        configuration.setConnectAutomatically(autoConnectCheckBox.isSelected());
        configuration.setEnableDdlFileBinding(newDdlFileBinding);
        configuration.setEnableDatabaseLogging(newDatabaseLogging);
        configuration.setAlternativeStatementDelimiter(alternativeStatementDelimiterTextField.getText());
        int idleTimeToDisconnect = ConfigurationEditorUtil.validateIntegerInputValue(idleTimeTextField, "Idle Time to Disconnect (minutes)", 0, 60, "");
        int maxPoolSize = ConfigurationEditorUtil.validateIntegerInputValue(maxPoolSizeTextField, "Max Connection Pool Size", 3, 20, "");
        configuration.setIdleTimeToDisconnect(idleTimeToDisconnect);
        configuration.setMaxConnectionPoolSize(maxPoolSize);

        new SettingsChangeNotifier() {
            @Override
            public void notifyChanges() {
                Project project = configuration.getProject();
                if (environmentChanged) {
                    EnvironmentChangeListener listener = EventManager.notify(project, EnvironmentChangeListener.TOPIC);
                    listener.configurationChanged();
                }

                if (settingsChanged) {
                    ConnectionStatusListener listener = EventManager.notify(project, ConnectionStatusListener.TOPIC);
                    listener.statusChanged(configuration.getConnectionId());
                }
            }
        };
    }

    @Override
    public void resetFormChanges() {
        ConnectionDetailSettings configuration = getConfiguration();
        encodingComboBox.setSelectedValue(CharsetOption.get(configuration.getCharset()));
        propertiesEditorForm.setProperties(configuration.getProperties());
        autoCommitCheckBox.setSelected(configuration.isEnableAutoCommit());
        ddlFileBindingCheckBox.setSelected(configuration.isEnableDdlFileBinding());
        databaseLoggingCheckBox.setSelected(configuration.isEnableDatabaseLogging());
        autoConnectCheckBox.setSelected(configuration.isConnectAutomatically());
        environmentTypesComboBox.setSelectedValue(configuration.getEnvironmentType());
        idleTimeTextField.setText(Integer.toString(configuration.getIdleTimeToDisconnect()));
        maxPoolSizeTextField.setText(Integer.toString(configuration.getMaxConnectionPoolSize()));
        alternativeStatementDelimiterTextField.setText(configuration.getAlternativeStatementDelimiter());
    }

    private EnvironmentConfigLocalListener presentationChangeListener = new EnvironmentConfigLocalListener() {
        @Override
        public void settingsChanged(EnvironmentTypeBundle environmentTypes) {
            EnvironmentType selectedItem = environmentTypesComboBox.getSelectedValue();
            String selectedId = selectedItem == null ? EnvironmentType.DEFAULT.getId() : selectedItem.getId();
            selectedItem = environmentTypes.getEnvironmentType(selectedId);

            environmentTypesComboBox.setValues(environmentTypes.getEnvironmentTypes());
            environmentTypesComboBox.setSelectedValue(selectedItem);
            notifyPresentationChanges();
        }
    };

    @Override
    public void dispose() {
        EventManager.unsubscribe(presentationChangeListener);
        super.dispose();
    }
}
