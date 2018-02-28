package com.dci.intellij.dbn.browser.action;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;

public class AutoscrollToEditorAction extends ToggleAction implements DumbAware{

    public AutoscrollToEditorAction() {
        super("Autoscroll to editor", "", Icons.BROWSER_AUTOSCROLL_TO_EDITOR);
    }


    public boolean isSelected(AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        if (project != null) {
            DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(project);
            return browserManager.getAutoscrollToEditor().value();
        }
        return false;
    }

    public void setSelected(AnActionEvent e, boolean state) {
        Project project = ActionUtil.getProject(e);
        DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(project);
        browserManager.getAutoscrollToEditor().setValue(state);
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        e.getPresentation().setText("Autoscroll to Editor");
    }

}
