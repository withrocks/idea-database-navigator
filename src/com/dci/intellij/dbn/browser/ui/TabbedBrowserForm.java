package com.dci.intellij.dbn.browser.ui;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.common.environment.EnvironmentType;
import com.dci.intellij.dbn.common.environment.options.EnvironmentVisibilitySettings;
import com.dci.intellij.dbn.common.environment.options.listener.EnvironmentChangeListener;
import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.ui.tab.TabbedPane;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBList;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.TabsListener;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;

public class TabbedBrowserForm extends DatabaseBrowserForm{
    private TabbedPane connectionTabs;
    private JPanel mainPanel;

    public TabbedBrowserForm(BrowserToolWindowForm parentComponent) {
        super(parentComponent);
        connectionTabs = new TabbedPane(this);
        //connectionTabs.setBackground(GUIUtil.getListBackground());
        //mainPanel.add(connectionTabs, BorderLayout.CENTER);
        initTabs();
        connectionTabs.addListener(new TabsListener() {
            public void selectionChanged(TabInfo oldSelection, TabInfo newSelection) {
/*
                ToolWindow toolWindow = browserManager.getBrowserToolWindow();
                if (toolWindow.isVisible()) {
                    toolWindow.activate(null);
                }
*/
            }

            public void beforeSelectionChanged(TabInfo oldSelection, TabInfo newSelection) {}
            public void tabRemoved(TabInfo tabInfo) {}
            public void tabsMoved() {}
        });

        EventManager.subscribe(getProject(), EnvironmentChangeListener.TOPIC, environmentChangeListener);

        Disposer.register(this, connectionTabs);
    }


    private void initTabs() {
        Project project = getProject();
        connectionTabs.dispose();
        connectionTabs = new TabbedPane(this);
        ConnectionManager connectionManager = ConnectionManager.getInstance(project);
        ConnectionBundle connectionBundle = connectionManager.getConnectionBundle();
        for (ConnectionHandler connectionHandler: connectionBundle.getConnectionHandlers()) {
            SimpleBrowserForm browserForm = new SimpleBrowserForm(this, connectionHandler);
            JComponent component = browserForm.getComponent();
            TabInfo tabInfo = new TabInfo(component);
            tabInfo.setText(CommonUtil.nvl(connectionHandler.getName(), "[unnamed connection]"));
            tabInfo.setObject(browserForm);
            //tabInfo.setIcon(connectionHandler.getIcon());
            connectionTabs.addTab(tabInfo);

            EnvironmentType environmentType = connectionHandler.getEnvironmentType();
            tabInfo.setTabColor(environmentType.getColor());
        }
        if (connectionTabs.getTabCount() == 0) {
            mainPanel.removeAll();
            mainPanel.add(new JBList(new ArrayList()), BorderLayout.CENTER);
        } else {
            if (mainPanel.getComponentCount() > 0) {
                Component component = mainPanel.getComponent(0);
                if (component != connectionTabs) {
                    mainPanel.removeAll();
                    mainPanel.add(connectionTabs, BorderLayout.CENTER);
                }
            } else {
                mainPanel.add(connectionTabs, BorderLayout.CENTER);
            }
        }

    }

    @Nullable
    private SimpleBrowserForm getBrowserForm(ConnectionHandler connectionHandler) {
        for (TabInfo tabInfo : connectionTabs.getTabs()) {
            SimpleBrowserForm browserForm = (SimpleBrowserForm) tabInfo.getObject();
            if (browserForm.getConnectionHandler() == connectionHandler) {
                return browserForm;
            }
        }
        return null;
    }

    public JComponent getComponent() {
        return mainPanel;
    }

    @Nullable
    public DatabaseBrowserTree getBrowserTree() {
        return getActiveBrowserTree();
    }

    @Nullable
    public DatabaseBrowserTree getBrowserTree(ConnectionHandler connectionHandler) {
        SimpleBrowserForm browserForm = getBrowserForm(connectionHandler);
        return browserForm== null ? null : browserForm.getBrowserTree();
    }

    @Nullable
    public DatabaseBrowserTree getActiveBrowserTree() {
        TabInfo tabInfo = connectionTabs.getSelectedInfo();
        if (tabInfo != null) {
            SimpleBrowserForm browserForm = (SimpleBrowserForm) tabInfo.getObject();
            return browserForm.getBrowserTree();
        }
        return null;
    }

    public void selectElement(BrowserTreeNode treeNode, boolean requestFocus) {
        ConnectionHandler connectionHandler = treeNode.getConnectionHandler();
        SimpleBrowserForm browserForm = getBrowserForm(connectionHandler);
        if (browserForm != null) {
            connectionTabs.select(browserForm.getComponent(), requestFocus);
            browserForm.selectElement(treeNode, requestFocus);
        }
    }

    public void rebuildTree() {
        for (TabInfo tabInfo : connectionTabs.getTabs()) {
            SimpleBrowserForm browserForm = (SimpleBrowserForm) tabInfo.getObject();
            browserForm.rebuildTree();
        }
    }

    public void rebuild() {
        initTabs();
    }

    public void dispose() {
        EventManager.unsubscribe(environmentChangeListener);
        super.dispose();
    }


    /********************************************************
     *                       Listeners                      *
     ********************************************************/
    private EnvironmentChangeListener environmentChangeListener = new EnvironmentChangeListener() {
        @Override
        public void configurationChanged() {
            Project project = getProject();
            EnvironmentVisibilitySettings visibilitySettings = getEnvironmentSettings(project).getVisibilitySettings();
            for (TabInfo tabInfo : connectionTabs.getTabs()) {
                SimpleBrowserForm browserForm = (SimpleBrowserForm) tabInfo.getObject();
                ConnectionHandler connectionHandler = browserForm.getConnectionHandler();
                JBColor environmentColor = connectionHandler.getEnvironmentType().getColor();
                if (visibilitySettings.getConnectionTabs().value()) {
                    tabInfo.setTabColor(environmentColor);
                } else {
                    tabInfo.setTabColor(null);
                }
            }
        }
    };
}

