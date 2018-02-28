package com.dci.intellij.dbn.menu.action;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.connection.DatabaseInterfaceProviderFactory;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;

public class MetaDataDefinitionReloadAction extends DumbAwareAction {

    public void actionPerformed(AnActionEvent e) {
        DatabaseInterfaceProviderFactory.reset();
    }

    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setVisible(DatabaseNavigator.getInstance().isDeveloperModeEnabled());
    }

}
