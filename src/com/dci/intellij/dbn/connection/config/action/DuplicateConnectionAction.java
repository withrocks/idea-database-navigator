package com.dci.intellij.dbn.connection.config.action;

import javax.swing.JList;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.connection.config.ConnectionBundleSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.dci.intellij.dbn.connection.config.ui.ConnectionListModel;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;

public class DuplicateConnectionAction extends DumbAwareAction {
    protected ConnectionBundleSettings connectionBundleSettings;
    protected JList list;

    public DuplicateConnectionAction(JList list, ConnectionBundleSettings connectionBundleSettings) {
        super("Duplicate connection", null, Icons.ACTION_COPY);
        this.list = list;
        this.connectionBundleSettings = connectionBundleSettings;
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        connectionBundleSettings.setModified(true);
        ConnectionSettings connectionSettings = (ConnectionSettings) list.getSelectedValue();
        ConnectionListModel model = (ConnectionListModel) list.getModel();
        ConnectionSettings clone = connectionSettings.clone();
        clone.setNew(true);
        String name = clone.getDatabaseSettings().getName();
        while (model.getConnectionConfig(name) != null) {
            name = NamingUtil.getNextNumberedName(name, true);
        }
        clone.getDatabaseSettings().setName(name);
        int selectedIndex = list.getSelectedIndex() + 1;
        model.add(selectedIndex, clone);
        list.setSelectedIndex(selectedIndex);
    }

    public void update(@NotNull AnActionEvent e) {
        int length = list.getSelectedValues().length;
        e.getPresentation().setEnabled(length == 1);
    }
}
