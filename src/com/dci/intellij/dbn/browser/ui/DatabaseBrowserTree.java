package com.dci.intellij.dbn.browser.ui;

import javax.swing.JPopupMenu;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.browser.DatabaseBrowserUtils;
import com.dci.intellij.dbn.browser.TreeNavigationHistory;
import com.dci.intellij.dbn.browser.model.BrowserTreeModel;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.model.EmptyBrowserTreeModel;
import com.dci.intellij.dbn.browser.model.TabbedBrowserTreeModel;
import com.dci.intellij.dbn.common.content.DatabaseLoadMonitor;
import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.ModalTask;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.common.ui.tree.DBNTree;
import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.action.ConnectionActionGroup;
import com.dci.intellij.dbn.object.action.ObjectActionGroup;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.action.ObjectListActionGroup;
import com.dci.intellij.dbn.object.common.property.DBObjectProperties;
import com.dci.intellij.dbn.object.common.property.DBObjectProperty;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPopupMenu;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.ui.tree.TreeUtil;

public class DatabaseBrowserTree extends DBNTree implements Disposable {
    public static final EmptyBrowserTreeModel EMPTY_TREE_MODEL = new EmptyBrowserTreeModel();
    private BrowserTreeNode targetSelection;
    private BrowserTreeModel treeModel;
    private JPopupMenu popupMenu;
    private TreeNavigationHistory navigationHistory = new TreeNavigationHistory();

    public DatabaseBrowserTree(BrowserTreeModel treeModel) {
        super(treeModel);
        this.treeModel = treeModel;

        addKeyListener(keyListener);
        addMouseListener(mouseListener);
        addTreeSelectionListener(treeSelectionListener);

        setToggleClickCount(0);
        setRootVisible(treeModel instanceof TabbedBrowserTreeModel);
        setShowsRootHandles(true);
        setAutoscrolls(true);
        DatabaseBrowserTreeCellRenderer browserTreeCellRenderer = new DatabaseBrowserTreeCellRenderer(treeModel.getProject());
        setCellRenderer(browserTreeCellRenderer);
        //setExpandedState(DatabaseBrowserUtils.createTreePath(treeModel.getRoot()), false);

        DatabaseBrowserTreeSpeedSearch speedSearch = new DatabaseBrowserTreeSpeedSearch(this);

        Disposer.register(this, speedSearch);
        Disposer.register(this, treeModel);
        Disposer.register(this, navigationHistory);
    }

    public Project getProject() {
        return treeModel.getProject();
    }

    @Override
    public BrowserTreeModel getModel() {
        return (BrowserTreeModel) super.getModel();
    }

    public TreeNavigationHistory getNavigationHistory() {
        return navigationHistory;
    }

    public void expandConnectionManagers() {
        new SimpleLaterInvocator() {
            public void execute() {
                ConnectionManager connectionManager = ConnectionManager.getInstance(getProject());
                ConnectionBundle connectionBundle = connectionManager.getConnectionBundle();
                TreePath treePath = DatabaseBrowserUtils.createTreePath(connectionBundle);
                setExpandedState(treePath, true);
            }
        }.start();
    }

    public void selectElement(BrowserTreeNode treeNode, boolean requestFocus) {
        ConnectionHandler connectionHandler = treeNode.getConnectionHandler();
        Filter<BrowserTreeNode> filter = connectionHandler == null ?
                DatabaseBrowserManager.getInstance(getProject()).getObjectTypeFilter() :
                connectionHandler.getObjectTypeFilter();

        if (filter.accepts(treeNode)) {
            targetSelection = treeNode;
            scrollToSelectedElement();
            if (requestFocus) requestFocus();
        }

    }

    public synchronized void scrollToSelectedElement() {
        if (getProject().isOpen() && targetSelection != null) {
            targetSelection = (BrowserTreeNode) targetSelection.getUndisposedElement();
            TreePath treePath = DatabaseBrowserUtils.createTreePath(targetSelection);
            if (treePath != null) {
                for (Object object : treePath.getPath()) {
                    BrowserTreeNode treeNode = (BrowserTreeNode) object;
                    if (treeNode == null || treeNode.isDisposed()) {
                        targetSelection = null;
                        return;
                    }


                    if (treeNode.equals(targetSelection)) {
                        break;
                    }

                    if (!treeNode.isLeafTreeElement() && !treeNode.isTreeStructureLoaded()) {
                        selectPath(DatabaseBrowserUtils.createTreePath(treeNode));
                        treeNode.getTreeChildren();
                        return;
                    }
                }

                targetSelection = null;
                selectPath(treePath);
            }
        }
    }



    public BrowserTreeNode getSelectedNode() {
        TreePath selectionPath = getSelectionPath();
        return selectionPath == null ? null : (BrowserTreeNode) selectionPath.getLastPathComponent();
    }

    public BrowserTreeNode getTargetSelection() {
        return targetSelection;
    }

    private void selectPath(final TreePath treePath) {
        new SimpleLaterInvocator() {
            public void execute() {
                TreeUtil.selectPath(DatabaseBrowserTree.this, treePath, true);
            }
        }.start();
    }


    public String getToolTipText(MouseEvent event) {
        TreePath path = getClosestPathForLocation(event.getX(), event.getY());
        if (path != null) {
            Rectangle pathBounds = getPathBounds(path);

            if (pathBounds != null) {
                Point mouseLocation = GUIUtil.getRelativeMouseLocation(event.getComponent());
                if (pathBounds.contains(mouseLocation)) {
                    Object object = path.getLastPathComponent();
                    if (object instanceof ToolTipProvider) {
                        ToolTipProvider toolTipProvider = (ToolTipProvider) object;
                        boolean ensureDataLoaded = DatabaseLoadMonitor.isEnsureDataLoaded();
                        try {
                            DatabaseLoadMonitor.setEnsureDataLoaded(false);
                            return toolTipProvider.getToolTip();
                        } finally {
                            DatabaseLoadMonitor.setEnsureDataLoaded(ensureDataLoaded);
                        }
                    }
                }
            }
        }
        return null;
    }

    public void navigateBack() {
        BrowserTreeNode treeNode = navigationHistory.previous();
        if (treeNode != null) {
            selectPathSilently(DatabaseBrowserUtils.createTreePath(treeNode));
        }
    }

    public void navigateForward() {
        BrowserTreeNode treeNode = navigationHistory.next();
        if (treeNode != null) {
            selectPathSilently(DatabaseBrowserUtils.createTreePath(treeNode));
        }
    }


    public void selectPathSilently(TreePath treePath) {
        if (treePath != null) {
            listenersEnabled = false;
            selectionModel.setSelectionPath(treePath);
            TreeUtil.selectPath(DatabaseBrowserTree.this, treePath, true);
            listenersEnabled = true;
        }
    }

    private boolean listenersEnabled = true;

    public void expandAll() {
        BrowserTreeNode root = getModel().getRoot();
        expand(root);
    }

    public void expand(BrowserTreeNode treeNode) {
        if (treeNode.canExpand()) {
            expandPath(DatabaseBrowserUtils.createTreePath(treeNode));
            for (int i = 0; i < treeNode.getTreeChildCount(); i++) {
                BrowserTreeNode childTreeNode = treeNode.getTreeChild(i);
                expand(childTreeNode);
            }
        }
    }

    public void collapseAll() {
        BrowserTreeNode root = getModel().getRoot();
        collapse(root);
    }

    public void collapse(BrowserTreeNode treeNode) {
        if (!treeNode.isLeafTreeElement() && treeNode.isTreeStructureLoaded()) {
            for (int i = 0; i < treeNode.getTreeChildCount(); i++) {
                BrowserTreeNode childTreeNode = treeNode.getTreeChild(i);
                collapse(childTreeNode);
                collapsePath(DatabaseBrowserUtils.createTreePath(childTreeNode));
            }
        }
    }

    private void processSelectEvent(InputEvent event, TreePath path, boolean deliberate) {
        if (path != null) {
            Object lastPathEntity = path.getLastPathComponent();
            if (lastPathEntity instanceof DBObject) {
                final DBObject object = (DBObject) lastPathEntity;
                DBObjectProperties properties = object.getProperties();
                if (properties.is(DBObjectProperty.EDITABLE)) {
                    DBSchemaObject schemaObject = (DBSchemaObject) object;
                    DatabaseFileSystem.getInstance().openEditor(schemaObject, deliberate);
                    event.consume();
                } else if (properties.is(DBObjectProperty.NAVIGABLE)) {
                    DatabaseFileSystem.getInstance().openEditor(object, deliberate);
                    event.consume();
                } else if (deliberate) {
                    new BackgroundTask(getProject(), "Loading Object Reference", false, false) {
                        protected void execute(@NotNull ProgressIndicator progressIndicator) throws InterruptedException {
                            final DBObject navigationObject = object.getDefaultNavigationObject();
                            if (navigationObject != null) {
                                new SimpleLaterInvocator(){
                                    public void execute() {
                                        navigationObject.navigate(true);
                                    }
                                }.start();
                            }

                        }
                    }.start();

                }
            } else if (lastPathEntity instanceof DBObjectBundle) {
                DBObjectBundle objectBundle = (DBObjectBundle) lastPathEntity;
                ConnectionHandler connectionHandler = objectBundle.getConnectionHandler();
                if (connectionHandler != null && !connectionHandler.isDisposed()) {
                    FileEditorManager fileEditorManager = FileEditorManager.getInstance(connectionHandler.getProject());
                    fileEditorManager.openFile(connectionHandler.getConsoleBundle().getDefaultConsole(), deliberate);
                }
            }
        }
    }
    
/*    @Override
    protected void processMouseMotionEvent(MouseEvent e) {
        boolean navigable = false;
        if (e.isControlDown() && e.getID() != MouseEvent.MOUSE_DRAGGED && !e.isConsumed()) {
            TreePath path = getPathForLocation(e.getX(), e.getY());
            Object lastPathEntity = path == null ? null : path.getLastPathComponent();
            if (lastPathEntity instanceof DBObject) {
                DBObject object = (DBObject) lastPathEntity;
                DBObject navigationObject = object.getDefaultNavigationObject();
                navigable = navigationObject != null;
            }
            
        }

        if (navigable) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            super.processMouseMotionEvent(e);
            setCursor(Cursor.getDefaultCursor());
        }
    }  */

    /********************************************************
     *                 TreeSelectionListener                *
     ********************************************************/
    private TreeSelectionListener treeSelectionListener = new TreeSelectionListener() {
        public void valueChanged(TreeSelectionEvent e) {
            if (!isDisposed() && listenersEnabled) {
                Object object = e.getPath().getLastPathComponent();
                if (object != null && object instanceof BrowserTreeNode) {
                    BrowserTreeNode treeNode = (BrowserTreeNode) object;
                    if (targetSelection == null || treeNode.equals(targetSelection)) {
                        navigationHistory.add(treeNode);
                    }
                }

                BrowserSelectionChangeListener listener = EventManager.notify(getProject(), BrowserSelectionChangeListener.TOPIC);
                listener.browserSelectionChanged();

            }
        }
    };

    /********************************************************
     *                      MouseListener                   *
     ********************************************************/
    private MouseListener mouseListener = new MouseAdapter() {
        public void mouseClicked(MouseEvent event) {
            if (event.getButton() == MouseEvent.BUTTON1) {
                DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(getProject());
                if (browserManager.getAutoscrollToEditor().value() || event.getClickCount() > 1) {
                    TreePath path = getPathForLocation(event.getX(), event.getY());
                    processSelectEvent(event, path, event.getClickCount() > 1);
                }
            }
        }

        public void mouseReleased(final MouseEvent event) {
            if (event.getButton() == MouseEvent.BUTTON3) {
                final TreePath path = getPathForLocation(event.getX(), event.getY());
                if (path != null) {
                    final BrowserTreeNode lastPathEntity = (BrowserTreeNode) path.getLastPathComponent();
                    if (lastPathEntity.isDisposed()) return;

                    new ModalTask(lastPathEntity.getProject(), "Loading object information", true) {
                        public void execute(@NotNull ProgressIndicator progressIndicator) {
                            ActionGroup actionGroup = null;
                            if (lastPathEntity instanceof DBObjectList) {
                                DBObjectList objectList = (DBObjectList) lastPathEntity;
                                actionGroup = new ObjectListActionGroup(objectList);
                            } else if (lastPathEntity instanceof DBObject) {
                                DBObject object = (DBObject) lastPathEntity;
                                actionGroup = new ObjectActionGroup(object);
                            } else if (lastPathEntity instanceof DBObjectBundle) {
                                DBObjectBundle objectsBundle = (DBObjectBundle) lastPathEntity;
                                ConnectionHandler connectionHandler = objectsBundle.getConnectionHandler();
                                actionGroup = new ConnectionActionGroup(connectionHandler);
                            }

                            if (actionGroup != null && !progressIndicator.isCanceled()) {
                                ActionPopupMenu actionPopupMenu = ActionManager.getInstance().createActionPopupMenu("", actionGroup);
                                popupMenu = actionPopupMenu.getComponent();
                                new SimpleLaterInvocator() {
                                    public void execute() {
                                        popupMenu.show(DatabaseBrowserTree.this, event.getX(), event.getY());
                                    }
                                }.start();
                            } else {
                                popupMenu = null;
                            }
                        }
                    }.start();
                }
            }
        }
    };

    /********************************************************
     *                      KeyListener                     *
     ********************************************************/
    private KeyListener keyListener = new KeyAdapter() {
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == 10) {  // ENTER
                TreePath path = getSelectionPath();
                processSelectEvent(e, path, true);
            }
        }
    };

    /********************************************************
     *                    Disposable                        *
     ********************************************************/
    private boolean disposed;

    public void dispose() {
        if (!disposed) {
            disposed = true;
            targetSelection = null;
            setModel(EMPTY_TREE_MODEL);
            GUIUtil.removeListeners(this);
            treeSelectionListener = null;
            mouseListener = null;
            keyListener = null;
            treeModelListener = null;
        }
    }


    @Override
    public boolean isDisposed() {
        return disposed;
    }
}
