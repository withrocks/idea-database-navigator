package com.dci.intellij.dbn.navigation.options.ui;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.ui.DBNComboBox;
import com.dci.intellij.dbn.common.ui.KeyUtil;
import com.dci.intellij.dbn.common.ui.Presentable;
import com.dci.intellij.dbn.common.ui.list.CheckBoxList;
import com.dci.intellij.dbn.navigation.options.ObjectsLookupSettings;
import com.intellij.openapi.actionSystem.Shortcut;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.options.ConfigurationException;

public class ObjectsLookupSettingsForm extends ConfigurationEditorForm<ObjectsLookupSettings> {
    private JPanel mainPanel;
    private JScrollPane lookupObjectsScrollPane;
    private DBNComboBox<ConnectionOption> connectionComboBox;
    private DBNComboBox<BehaviorOption> behaviorComboBox;
    private CheckBoxList lookupObjectsList;

    public ObjectsLookupSettingsForm(ObjectsLookupSettings configuration) {
        super(configuration);
        Shortcut[] shortcuts = KeyUtil.getShortcuts("DBNavigator.Actions.Navigation.GotoDatabaseObject");
        TitledBorder border = (TitledBorder) mainPanel.getBorder();
        border.setTitle("Lookup Objects (" + KeymapUtil.getShortcutsText(shortcuts) + ")");
        updateBorderTitleForeground(mainPanel);

        connectionComboBox.setValues(
                ConnectionOption.PROMPT,
                ConnectionOption.RECENT);

        behaviorComboBox.setValues(
                BehaviorOption.LOOKUP,
                BehaviorOption.LOAD);

        lookupObjectsList = new CheckBoxList(configuration.getLookupObjectTypes());
        lookupObjectsScrollPane.setViewportView(lookupObjectsList);

        resetFormChanges();
        registerComponents(mainPanel);
    }

    @Override
    public void applyFormChanges() throws ConfigurationException {
        lookupObjectsList.applyChanges();
        ObjectsLookupSettings configuration = getConfiguration();
        configuration.getForceDatabaseLoad().setValue(behaviorComboBox.getSelectedValue().getValue());
        configuration.getPromptConnectionSelection().setValue(connectionComboBox.getSelectedValue().getValue());
    }

    @Override
    public void resetFormChanges() {
        ObjectsLookupSettings configuration = getConfiguration();
        if (configuration.getForceDatabaseLoad().getValue())
            behaviorComboBox.setSelectedValue(BehaviorOption.LOAD); else
            behaviorComboBox.setSelectedValue(BehaviorOption.LOOKUP);

        if (configuration.getPromptConnectionSelection().getValue())
            connectionComboBox.setSelectedValue(ConnectionOption.PROMPT); else
            connectionComboBox.setSelectedValue(ConnectionOption.RECENT);
    }

    public JComponent getComponent() {
        return mainPanel;
    }

    private enum ConnectionOption implements Presentable {
        PROMPT("Prompt connection selection", true),
        RECENT("Select most recently used connection", false);

        private String name;
        private boolean value;

        ConnectionOption(String name, boolean value) {
            this.name = name;
            this.value = value;
        }

        @NotNull
        @Override
        public String getName() {
            return name;
        }

        @Nullable
        @Override
        public Icon getIcon() {
            return null;
        }

        public boolean getValue() {
            return value;
        }
    }

    private enum BehaviorOption implements Presentable {
        LOOKUP("Lookup loaded objects only", false),
        LOAD("Force database load (slow)", true);

        private String name;
        private boolean value;

        BehaviorOption(String name, boolean value) {
            this.name = name;
            this.value = value;
        }

        @NotNull
        @Override
        public String getName() {
            return name;
        }

        @Nullable
        @Override
        public Icon getIcon() {
            return null;
        }

        public boolean getValue() {
            return value;
        }
    }
}
