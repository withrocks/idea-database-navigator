package com.dci.intellij.dbn.browser;

import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.browser.model.BrowserTreeModel;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.model.TabbedBrowserTreeModel;
import com.dci.intellij.dbn.browser.options.BrowserDisplayMode;
import com.dci.intellij.dbn.browser.options.DatabaseBrowserSettings;
import com.dci.intellij.dbn.browser.options.ObjectFilterChangeListener;
import com.dci.intellij.dbn.browser.ui.BrowserToolWindowForm;
import com.dci.intellij.dbn.browser.ui.DatabaseBrowserTree;
import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.options.setting.BooleanSetting;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.ConditionalLaterInvocator;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.ConnectionManagerListener;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.vfs.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.DBVirtualFile;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerAdapter;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;

@State(
    name = "DBNavigator.Project.DatabaseBrowserManager",
    storages = {
        @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/dbnavigator.xml", scheme = StorageScheme.DIRECTORY_BASED),
        @Storage(file = StoragePathMacros.PROJECT_FILE)}
)
public class DatabaseBrowserManager extends AbstractProjectComponent implements PersistentStateComponent<Element> {
    public static final String TOOL_WINDOW_ID = "DB Browser";

    private BooleanSetting autoscrollFromEditor = new BooleanSetting("autoscroll-from-editor", true);
    private BooleanSetting autoscrollToEditor   = new BooleanSetting("autoscroll-to-editor", false);
    private BooleanSetting showObjectProperties = new BooleanSetting("show-object-properties", true);
    public static final ThreadLocal<Boolean> AUTOSCROLL_FROM_EDITOR = new ThreadLocal<Boolean>();
    private BrowserToolWindowForm toolWindowForm;

    private DatabaseBrowserManager(Project project) {
        super(project);
    }

    @Nullable
    public DatabaseBrowserTree getActiveBrowserTree() {
        return getToolWindowForm().getActiveBrowserTree();
    }

    @Nullable
    public ConnectionHandler getActiveConnection() {
        DatabaseBrowserTree activeBrowserTree = getActiveBrowserTree();
        if (activeBrowserTree != null) {
            BrowserTreeModel browserTreeModel = activeBrowserTree.getModel();
            if (browserTreeModel instanceof TabbedBrowserTreeModel) {
                TabbedBrowserTreeModel tabbedBrowserTreeModel = (TabbedBrowserTreeModel) browserTreeModel;
                return tabbedBrowserTreeModel.getConnectionHandler();
            }

            BrowserTreeNode browserTreeNode = activeBrowserTree.getSelectedNode();
            if (browserTreeNode != null) {
                return browserTreeNode.getConnectionHandler();
            }
        }

        return null;
    }

    @NotNull
    public ToolWindow getBrowserToolWindow() {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(getProject());
        return toolWindowManager.getToolWindow(TOOL_WINDOW_ID);
    }

    public synchronized BrowserToolWindowForm getToolWindowForm() {
        if (toolWindowForm == null) {
            toolWindowForm = new BrowserToolWindowForm(getProject());
        }
        return toolWindowForm;
    }

    public BooleanSetting getAutoscrollFromEditor() {
        return autoscrollFromEditor;
    }

    public BooleanSetting getAutoscrollToEditor() {
        return autoscrollToEditor;
    }

    public BooleanSetting getShowObjectProperties() {
        return showObjectProperties;
    }

    public boolean isDisposed() {
        return false;
    }

    public String toString() {
        return "DB Browser";
    }

    public synchronized void navigateToElement(@Nullable BrowserTreeNode treeNode, boolean requestFocus) {
        ToolWindow toolWindow = getBrowserToolWindow();

        toolWindow.show(null);
        if (treeNode != null) {
            getToolWindowForm().getBrowserForm().selectElement(treeNode, requestFocus);
        }
    }

    public synchronized void navigateToElement(@Nullable BrowserTreeNode treeNode) {
        if (treeNode != null) {
            getToolWindowForm().getBrowserForm().selectElement(treeNode, false);
        }
    }

    public boolean isVisible() {
        ToolWindow toolWindow = getBrowserToolWindow();
        return toolWindow.isVisible();
    }

    /***************************************
     *     FileEditorManagerListener       *
     ***************************************/

    private boolean scroll() {
        return autoscrollFromEditor.value() && (AUTOSCROLL_FROM_EDITOR.get() == null || AUTOSCROLL_FROM_EDITOR.get());
    }

    /***************************************
     *            ProjectComponent         *
     ***************************************/
    public static DatabaseBrowserManager getInstance(Project project) {
        return project.getComponent(DatabaseBrowserManager.class);
    }

    @NonNls @NotNull
    public String getComponentName() {
        return "DBNavigator.Project.DatabaseBrowserManager";
    }

    public void initComponent() {
        Project project = getProject();
        EventManager.subscribe(project, FileEditorManagerListener.FILE_EDITOR_MANAGER, fileEditorManagerListener);
        EventManager.subscribe(project, ObjectFilterChangeListener.TOPIC, filterChangeListener);
        EventManager.subscribe(project, ConnectionManagerListener.TOPIC, connectionManagerListener);
    }

    public void disposeComponent() {
        EventManager.unsubscribe(
                fileEditorManagerListener,
                filterChangeListener,
                connectionManagerListener);

        DisposerUtil.dispose(toolWindowForm);
        toolWindowForm = null;
        super.disposeComponent();
    }

    /**
     *
     * @deprecated
     */
    public static void scrollToSelectedElement(final ConnectionHandler connectionHandler) {
        if (connectionHandler != null && !connectionHandler.isDisposed()) {
            DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(connectionHandler.getProject());
            BrowserToolWindowForm toolWindowForm = browserManager.getToolWindowForm();
            if (toolWindowForm != null) {
                final DatabaseBrowserTree browserTree = toolWindowForm.getBrowserTree(connectionHandler);
                if (browserTree != null && browserTree.getTargetSelection() != null) {
                    new ConditionalLaterInvocator() {
                        public void execute() {
                            browserTree.scrollToSelectedElement();
                        }
                    }.start();
                }
            }
        }
    }

    public void dispose() {
    }

    public boolean isTabbedMode() {
        DatabaseBrowserSettings browserSettings = DatabaseBrowserSettings.getInstance(getProject());
        return browserSettings.getGeneralSettings().getDisplayMode() == BrowserDisplayMode.TABBED;
    }

    /***************************************
     *           ModuleListener            *
     ***************************************/

    /**********************************************************
     *                       Listeners                        *
     **********************************************************/
    private ConnectionManagerListener connectionManagerListener = new ConnectionManagerListener() {
        @Override
        public void connectionsChanged() {
            if (toolWindowForm != null) {
                toolWindowForm.getBrowserForm().rebuild();
            }
        }
    };

    private ObjectFilterChangeListener filterChangeListener = new ObjectFilterChangeListener() {
        public void typeFiltersChanged(ConnectionHandler connectionHandler) {
            if (connectionHandler == null) {
                getToolWindowForm().getBrowserForm().rebuildTree();
            } else {
                connectionHandler.getObjectBundle().rebuildTreeChildren();
            }
        }

        @Override
        public void nameFiltersChanged(ConnectionHandler connectionHandler, @Nullable DBObjectType objectType) {
            connectionHandler.getObjectBundle().refreshTreeChildren(objectType);
        }
    };

    public Filter<BrowserTreeNode> getObjectTypeFilter() {
        DatabaseBrowserSettings browserSettings = DatabaseBrowserSettings.getInstance(getProject());
        return browserSettings.getFilterSettings().getObjectTypeFilterSettings().getElementFilter();
    }

    private FileEditorManagerListener fileEditorManagerListener = new FileEditorManagerAdapter() {
        public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
            if (scroll()) {
                if (file instanceof DBEditableObjectVirtualFile) {
                    DBEditableObjectVirtualFile databaseFile = (DBEditableObjectVirtualFile) file;
                    navigateToElement(databaseFile.getObject());
                }
                else  if (file instanceof DBVirtualFile) {
                    DBVirtualFile databaseVirtualFile = (DBVirtualFile) file;
                    ConnectionHandler connectionHandler = FailsafeUtil.get(databaseVirtualFile.getConnectionHandler());
                    navigateToElement(connectionHandler.getObjectBundle());
                }
            }
        }

        public void selectionChanged(@NotNull FileEditorManagerEvent event) {
            if (scroll()) {
                VirtualFile newFile = event.getNewFile();
                VirtualFile oldFile = event.getOldFile();

                if (oldFile != null && !oldFile.equals(newFile)) {
                    if (newFile instanceof DBEditableObjectVirtualFile) {
                        DBEditableObjectVirtualFile databaseFile = (DBEditableObjectVirtualFile) newFile;
                        navigateToElement(databaseFile.getObject());
                    }
                    else  if (newFile instanceof DBVirtualFile) {
                        DBVirtualFile databaseVirtualFile = (DBVirtualFile) newFile;
                        ConnectionHandler connectionHandler = FailsafeUtil.get(databaseVirtualFile.getConnectionHandler());
                        navigateToElement(connectionHandler.getObjectBundle());
                    }
                }
            }
        }
    };

    public void showObjectProperties(boolean visible) {
        BrowserToolWindowForm toolWindowForm = getToolWindowForm();
        if (visible)
            toolWindowForm.showObjectProperties(); else
            toolWindowForm.hideObjectProperties();
        showObjectProperties.setValue(visible);
    }

    public List<DBObject> getSelectedObjects() {
        List<DBObject> selectedObjects = new ArrayList<DBObject>();
        DatabaseBrowserTree activeBrowserTree = getActiveBrowserTree();
        if (activeBrowserTree != null) {
            TreePath[] selectionPaths = activeBrowserTree.getSelectionPaths();
            if (selectionPaths != null) {
                for (TreePath treePath : selectionPaths) {
                    Object lastPathComponent = treePath.getLastPathComponent();
                    if (lastPathComponent instanceof DBObject) {
                        DBObject object = (DBObject) lastPathComponent;
                        selectedObjects.add(object);
                    }
                }
            }
        }
        return selectedObjects;
    }

    /****************************************
     *       PersistentStateComponent       *
     *****************************************/
    @Nullable
    @Override
    public Element getState() {
        Element element = new Element("state");
        autoscrollToEditor.writeConfiguration(element);
        autoscrollFromEditor.writeConfiguration(element);
        showObjectProperties.writeConfiguration(element);
        storeTouchedNodes(element);
        return element;
    }

    @Override
    public void loadState(final Element element) {
        autoscrollToEditor.readConfiguration(element);
        autoscrollFromEditor.readConfiguration(element);
        showObjectProperties.readConfiguration(element);
        initTouchedNodes(element);
    }

    private void storeTouchedNodes(Element element) {
        Element nodesElement = new Element("loaded-nodes");
        element.addContent(nodesElement);

        ConnectionManager connectionManager = ConnectionManager.getInstance(getProject());
        List<ConnectionHandler> connectionHandlers = connectionManager.getConnectionHandlers();
        for (ConnectionHandler connectionHandler : connectionHandlers) {
            Element connectionElement = new Element("connection");

            boolean addConnectionElement = false;
            for (DBSchema schema : connectionHandler.getObjectBundle().getSchemas()) {
                List<DBObjectType> objectTypes = new ArrayList<DBObjectType>();
                DBObjectListContainer childObjects = schema.getChildObjects();
                if (childObjects != null) {
                    List<DBObjectList<DBObject>> allObjectLists = childObjects.getAllObjectLists();
                    for (DBObjectList<DBObject> objectList : allObjectLists) {
                        if (objectList.isLoaded() || objectList.isLoading()) {
                            objectTypes.add(objectList.getObjectType());
                        }
                    }
                }
                if (objectTypes.size() > 0) {
                    Element schemaElement = new Element("schema");
                    schemaElement.setAttribute("name", schema.getName());
                    schemaElement.setAttribute("object-types", DBObjectType.toCommaSeparated(objectTypes));
                    connectionElement.addContent(schemaElement);
                    addConnectionElement = true;
                }
            }

            if (addConnectionElement) {
                connectionElement.setAttribute("connection-id", connectionHandler.getId());
                nodesElement.addContent(connectionElement);
            }
        }
    }


    private void initTouchedNodes(Element element) {
        Element nodesElement = element.getChild("loaded-nodes");
        if (nodesElement != null) {
            final Project project = getProject();
            List<Element> connectionElements = nodesElement.getChildren();
            ConnectionManager connectionManager = ConnectionManager.getInstance(project);
            for (final Element connectionElement : connectionElements) {
                String connectionId = connectionElement.getAttributeValue("connection-id");
                final ConnectionHandler connectionHandler = connectionManager.getConnectionHandler(connectionId);
                if (connectionHandler != null) {
                    String connectionString = " (" + connectionHandler.getName() + ")";
                    new BackgroundTask(project, "Loading data dictionary" + connectionString, true) {

                        @Override
                        protected void execute(@NotNull ProgressIndicator progressIndicator) throws InterruptedException {
                            List<Element> schemaElements = connectionElement.getChildren();
                            for (Element schemaElement : schemaElements) {
                                String schemaName = schemaElement.getAttributeValue("name");
                                DBSchema schema = connectionHandler.getObjectBundle().getSchema(schemaName);
                                if (schema != null) {
                                    String objectTypesAttr = schemaElement.getAttributeValue("object-types");
                                    List<DBObjectType> objectTypes = DBObjectType.fromCommaSeparated(objectTypesAttr);
                                    for (DBObjectType objectType : objectTypes) {
                                        DBObjectListContainer childObjects = schema.getChildObjects();
                                        if (childObjects != null) {
                                            childObjects.loadObjectList(objectType);
                                        }
                                    }
                                }
                            }
                        }
                    }.start();
                }
            }
        }
    }


}
