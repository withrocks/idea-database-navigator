package com.dci.intellij.dbn.connection.config.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.connection.config.ConnectionBundleSettings;
import com.dci.intellij.dbn.connection.config.ui.ConnectionListModel;
import com.dci.intellij.dbn.data.sorting.SortDirection;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;

import javax.swing.Icon;
import javax.swing.JList;

public class SortConnectionsAction extends DumbAwareAction {
    private SortDirection currentSortDirection = SortDirection.ASCENDING;
    private ConnectionBundleSettings connectionBundleSettings;
    private JList list;

    public SortConnectionsAction(JList list, ConnectionBundleSettings connectionBundleSettings) {
        this.list = list;
        this.connectionBundleSettings = connectionBundleSettings;
    }

    public void actionPerformed(AnActionEvent e) {
        currentSortDirection = currentSortDirection == SortDirection.ASCENDING ?
                SortDirection.DESCENDING :
                SortDirection.ASCENDING;

        if (list.getModel().getSize() > 0) {
            Object selectedValue = list.getSelectedValue();
            connectionBundleSettings.setModified(true);
            ConnectionListModel model = (ConnectionListModel) list.getModel();
            model.sort(currentSortDirection);
            list.setSelectedValue(selectedValue, true);
        }
    }

    public void update(AnActionEvent e) {
        Icon icon;
        String text;
        if (currentSortDirection != SortDirection.ASCENDING) {
            icon = Icons.ACTION_SORT_ASC;
            text = "Sort list ascending";
        } else {
            icon = Icons.ACTION_SORT_DESC;
            text = "Sort list descending";
        }
        Presentation presentation = e.getPresentation();
        presentation.setIcon(icon);
        presentation.setText(text);
    }
}
