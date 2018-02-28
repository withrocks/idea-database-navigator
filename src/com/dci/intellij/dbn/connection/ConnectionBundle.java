package com.dci.intellij.dbn.connection;

import javax.swing.Icon;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.ui.DatabaseBrowserTree;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.list.FiltrableList;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;

public class ConnectionBundle implements BrowserTreeNode, Disposable {

    public static final Filter<ConnectionHandler> ACTIVE_CONNECTIONS_FILTER = new Filter<ConnectionHandler>() {
        public boolean accepts(ConnectionHandler connectionHandler) {
            return connectionHandler != null && connectionHandler.isActive();
        }
    };


    private Project project;
    private FiltrableList<ConnectionHandler> connectionHandlers = new FiltrableList<ConnectionHandler>(ACTIVE_CONNECTIONS_FILTER);
    private List<ConnectionHandler> virtualConnections = new ArrayList<ConnectionHandler>();

    public ConnectionBundle(Project project) {
        this.project = project;
        virtualConnections.add(new VirtualConnectionHandler(
                "virtual-oracle-connection",
                "Virtual - Oracle 10.1",
                DatabaseType.ORACLE,
                10.1,
                project));

        virtualConnections.add(new VirtualConnectionHandler(
                "virtual-mysql-connection",
                "Virtual - MySQL 5.0",
                DatabaseType.MYSQL,
                5.0,
                project));

        virtualConnections.add(new VirtualConnectionHandler(
                "virtual-postgres-connection",
                "Virtual - PostgreSQL 9.3.4",
                DatabaseType.POSTGRES,
                9.3,
                project));

        virtualConnections.add(new VirtualConnectionHandler(
                "virtual-iso92-sql-connection",
                "Virtual - ISO-92 SQL",
                DatabaseType.UNKNOWN,
                92,
                project));
    }

    public List<ConnectionHandler> getVirtualConnections() {
        return virtualConnections;
    }

    public ConnectionHandler getVirtualConnection(String id) {
        for (ConnectionHandler virtualConnection : virtualConnections) {
            if (virtualConnection.getId().equals(id)) {
                return virtualConnection;
            }
        }
        return null;
    }

    public Icon getIcon(int flags) {
        return Icons.PROJECT;
    }


    public Project getProject() {
        return project;
    }

    public boolean isDisposed() {
        return false;
    }

    public GenericDatabaseElement getUndisposedElement() {
        return this;
    }

    public DynamicContent getDynamicContent(DynamicContentType dynamicContentType) {
        return null;
    }

    public void addConnection(ConnectionHandler connectionHandler) {
        connectionHandlers.add(connectionHandler);
    }

    public void setConnectionHandlers(List<ConnectionHandler> connectionHandlers) {
        this.connectionHandlers = new FiltrableList<ConnectionHandler>(connectionHandlers, ACTIVE_CONNECTIONS_FILTER);
    }

    public boolean containsConnection(ConnectionHandler connectionHandler) {
        return connectionHandlers.contains(connectionHandler);
    }

    public ConnectionHandler getConnection(String id) {
        for (ConnectionHandler connectionHandler : connectionHandlers.getFullList()){
            if (connectionHandler.getId().equals(id)) return connectionHandler;
        }
        return null;
    }

    public FiltrableList<ConnectionHandler> getConnectionHandlers() {
        return connectionHandlers;
    }

    public List<ConnectionHandler> getAllConnectionHandlers() {
        return connectionHandlers.getFullList();
    }


    public void dispose() {
        DisposerUtil.dispose(connectionHandlers.getFullList());
        project = null;
    }

    /*********************************************************
    *                    NavigationItem                      *
    *********************************************************/
    public void navigate(boolean requestFocus) {}

    public boolean canNavigate() {
        return true;
    }

    public boolean canNavigateToSource() {
        return false;
    }

    /*********************************************************
    *                  ItemPresentation                      *
    *********************************************************/
    public String getName() {
        return getPresentableText();
    }

    public String getLocationString() {
        return null;
    }

    public ItemPresentation getPresentation() {
        return this;
    }

    public Icon getIcon(boolean open) {
        return getIcon(0);
    }

    /*********************************************************
    *                       TreeElement                     *
    *********************************************************/
    public boolean isTreeStructureLoaded() {
        return true;
    }

    public void initTreeElement() {}

    public boolean canExpand() {
        return true;
    }

    public BrowserTreeNode getTreeParent() {
        DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(project);
        DatabaseBrowserTree activeBrowserTree = browserManager.getActiveBrowserTree();
        return browserManager.isTabbedMode() ? null : activeBrowserTree == null ? null : activeBrowserTree.getModel().getRoot();
    }

    public List<? extends BrowserTreeNode> getTreeChildren() {
        return null;  //should never be used
    }

    public void refreshTreeChildren(@Nullable DBObjectType objectType) {
        for (ConnectionHandler connectionHandler : connectionHandlers) {
            connectionHandler.getObjectBundle().refreshTreeChildren(objectType);
        }
    }

    public void rebuildTreeChildren() {
        for (ConnectionHandler connectionHandler : connectionHandlers) {
            connectionHandler.getObjectBundle().rebuildTreeChildren();
        }
    }

    public BrowserTreeNode getTreeChild(int index) {
        return connectionHandlers.get(index).getObjectBundle();
    }

    public int getTreeChildCount() {
        return connectionHandlers.size();
    }

    public boolean isLeafTreeElement() {
        return connectionHandlers.size() == 0;
    }

    public int getIndexOfTreeChild(BrowserTreeNode child) {
        DBObjectBundle objectBundle = (DBObjectBundle) child;
        return connectionHandlers.indexOf(objectBundle.getConnectionHandler());
    }

    public int getTreeDepth() {
        return 1;
    }

    public String getPresentableText() {
        return "Database connections";
    }

    public String getPresentableTextDetails() {
        int size = connectionHandlers.size();
        return size == 0 ? "(no connections)" : "(" + size + ')';
    }

    public String getPresentableTextConditionalDetails() {
        return null;
    }

    @Nullable
    public ConnectionHandler getConnectionHandler() {
        return null;
    }

   /*********************************************************
    *                    ToolTipProvider                    *
    *********************************************************/
    public String getToolTip() {
        return "";
    }


    public boolean isEmpty() {
        return connectionHandlers.getFullList().isEmpty();
    }
}
