package com.dci.intellij.dbn.object.filter.name.action;

import com.dci.intellij.dbn.object.filter.name.ui.ObjectNameFilterSettingsForm;
import com.intellij.openapi.actionSystem.AnAction;

import javax.swing.*;
import javax.swing.tree.TreePath;

public abstract class ObjectNameFilterAction extends AnAction {
    protected ObjectNameFilterSettingsForm settingsForm;

    protected ObjectNameFilterAction(String text, Icon icon, ObjectNameFilterSettingsForm settingsForm) {
        super(text, null, icon);
        this.settingsForm = settingsForm;
    }

    protected Object getSelection() {
        TreePath selectionPath = getFiltersTree().getSelectionPath();
        return selectionPath == null ? null : selectionPath.getLastPathComponent();
    }

    protected JTree getFiltersTree() {
        return settingsForm.getFiltersTree();
    }
}
