package com.dci.intellij.dbn.menu.action;

import com.dci.intellij.dbn.common.about.ui.AboutComponent;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;

public class OpenAboutPageAction extends DumbAwareAction {

    public void actionPerformed(AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        if (project != null) {
            AboutComponent aboutComponent = new AboutComponent(project);
            aboutComponent.showPopup(project);
        }
    }
}
