package com.dci.intellij.dbn.connection.config;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import java.awt.Component;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.connection.ConnectivityStatus;
import com.dci.intellij.dbn.connection.config.ui.GenericDatabaseSettingsForm;

public class ConnectionConfigListCellRenderer extends DefaultListCellRenderer{
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        ConnectionSettings connectionSettings = (ConnectionSettings) value;
        ConnectionDatabaseSettings databaseSettings = connectionSettings.getDatabaseSettings();
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus );
        GenericDatabaseSettingsForm settingsEditor = databaseSettings.getSettingsEditor();
        String name = settingsEditor == null ?
                databaseSettings.getName() :
                settingsEditor.getConnectionName();

        ConnectivityStatus connectivityStatus = settingsEditor == null ?
                databaseSettings.getConnectivityStatus() :
                settingsEditor.getConnectivityStatus();

        boolean isActive = settingsEditor == null ?
                databaseSettings.isActive() :
                settingsEditor.isConnectionActive();

        Icon icon = Icons.CONNECTION_DISABLED;
        boolean isNew = connectionSettings.isNew();

        if (isNew) {
            icon = connectivityStatus == ConnectivityStatus.VALID ? Icons.CONNECTION_ACTIVE_NEW : Icons.CONNECTION_NEW;
        } else if (isActive) {
            icon = connectivityStatus == ConnectivityStatus.VALID ? Icons.CONNECTION_ACTIVE :
                   connectivityStatus == ConnectivityStatus.INVALID ? Icons.CONNECTION_INVALID : Icons.CONNECTION_INACTIVE;
        }

        label.setIcon(icon);
        label.setText(name);
/*        if (!cellHasFocus && isSelected) {
            label.setForeground(list.getForeground());
            label.setBackground(list.hasFocus() ? list.getBackground() : UIUtil.getFocusedFillColor());
            label.setBorder(new DottedBorder(Color.BLACK));
        }*/
        return label;
    }
}
