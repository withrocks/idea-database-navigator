package com.dci.intellij.dbn.object.common;

import javax.swing.Icon;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.browser.DatabaseBrowserUtils;
import com.dci.intellij.dbn.browser.model.BrowserTreeChangeListener;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.model.LoadInProgressTreeNode;
import com.dci.intellij.dbn.browser.ui.HtmlToolTipBuilder;
import com.dci.intellij.dbn.browser.ui.ToolTipProvider;
import com.dci.intellij.dbn.code.common.lookup.LookupItemBuilder;
import com.dci.intellij.dbn.code.common.lookup.ObjectLookupItemBuilder;
import com.dci.intellij.dbn.code.sql.color.SQLTextAttributesKeys;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.environment.EnvironmentType;
import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.thread.ConditionalLaterInvocator;
import com.dci.intellij.dbn.common.thread.SimpleBackgroundTask;
import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.common.util.CollectionUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionUtil;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseInterface;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.psql.PSQLLanguage;
import com.dci.intellij.dbn.language.sql.SQLLanguage;
import com.dci.intellij.dbn.navigation.psi.NavigationPsiCache;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.DBUser;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationList;
import com.dci.intellij.dbn.object.common.list.DBObjectRelationListContainer;
import com.dci.intellij.dbn.object.common.operation.DBOperationExecutor;
import com.dci.intellij.dbn.object.common.operation.DBOperationNotSupportedException;
import com.dci.intellij.dbn.object.common.operation.DBOperationType;
import com.dci.intellij.dbn.object.common.property.DBObjectProperties;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.object.properties.ConnectionPresentableProperty;
import com.dci.intellij.dbn.object.properties.DBObjectPresentableProperty;
import com.dci.intellij.dbn.object.properties.PresentableProperty;
import com.dci.intellij.dbn.vfs.DBObjectVirtualFile;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiInvalidElementAccessException;

public abstract class DBObjectImpl extends DBObjectPsiAbstraction implements DBObject, ToolTipProvider {
    public static final List<DBObject> EMPTY_OBJECT_LIST = Collections.unmodifiableList(new ArrayList<DBObject>(0));
    public static final List<BrowserTreeNode> EMPTY_TREE_NODE_LIST = Collections.unmodifiableList(new ArrayList<BrowserTreeNode>(0));

    private List<BrowserTreeNode> allPossibleTreeChildren;
    private List<BrowserTreeNode> visibleTreeChildren;
    private boolean treeChildrenLoaded;
    private boolean isDisposed = false;

    protected String name;
    protected DBObjectRef objectRef;
    protected DBObjectRef parentObject;
    private DBObjectProperties properties;
    private DBObjectListContainer childObjects;
    private DBObjectRelationListContainer childObjectRelations;
    private DBObjectBundle objectBundle;

    private LookupItemBuilder sqlLookupItemBuilder;
    private LookupItemBuilder psqlLookupItemBuilder;

    protected DBObjectVirtualFile virtualFile;

    private static final DBOperationExecutor NULL_OPERATION_EXECUTOR = new DBOperationExecutor() {
        public void executeOperation(DBOperationType operationType) throws SQLException, DBOperationNotSupportedException {
            throw new DBOperationNotSupportedException(operationType);
        }
    };

    public DBObjectImpl(DBObject parentObject, ResultSet resultSet) throws SQLException {
        this.parentObject = DBObjectRef.from(parentObject);
        init(resultSet);
    }

    public DBObjectImpl(DBObjectBundle objectBundle, ResultSet resultSet) throws SQLException {
        this.objectBundle = objectBundle;
        init(resultSet);
    }

    public DBObjectImpl(DBObjectBundle objectBundle, String name) {
        this.objectBundle = objectBundle;
        this.name = name;
    }

    private void init(ResultSet resultSet) throws SQLException {
        checkConnection();
        initObject(resultSet);
        initStatus(resultSet);
        initProperties();
        initLists();

        checkConnection();
        objectRef = new DBObjectRef(this);
    }

    protected abstract void initObject(ResultSet resultSet) throws SQLException;

    public void initStatus(ResultSet resultSet) throws SQLException {}

    protected void initProperties() {}

    protected void initLists() {}

    @Override
    public PsiElement getParent() {
        PsiFile containingFile = getContainingFile();
        if (containingFile != null) {
            return containingFile.getParent();
        }
        return null;
    }

    public DBContentType getContentType() {
        return DBContentType.NONE;
    }

    @Override
    public DBObjectRef getRef() {
        return objectRef;
    }

    @Override
    public boolean isParentOf(DBObject object) {
        return this.equals(object.getParentObject());
    }

    protected void checkConnection() throws SQLException {
        ConnectionHandler connectionHandler = getConnectionHandler();
        if (connectionHandler == null) throw DatabaseInterface.DBN_INTERRUPTED_EXCEPTION;
    }

    public DBObjectProperties getProperties() {
        if (properties == null) {
            properties = new DBObjectProperties();
        }
        return properties;
    }

    public DBOperationExecutor getOperationExecutor() {
        return NULL_OPERATION_EXECUTOR;
    }

    @Override
    public DBSchema getSchema() {
        DBObject object = this;
        while (object != null) {
            if (object instanceof DBSchema) {
                return (DBSchema) object;
            }
            object = object.getParentObject();
        }
        return null;
    }

    public DBObject getParentObject() {
        return DBObjectRef.get(parentObject);
    }

    public DBObject getDefaultNavigationObject() {
        return null;
    }

    public boolean isOfType(DBObjectType objectType) {
        return getObjectType().matches(objectType);
    }

    public String getTypeName() {
        return getObjectType().getName();
    }

    @NotNull
    public String getName() {
        return name;
    }

    @Override
    public int getOverload() {
        return 0;
    }

    @Override
    public String getQuotedName(boolean quoteAlways) {
        if (quoteAlways || needsNameQuoting()) {
            DatabaseCompatibilityInterface compatibilityInterface = DatabaseCompatibilityInterface.getInstance(this);
            char quoteChar = compatibilityInterface.getIdentifierQuotes();
            return quoteChar + name + quoteChar;
        } else {
            return name;
        }
    }

    public boolean needsNameQuoting() {
        return name.indexOf('-') > 0 ||
                name.indexOf('.') > 0 ||
                name.indexOf('#') > 0 ||
                getLanguageDialect(SQLLanguage.INSTANCE).isReservedWord(name);
    }

    @Nullable
    public Icon getIcon() {
        return getObjectType().getIcon();
    }

    public String getQualifiedName() {
        return objectRef.getPath();
    }

    public String getQualifiedNameWithType() {
        return objectRef.getQualifiedNameWithType();
    }

    public DBUser getOwner() {
        DBObject parentObject = getParentObject();
        return parentObject == null ? null : parentObject.getOwner();
    }

    public Icon getOriginalIcon() {
        return getIcon();
    }

    public String getNavigationTooltipText() {
        DBObject parentObject = getParentObject();
        if (parentObject == null) {
            return getTypeName();
        } else {
            return getTypeName() + " (" +
                    parentObject.getTypeName() + ' ' +
                    parentObject.getName() + ')';
        }
    }


    public String getToolTip() {
        if (isDisposed) {
            return null;
        }
        return new HtmlToolTipBuilder() {
            public void buildToolTip() {
                DBObjectImpl.this.buildToolTip(this);
            }
        }.getToolTip();
    }

    public void buildToolTip(HtmlToolTipBuilder ttb) {
        ConnectionHandler connectionHandler = getConnectionHandler();
        ttb.append(true, getQualifiedName(), false);
        ttb.append(true, "Connection: ", "-2", null, false );
        ttb.append(false, connectionHandler == null ? "[unknown]" : connectionHandler.getPresentableText(), false);
    }

    public DBObjectAttribute[] getObjectAttributes(){return null;}
    public DBObjectAttribute getNameAttribute(){return null;}

    @Nullable
    @Override
    public DBObjectBundle getObjectBundle() {
        ConnectionHandler connectionHandler = getConnectionHandler();
        return connectionHandler == null ? null : connectionHandler.getObjectBundle();
    }

    @Nullable
    public ConnectionHandler getConnectionHandler() {
        if (parentObject != null) {
            DBObject object = parentObject.get();
            if (object != null) {
                return object.getConnectionHandler();
            }
        } else if (objectBundle != null) {
            return objectBundle.getConnectionHandler();
        }
        return null;
    }

    @Override
    public EnvironmentType getEnvironmentType() {
        ConnectionHandler connectionHandler = getConnectionHandler();
        return connectionHandler == null ? EnvironmentType.DEFAULT : connectionHandler.getEnvironmentType();
    }

    public DBLanguageDialect getLanguageDialect(DBLanguage language) {
        ConnectionHandler connectionHandler = getConnectionHandler();
        return connectionHandler == null ?
                SQLLanguage.INSTANCE.getMainLanguageDialect() :
                connectionHandler.getLanguageDialect(language);
    }

    public DBObjectListContainer getChildObjects() {
        return childObjects;
    }

    public DBObjectRelationListContainer getChildObjectRelations() {
        return childObjectRelations;
    }

    public DBObjectListContainer initChildObjects() {
        if (childObjects == null) {
            childObjects = new DBObjectListContainer(this);
        }
        return childObjects;
    }

    public DBObjectRelationListContainer initChildObjectRelations() {
        if (childObjectRelations == null) {
            childObjectRelations = new DBObjectRelationListContainer(this);
        }
        return childObjectRelations;

    }

    public static DBObject getObjectByName(List<? extends DBObject> objects, String name) {
        if (objects != null) {
            for (DBObject object : objects) {
                if (object.getName().equals(name)) {
                    return object;
                }
            }
        }
        return null;
    }

    public DBObject getChildObject(DBObjectType objectType, String name, boolean lookupHidden) {
        return getChildObject(objectType, name, 0, lookupHidden);
    }

    public DBObject getChildObject(DBObjectType objectType, String name, int overload, boolean lookupHidden) {
        if (childObjects == null) {
            return null;
        } else {
            DBObject object = childObjects.getObject(objectType, name, overload);
            if (object == null && lookupHidden) {
                object = childObjects.getHiddenObject(objectType, name, overload);
            }
            return object;
        }
    }

    public DBObject getChildObject(String name, boolean lookupHidden) {
        return getChildObject(name, 0, lookupHidden);
    }

    public DBObject getChildObject(String name, int overload, boolean lookupHidden) {
        return childObjects == null ? null :
                childObjects.getObjectForParentType(this.getObjectType(), name, overload, lookupHidden);
    }

    public DBObject getChildObjectNoLoad(String name) {
        return getChildObjectNoLoad(name, 0);
    }

    public DBObject getChildObjectNoLoad(String name, int overload) {
        return childObjects == null ? null : childObjects.getObjectNoLoad(name, overload);
    }

    @NotNull
    public List<DBObject> getChildObjects(DBObjectType objectType) {
        if (objectType.getFamilyTypes().size() > 1) {
            List<DBObject> list = new ArrayList<DBObject>();
            for (DBObjectType childObjectType : objectType.getFamilyTypes()) {
                if (objectType != childObjectType) {
                    List<DBObject> childObjects = getChildObjects(childObjectType);
                    list.addAll(childObjects);
                } else {
                    DBObjectList<? extends DBObject> objectList = childObjects == null ? null : childObjects.getObjectList(objectType);
                    if (objectList != null) {
                        list.addAll(objectList.getObjects());
                    }
                }
            }
            return list;
        } else {
            if (objectType == DBObjectType.ANY) {
                Collection<DBObjectList<DBObject>> objectLists = childObjects.getObjectLists();
                if (objectLists != null) {
                    List<DBObject> objects = new ArrayList<DBObject>();
                    for (DBObjectList objectList : objectLists) {
                        objects.addAll(objectList.getObjects());
                    }
                    return objects;
                }
                return EMPTY_OBJECT_LIST;
            } else {
                DBObjectList<DBObject> objectList = childObjects == null ? null : childObjects.getObjectList(objectType);
                return objectList == null ? EMPTY_OBJECT_LIST : objectList.getObjects();
            }
        }
    }

    @Override
    public DBObjectList<? extends DBObject> getChildObjectList(DBObjectType objectType) {
        return childObjects == null ? null : childObjects.getObjectList(objectType);
    }

    public List<DBObjectNavigationList> getNavigationLists() {
        // todo consider caching;
        return createNavigationLists();
    }

    protected List<DBObjectNavigationList> createNavigationLists() {
        return null;
    }

    public LookupItemBuilder getLookupItemBuilder(DBLanguage language) {
        if (language == SQLLanguage.INSTANCE) {
            if (sqlLookupItemBuilder == null) {
                sqlLookupItemBuilder = new ObjectLookupItemBuilder(this, language);
            }
            return sqlLookupItemBuilder;
        }
        if (language == PSQLLanguage.INSTANCE) {
            if (psqlLookupItemBuilder == null) {
                psqlLookupItemBuilder = new ObjectLookupItemBuilder(this, language);
            }
            return psqlLookupItemBuilder;
        }
        return null;
    }

    public String extractDDL() throws SQLException {
        String ddl = null;
        CallableStatement statement = null;
        Connection connection = null;

        ConnectionHandler connectionHandler = getConnectionHandler();
        if (connectionHandler != null) {
            try {
                connection = connectionHandler.getPoolConnection();
                statement = connection.prepareCall("{? = call DBMS_METADATA.GET_DDL(?, ?, ?)}");
                statement.registerOutParameter(1, Types.CLOB);
                statement.setString(2, getTypeName().toUpperCase());
                statement.setString(3, name);
                statement.setString(4, getParentObject().getName());

                statement.execute();
                ddl = statement.getString(1);
                ddl = ddl == null ? null : ddl.trim();
                statement.close();
            } finally{
                ConnectionUtil.closeStatement(statement);
                connectionHandler.freePoolConnection(connection);
            }
        }
        return ddl;
    }

    @Nullable
    public DBObject getUndisposedElement() {
        return objectRef.get();
    }

    public DynamicContent getDynamicContent(DynamicContentType dynamicContentType) {
        if(dynamicContentType instanceof DBObjectType && childObjects != null) {
            DBObjectType objectType = (DBObjectType) dynamicContentType;
            DynamicContent dynamicContent = childObjects.getObjectList(objectType);
            if (dynamicContent == null) dynamicContent = childObjects.getHiddenObjectList(objectType);
            return dynamicContent;
        }

        else if (dynamicContentType instanceof DBObjectRelationType && childObjectRelations != null) {
            DBObjectRelationType objectRelationType = (DBObjectRelationType) dynamicContentType;
            return childObjectRelations.getObjectRelationList(objectRelationType);
        }

        return null;
    }

    public void reload() {

    }

    @NotNull
    public DBObjectVirtualFile getVirtualFile() {
        if (virtualFile == null) {
            virtualFile = new DBObjectVirtualFile(this);
        }
        return virtualFile;
    }

    /*********************************************************
     *                   NavigationItem                      *
     *********************************************************/
    public FileStatus getFileStatus() {
        return FileStatus.UNKNOWN;
    }

    public ItemPresentation getPresentation() {
        return this;
    }

    public TextAttributesKey getTextAttributesKey() {
        return SQLTextAttributesKeys.IDENTIFIER;
    }

    public String getLocationString() {
        return null;
    }

    public Icon getIcon(boolean open) {
        return getIcon();
    }

    /*********************************************************
     *                  BrowserTreeNode                   *
     *********************************************************/
    public void initTreeElement() {}

    public boolean isTreeStructureLoaded() {
        return treeChildrenLoaded;
    }

    public boolean canExpand() {
        return !isLeafTreeElement() && treeChildrenLoaded && getTreeChild(0).isTreeStructureLoaded();
    }

    public Icon getIcon(int flags) {
        return getIcon();
    }

    public String getPresentableText() {
        return name;
    }

    public String getPresentableTextDetails() {
        return null;
    }

    public String getPresentableTextConditionalDetails() {
        return null;
    }

    public BrowserTreeNode getTreeParent() {
        if (parentObject != null){
            DBObject object = parentObject.get();
            if (object != null) {
                DBObjectListContainer childObjects = object.getChildObjects();
                if (childObjects != null) {
                    return childObjects.getObjectList(getObjectType());
                }
            }
        } else if (objectBundle != null) {
            return objectBundle.getObjectListContainer().getObjectList(getObjectType());
        }
        return null;
    }

    public int getTreeDepth() {
        BrowserTreeNode treeParent = getTreeParent();
        return treeParent == null ? 0 : treeParent.getTreeDepth() + 1;
    }


    @NotNull
    public synchronized List<BrowserTreeNode> getAllPossibleTreeChildren() {
        if (allPossibleTreeChildren == null) {
            allPossibleTreeChildren = buildAllPossibleTreeChildren();
        }
        return allPossibleTreeChildren;
    }



    public List<? extends BrowserTreeNode> getTreeChildren() {
        if (visibleTreeChildren == null) {
            visibleTreeChildren = new ArrayList<BrowserTreeNode>();
            visibleTreeChildren.add(new LoadInProgressTreeNode(this));

            new SimpleBackgroundTask("load database objects") {
                public void execute() {
                    if (!isDisposed()) buildTreeChildren();
                }
            }.start();

        }
        return visibleTreeChildren;
    }

    private void buildTreeChildren() {
        ConnectionHandler connectionHandler = getConnectionHandler();
        if (connectionHandler != null && !isDisposed) {
            Filter<BrowserTreeNode> filter = connectionHandler.getObjectTypeFilter();
            List<BrowserTreeNode> allPossibleTreeChildren = getAllPossibleTreeChildren();
            List<BrowserTreeNode> newTreeChildren = allPossibleTreeChildren;
            if (allPossibleTreeChildren.size() > 0) {
                if (!filter.acceptsAll(allPossibleTreeChildren)) {
                    newTreeChildren = new ArrayList<BrowserTreeNode>();
                    for (BrowserTreeNode treeNode : allPossibleTreeChildren) {
                        if (treeNode != null && filter.accepts(treeNode)) {
                            DBObjectList objectList = (DBObjectList) treeNode;
                            newTreeChildren.add(objectList);
                        }
                    }
                }
                newTreeChildren = new ArrayList<BrowserTreeNode>(newTreeChildren);

                for (BrowserTreeNode treeNode : newTreeChildren) {
                    DBObjectList objectList = (DBObjectList) treeNode;
                    objectList.initTreeElement();
                }

                if (visibleTreeChildren.size() == 1 && visibleTreeChildren.get(0) instanceof LoadInProgressTreeNode) {
                    visibleTreeChildren.get(0).dispose();
                }
            }
            visibleTreeChildren = newTreeChildren;
            treeChildrenLoaded = true;


            Project project = getProject();
            if (!isDisposed && !project.isDisposed()) {
                EventManager.notify(project, BrowserTreeChangeListener.TOPIC).nodeChanged(this, TreeEventType.STRUCTURE_CHANGED);
                new ConditionalLaterInvocator() {
                    public void execute() {
                        if (!isDisposed()) {
                            DatabaseBrowserManager.scrollToSelectedElement(getConnectionHandler());
                        }
                    }
                }.start();
            }
        }
    }

    @Override
    public void refreshTreeChildren(@Nullable DBObjectType objectType) {
        ConnectionHandler connectionHandler = getConnectionHandler();
        if (connectionHandler != null && !isDisposed) {
            if (visibleTreeChildren != null) {
                for (BrowserTreeNode treeNode : visibleTreeChildren) {
                    treeNode.refreshTreeChildren(objectType);
                }
            }
        }
    }

    public void rebuildTreeChildren() {
        ConnectionHandler connectionHandler = getConnectionHandler();
        if (connectionHandler != null && !isDisposed) {
            Filter<BrowserTreeNode> filter = connectionHandler.getObjectTypeFilter();
            if (visibleTreeChildren != null && DatabaseBrowserUtils.treeVisibilityChanged(getAllPossibleTreeChildren(), visibleTreeChildren, filter)) {
                buildTreeChildren();
            }
            if (visibleTreeChildren != null) {
                for (BrowserTreeNode treeNode : visibleTreeChildren) {
                    treeNode.rebuildTreeChildren();
                }
            }
        }
    }

    @NotNull
    public abstract List<BrowserTreeNode> buildAllPossibleTreeChildren();

    public boolean isLeafTreeElement() {
        ConnectionHandler connectionHandler = getConnectionHandler();
        if (connectionHandler != null && !isDisposed) {
            Filter<BrowserTreeNode> filter = connectionHandler.getObjectTypeFilter();
            for (BrowserTreeNode treeNode : getAllPossibleTreeChildren() ) {
                if (treeNode != null && filter.accepts(treeNode)) {
                    return false;
                }
            }
        }
        return true;
    }

    public BrowserTreeNode getTreeChild(int index) {
        return getTreeChildren().get(index);
    }

    public int getTreeChildCount() {
        return getTreeChildren().size();
    }

    public int getIndexOfTreeChild(BrowserTreeNode child) {
        return getTreeChildren().indexOf(child);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof DBObject) {
            DBObject object = (DBObject) obj;
            return objectRef.equals(object.getRef());
        }
        return false;
    }


    public int hashCode() {
        return objectRef.hashCode();
    }

    @NotNull
    public Project getProject() throws PsiInvalidElementAccessException {
        ConnectionHandler connectionHandler = getConnectionHandler();
        Project project = connectionHandler == null ? null : connectionHandler.getProject();
        return FailsafeUtil.nvl(project);
    }

    public int compareTo(@NotNull Object o) {
        if (o instanceof DBObject) {
            DBObject object = (DBObject) o;
            return objectRef.compareTo(object.getRef());
        }
        return -1;
    }

    public String toString() {
        return name;
    }

    public List<PresentableProperty> getPresentableProperties() {
        List<PresentableProperty> properties = new ArrayList<PresentableProperty>();
        DBObject parent = getParentObject();
        while (parent != null) {
            properties.add(new DBObjectPresentableProperty(parent));
            parent = parent.getParentObject();
        }
        properties.add(new ConnectionPresentableProperty(getConnectionHandler()));

        return properties;
    }

    public boolean isValid() {
        return !isDisposed;
    }

    /*********************************************************
    *               DynamicContentElement                    *
    *********************************************************/
    public void dispose() {
        if (!isDisposed) {
            isDisposed = true;
            DisposerUtil.dispose(childObjects);
            DisposerUtil.dispose(childObjectRelations);
            CollectionUtil.clearCollection(visibleTreeChildren);
            CollectionUtil.clearCollection(allPossibleTreeChildren);
            DisposerUtil.dispose(sqlLookupItemBuilder);
            DisposerUtil.dispose(psqlLookupItemBuilder);
            objectBundle = null;
        }
    }


    public boolean isDisposed() {
        return isDisposed;
    }

    public String getDescription() {
        return getQualifiedName();
    }

    /*********************************************************
    *                      Navigatable                      *
    *********************************************************/
    public void navigate(boolean requestFocus) {
        DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(getProject());
        browserManager.navigateToElement(this, requestFocus);
    }

    public boolean canNavigate() {
        return true;
    }

    /*********************************************************
     *                   PsiElement                          *
     *********************************************************/

    @Override
    public PsiFile getContainingFile() throws PsiInvalidElementAccessException {
        return NavigationPsiCache.getPsiFile(this);
    }
}
