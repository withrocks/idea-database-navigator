package com.dci.intellij.dbn.browser;

import com.dci.intellij.dbn.browser.ui.BrowserToolWindowForm;
import com.dci.intellij.dbn.common.Icons;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentFactoryImpl;

public class DatabaseBrowserToolWindowFactory implements ToolWindowFactory, DumbAware{
    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        BrowserToolWindowForm toolWindowForm = DatabaseBrowserManager.getInstance(project).getToolWindowForm();
        ContentFactory contentFactory = new ContentFactoryImpl();
        Content content = contentFactory.createContent(toolWindowForm.getComponent(), null, false);
        toolWindow.getContentManager().addContent(content);
        toolWindow.setIcon(Icons.WINDOW_DATABASE_BROWSER);
    }
}
