package com.dci.intellij.dbn.data.find.action;


import com.dci.intellij.dbn.data.find.DataSearchComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.actionSystem.Shortcut;

import javax.swing.JComponent;
import java.util.Set;

public abstract class DataSearchHeaderAction extends AnAction {
    private DataSearchComponent searchComponent;

    protected static void registerShortcutsToComponent(Set<Shortcut> shortcuts, AnAction action, JComponent component) {
        CustomShortcutSet shortcutSet = new CustomShortcutSet(shortcuts.toArray(new Shortcut[shortcuts.size()]));
        action.registerCustomShortcutSet(shortcutSet, component);
    }

    public DataSearchComponent getSearchComponent() {
        return searchComponent;
    }

    protected DataSearchHeaderAction(DataSearchComponent searchComponent) {
        this.searchComponent = searchComponent;
    }
}

