package com.dci.intellij.dbn.execution.common.ui;

import com.intellij.openapi.actionSystem.*;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.impl.TabLabel;

public class ExecutionConsolePopupActionGroup extends DefaultActionGroup {
    private ExecutionConsoleForm executionConsoleForm;

    public ExecutionConsolePopupActionGroup(ExecutionConsoleForm executionConsoleForm) {
        this.executionConsoleForm = executionConsoleForm;
        add(close);
        add(closeAll);
        add(closeAllButThis);
    }

    private static TabInfo getTabInfo(AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        Object o = dataContext.getData(PlatformDataKeys.CONTEXT_COMPONENT.getName());
        if (o instanceof TabLabel) {
            TabLabel tabLabel = (TabLabel) o;
            return tabLabel.getInfo();
        }
        return null;
    }

    private AnAction close = new AnAction("Close") {
        @Override
        public void actionPerformed(AnActionEvent e) {
            TabInfo tabInfo = getTabInfo(e);
            if (tabInfo != null) {
                executionConsoleForm.removeTab(tabInfo);
            }
        }
    };

    private AnAction closeAll = new AnAction("Close All") {
        public void actionPerformed(AnActionEvent e) {
            executionConsoleForm.removeAllTabs();
        }
    };

    private AnAction closeAllButThis = new AnAction("Close All But This") {
        public void actionPerformed(AnActionEvent e) {
            TabInfo tabInfo = getTabInfo(e);
            if (tabInfo != null) {
                executionConsoleForm.removeAllExceptTab(tabInfo);
            }
        }
    };
}
