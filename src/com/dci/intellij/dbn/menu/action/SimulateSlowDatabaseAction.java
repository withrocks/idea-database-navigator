package com.dci.intellij.dbn.menu.action;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;

public class SimulateSlowDatabaseAction extends ToggleAction {
    @Override
    public boolean isSelected(AnActionEvent e) {
        return DatabaseNavigator.getInstance().isSlowDatabaseModeEnabled();
    }

    @Override
    public void setSelected(AnActionEvent e, boolean state) {
        DatabaseNavigator.getInstance().setSlowDatabaseModeEnabled(state);
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        e.getPresentation().setVisible(DatabaseNavigator.getInstance().isDeveloperModeEnabled());
    }
}
