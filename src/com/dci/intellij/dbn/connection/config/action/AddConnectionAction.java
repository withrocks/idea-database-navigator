package com.dci.intellij.dbn.connection.config.action;

import javax.swing.JList;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.connection.config.ConnectionBundleSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.dci.intellij.dbn.connection.config.GenericConnectionDatabaseSettings;
import com.dci.intellij.dbn.connection.config.ui.ConnectionListModel;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;

public class AddConnectionAction extends DumbAwareAction {
    protected ConnectionBundleSettings connectionBundleSettings;
    protected JList list;

    public AddConnectionAction(JList list, ConnectionBundleSettings connectionBundleSettings) {
        super("Add connection", null, Icons.ACTION_ADD);
        this.connectionBundleSettings = connectionBundleSettings;
        this.list = list;
    }

    public void actionPerformed(AnActionEvent anActionEvent) {
        connectionBundleSettings.setModified(true);
        ConnectionSettings connectionSettings = new ConnectionSettings(connectionBundleSettings);
        connectionSettings.setNew(true);
        connectionSettings.generateNewId();

        String name = "Connection";
        ConnectionListModel model = (ConnectionListModel) list.getModel();
        while (model.getConnectionConfig(name) != null) {
            name = NamingUtil.getNextNumberedName(name, true);
        }
        GenericConnectionDatabaseSettings connectionConfig = (GenericConnectionDatabaseSettings) connectionSettings.getDatabaseSettings();
        connectionConfig.setName(name);
        int selectedIndex = list.getSelectedIndex() + 1;
        model.add(selectedIndex, connectionSettings);
        list.setSelectedIndex(selectedIndex);
    }
}
