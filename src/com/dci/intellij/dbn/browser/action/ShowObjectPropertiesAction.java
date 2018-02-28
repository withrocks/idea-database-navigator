package com.dci.intellij.dbn.browser.action;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;

public class ShowObjectPropertiesAction extends ToggleAction implements DumbAware {
    public ShowObjectPropertiesAction() {
        super("Show properties", null, Icons.BROWSER_OBJECT_PROPERTIES);
    }

    @Override
    public boolean isSelected(AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        return project != null &&
                DatabaseBrowserManager.getInstance(project).getShowObjectProperties().value();
    }

    @Override
    public void setSelected(AnActionEvent e, boolean state) {
        Project project = ActionUtil.getProject(e);
        DatabaseBrowserManager.getInstance(project).showObjectProperties(state);
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        e.getPresentation().setText(isSelected(e) ? "Hide Object Properties" : "Show Object Properties");
    }
}
