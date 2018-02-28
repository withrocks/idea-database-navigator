package com.dci.intellij.dbn.object.common;

import javax.swing.Icon;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.browser.DatabaseBrowserUtils;
import com.dci.intellij.dbn.browser.model.BrowserTreeChangeListener;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.model.LoadInProgressTreeNode;
import com.dci.intellij.dbn.browser.ui.HtmlToolTipBuilder;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentElement;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoader;
import com.dci.intellij.dbn.common.content.loader.DynamicContentResultSetLoader;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.lookup.ConsumerStoppedException;
import com.dci.intellij.dbn.common.lookup.LookupConsumer;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.ConditionalLaterInvocator;
import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.common.util.CollectionUtil;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionPool;
import com.dci.intellij.dbn.connection.GenericDatabaseElement;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.data.type.DBNativeDataType;
import com.dci.intellij.dbn.data.type.DataTypeDefinition;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.database.DatabaseObjectIdentifier;
import com.dci.intellij.dbn.execution.statement.DataDefinitionChangeListener;
import com.dci.intellij.dbn.object.DBCharset;
import com.dci.intellij.dbn.object.DBGrantedPrivilege;
import com.dci.intellij.dbn.object.DBGrantedRole;
import com.dci.intellij.dbn.object.DBObjectPrivilege;
import com.dci.intellij.dbn.object.DBPrivilege;
import com.dci.intellij.dbn.object.DBRole;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.DBSynonym;
import com.dci.intellij.dbn.object.DBSystemPrivilege;
import com.dci.intellij.dbn.object.DBUser;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.common.list.DBObjectRelationListContainer;
import com.dci.intellij.dbn.object.impl.DBCharsetImpl;
import com.dci.intellij.dbn.object.impl.DBGrantedPrivilegeImpl;
import com.dci.intellij.dbn.object.impl.DBGrantedRoleImpl;
import com.dci.intellij.dbn.object.impl.DBObjectPrivilegeImpl;
import com.dci.intellij.dbn.object.impl.DBRoleImpl;
import com.dci.intellij.dbn.object.impl.DBRolePrivilegeRelation;
import com.dci.intellij.dbn.object.impl.DBRoleRoleRelation;
import com.dci.intellij.dbn.object.impl.DBSchemaImpl;
import com.dci.intellij.dbn.object.impl.DBSystemPrivilegeImpl;
import com.dci.intellij.dbn.object.impl.DBUserImpl;
import com.dci.intellij.dbn.object.impl.DBUserPrivilegeRelation;
import com.dci.intellij.dbn.object.impl.DBUserRoleRelation;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;

public class DBObjectBundleImpl implements DBObjectBundle {
    private ConnectionHandler connectionHandler;
    private BrowserTreeNode treeParent;
    private List<BrowserTreeNode> allPossibleTreeChildren;
    private List<BrowserTreeNode> visibleTreeChildren;
    private boolean treeChildrenLoaded;
    private boolean isDisposed;

    private DBObjectList<DBSchema> schemas;
    private DBObjectList<DBUser> users;
    private DBObjectList<DBRole> roles;
    private DBObjectList<DBSystemPrivilege> systemPrivileges;
    private DBObjectList<DBObjectPrivilege> objectPrivileges;
    private DBObjectList<DBCharset> charsets;

    private List<DBNativeDataType> nativeDataTypes;
    private List<DBDataType> cachedDataTypes = new CopyOnWriteArrayList<DBDataType>();

    protected DBObjectListContainer objectLists;
    protected DBObjectRelationListContainer objectRelationLists;
    private int connectionConfigHash;

    public DBObjectBundleImpl(ConnectionHandler connectionHandler, BrowserTreeNode treeParent) {
        this.connectionHandler = connectionHandler;
        this.treeParent = treeParent;
        connectionConfigHash = connectionHandler.getSettings().getDatabaseSettings().hashCode();

        this.objectLists = new DBObjectListContainer(this);
        users = objectLists.createObjectList(DBObjectType.USER, this, USERS_LOADER, true, false);
        schemas = objectLists.createObjectList(DBObjectType.SCHEMA, this, SCHEMAS_LOADER, new DBObjectList[]{users}, true, false);
        roles = objectLists.createObjectList(DBObjectType.ROLE, this, ROLES_LOADER, true, false);
        systemPrivileges = objectLists.createObjectList(DBObjectType.SYSTEM_PRIVILEGE, this, SYSTEM_PRIVILEGES_LOADER, true, false);
        charsets = objectLists.createObjectList(DBObjectType.CHARSET, this, CHARSETS_LOADER, true, false);
        allPossibleTreeChildren = DatabaseBrowserUtils.createList(schemas, users, roles, systemPrivileges, charsets);

        objectRelationLists = new DBObjectRelationListContainer(this);
        objectRelationLists.createObjectRelationList(
                DBObjectRelationType.USER_ROLE, this,
                "User role relations",
                USER_ROLE_RELATION_LOADER,
                users, roles);

        objectRelationLists.createObjectRelationList(
                DBObjectRelationType.USER_PRIVILEGE, this,
                "User privilege relations",
                USER_PRIVILEGE_RELATION_LOADER,
                users, systemPrivileges);

        objectRelationLists.createObjectRelationList(
                DBObjectRelationType.ROLE_ROLE, this,
                "Role role relations",
                ROLE_ROLE_RELATION_LOADER,
                roles);

        objectRelationLists.createObjectRelationList(
                DBObjectRelationType.ROLE_PRIVILEGE, this,
                "Role privilege relations",
                ROLE_PRIVILEGE_RELATION_LOADER,
                roles, systemPrivileges);

        EventManager.subscribe(connectionHandler.getProject(), DataDefinitionChangeListener.TOPIC, dataDefinitionChangeListener);
    }

    private final DataDefinitionChangeListener dataDefinitionChangeListener = new DataDefinitionChangeListener() {
        @Override
        public void dataDefinitionChanged(DBSchema schema, DBObjectType objectType) {
            if (schema.getConnectionHandler() == connectionHandler) {
                DBObjectList childObjectList = schema.getChildObjectList(objectType);
                if (childObjectList != null && childObjectList.isLoaded()) {
                    childObjectList.reload();
                }

                Set<DBObjectType> childObjectTypes = objectType.getChildren();
                for (DBObjectType childObjectType : childObjectTypes) {
                    DBObjectListContainer childObjects = schema.getChildObjects();
                    if (childObjects != null) {
                        childObjectList = childObjects.getHiddenObjectList(childObjectType);
                        if (childObjectList != null && childObjectList.isLoaded()) {
                            childObjectList.reload();
                        }
                    }
                }
            }
        }

        @Override
        public void dataDefinitionChanged(@NotNull DBSchemaObject schemaObject) {
            if (schemaObject.getConnectionHandler() == connectionHandler) {
                DBObjectListContainer childObjects = schemaObject.getChildObjects();
                List<DBObjectList<DBObject>> objectLists = null;
                if (childObjects != null) {
                    objectLists = childObjects.getAllObjectLists();
                    for (DBObjectList objectList : objectLists) {
                        if (objectList.isLoaded()) {
                            objectList.reload();
                        }
                    }
                }
            }
        }
    };

    public boolean isValid() {
        return connectionConfigHash == connectionHandler.getSettings().getDatabaseSettings().hashCode();
    }

    @Nullable
    public ConnectionHandler getConnectionHandler() {
        return FailsafeUtil.get(connectionHandler);
    }

    public List<DBSchema> getSchemas() {
        return schemas.getObjects();
    }

    public List<DBUser> getUsers() {
        return users.getObjects();
    }

    public List<DBRole> getRoles() {
        return roles.getObjects();
    }

    public List<DBSystemPrivilege> getSystemPrivileges() {
        return systemPrivileges.getObjects();
    }

    public List<DBCharset> getCharsets() {
        return charsets.getObjects();
    }

    public synchronized List<DBNativeDataType> getNativeDataTypes(){
        if (nativeDataTypes == null) {
            List<DataTypeDefinition> dataTypeDefinitions = connectionHandler.getInterfaceProvider().getNativeDataTypes().list();
            nativeDataTypes = new ArrayList<DBNativeDataType>();
            for (DataTypeDefinition dataTypeDefinition : dataTypeDefinitions) {
                DBNativeDataType dataType = new DBNativeDataType(dataTypeDefinition);
                nativeDataTypes.add(dataType);
            }
            Collections.sort(nativeDataTypes, new Comparator<DBNativeDataType>() {
                @Override
                public int compare(DBNativeDataType o1, DBNativeDataType o2) {
                    return -o1.compareTo(o2);
                }
            });
        }
        return nativeDataTypes;
    }

    public DBNativeDataType getNativeDataType(String name) {
        String upperCaseName = name.toUpperCase();
        for (DBNativeDataType dataType : getNativeDataTypes()) {
            if (upperCaseName.equals(dataType.getName())) {
                return dataType;
            }
        }
        for (DBNativeDataType dataType : getNativeDataTypes()) {
            if (upperCaseName.startsWith(dataType.getName())) {
                return dataType;
            }
        }
        return null;
    }

    public DBSchema getSchema(String name) {
        return schemas.getObject(name);
    }

    public DBSchema getPublicSchema() {
        return getSchema("PUBLIC");
    }

    public DBSchema getUserSchema() {
        for (DBSchema schema : getSchemas()) {
            if (schema.isUserSchema()) return schema;
        }
        return null;
    }

    public DBUser getUser(String name) {
        return users.getObject(name);
    }

    public DBRole getRole(String name) {
        return roles.getObject(name);
    }

    @Override
    public DBPrivilege getPrivilege(String name) {
        return systemPrivileges.getObject(name);
    }

    public DBSystemPrivilege getSystemPrivilege(String name) {
        return systemPrivileges.getObject(name);
    }

    public DBCharset getCharset(String name) {
        return charsets.getObject(name);
    }

    @Override
    public List<DBDataType> getCachedDataTypes() {
        return cachedDataTypes;
    }

    /*********************************************************
     *                     TreeElement                       *
     *********************************************************/
    public boolean isTreeStructureLoaded() {
        return treeChildrenLoaded;
    }

    public boolean canExpand() {
        return treeChildrenLoaded && getTreeChild(0).isTreeStructureLoaded();
    }

    public int getTreeDepth() {
        return treeParent == null ? 0 : treeParent.getTreeDepth() + 1;
    }

    public BrowserTreeNode getTreeChild(int index) {
        return getTreeChildren().get(index);
    }

    public BrowserTreeNode getTreeParent() {
        return treeParent;
    }

    public List<? extends BrowserTreeNode> getTreeChildren() {
        if (visibleTreeChildren == null) {
            visibleTreeChildren = new ArrayList<BrowserTreeNode>();
            visibleTreeChildren.add(new LoadInProgressTreeNode(this));
            ConnectionHandler connectionHandler = getConnectionHandler();
            String connectionString = connectionHandler == null ? "" : " (" + connectionHandler.getName() + ")";

            new BackgroundTask(getProject(), "Loading data dictionary" + connectionString, true) {
                public void execute(@NotNull ProgressIndicator progressIndicator) {
                    buildTreeChildren();
                }
            }.start();

        }
        return visibleTreeChildren;
    }

    private void buildTreeChildren() {
        List<BrowserTreeNode> newTreeChildren = allPossibleTreeChildren;
        Filter<BrowserTreeNode> filter = connectionHandler.getObjectTypeFilter();
        if (!filter.acceptsAll(allPossibleTreeChildren)) {
            newTreeChildren = new ArrayList<BrowserTreeNode>();
            for (BrowserTreeNode treeNode : allPossibleTreeChildren) {
                if (treeNode != null && filter.accepts(treeNode)) {
                    DBObjectList objectList = (DBObjectList) treeNode;
                    newTreeChildren.add(objectList);
                }
            }
        }

        for (BrowserTreeNode treeNode : newTreeChildren) {
            DBObjectList objectList = (DBObjectList) treeNode;
            objectList.initTreeElement();
        }

        if (visibleTreeChildren.size() == 1 && visibleTreeChildren.get(0) instanceof LoadInProgressTreeNode) {
            visibleTreeChildren.get(0).dispose();
        }

        visibleTreeChildren = newTreeChildren;
        treeChildrenLoaded = true;

        Project project = getProject();
        if (project != null) {
            EventManager.notify(project, BrowserTreeChangeListener.TOPIC).nodeChanged(this, TreeEventType.STRUCTURE_CHANGED);
            new ConditionalLaterInvocator() {
                public void execute() {
                    DatabaseBrowserManager.scrollToSelectedElement(getConnectionHandler());

                }
            }.start();
        }
    }

    @Override
    public void refreshTreeChildren(@Nullable DBObjectType objectType) {
        if (visibleTreeChildren != null) {
            for (BrowserTreeNode treeNode : visibleTreeChildren) {
                treeNode.refreshTreeChildren(objectType);
            }
        }
    }

    public void rebuildTreeChildren() {
        Filter<BrowserTreeNode> filter = connectionHandler.getObjectTypeFilter();
        if (visibleTreeChildren != null && DatabaseBrowserUtils.treeVisibilityChanged(allPossibleTreeChildren, visibleTreeChildren, filter)) {
            buildTreeChildren();
        }

        if (visibleTreeChildren != null) {
            for (BrowserTreeNode treeNode : visibleTreeChildren) {
                treeNode.rebuildTreeChildren();
            }
        }
    }

    public int getTreeChildCount() {
        return getTreeChildren().size();
    }

    public boolean isLeafTreeElement() {
        return false;
    }

    public int getIndexOfTreeChild(BrowserTreeNode child) {
        return getTreeChildren().indexOf(child);
    }

    public Icon getIcon(int flags) {
        return getConnectionHandler().getIcon();
    }

    public String getPresentableText() {
        return getConnectionHandler().getPresentableText();
    }

    public String getPresentableTextDetails() {
        //return getConnectionHandler().isAutoCommit() ? "[Auto Commit]" : null;
        return null;
    }

    public String getPresentableTextConditionalDetails() {
        return null;
    }

    /*********************************************************
     *                  HtmlToolTipBuilder                   *
     *********************************************************/
    public String getToolTip() {
        return new HtmlToolTipBuilder() {
            public void buildToolTip() {
                append(true, "connection", true);
                ConnectionHandler connectionHandler = getConnectionHandler();
                if (connectionHandler.getConnectionStatus().isConnected()) {
                    append(false, " - active", true);
                } else if (connectionHandler.canConnect() && !connectionHandler.isValid()) {
                    append(false, " - invalid", true);
                    append(true, connectionHandler.getConnectionStatus().getStatusMessage(), "-2", "red", false);
                }
                createEmptyRow();

                append(true, connectionHandler.getProject().getName(), false);
                append(false, "/", false);
                ConnectionBundle connectionBundle = connectionHandler.getConnectionBundle();
                append(false, connectionHandler.getName(), false);

                ConnectionPool connectionPool = connectionHandler.getConnectionPool();
                append(true, "Pool size: ", "-2", null, false);
                append(false, String.valueOf(connectionPool.getSize()), false);
                append(false, " (", false);
                append(false, "peak&nbsp;" + connectionPool.getPeakPoolSize(), false);
                append(false, ")", false);
            }
        }.getToolTip();
    }



    /*********************************************************
     *                   NavigationItem                      *
     *********************************************************/
    public void navigate(boolean requestFocus) {
        DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(getProject());
        browserManager.navigateToElement(this, requestFocus);
    }
    public boolean canNavigate() {return true;}
    public boolean canNavigateToSource() {return false;}

    public String getName() {
        return getPresentableText();
    }

    public ItemPresentation getPresentation() {
        return this;
    }

    /*********************************************************
     *                   NavigationItem                      *
     *********************************************************/
    public String getLocationString() {
        return null;
    }

    public Icon getIcon(boolean open) {
        return getIcon(0);
    }

    /*********************************************************
     *                 Lookup utilities                      *
     *********************************************************/


    public DBObject getObject(DatabaseObjectIdentifier objectIdentifier) {
        DBObject object = null;
        for (int i=0; i<objectIdentifier.getObjectTypes().length; i++){
            DBObjectType objectType = objectIdentifier.getObjectTypes()[i];
            String objectName = objectIdentifier.getObjectNames()[i];
            if (object == null) {
                object = getObject(objectType, objectName);
            } else {
                object = object.getChildObject(objectType, objectName, true);
            }
            if (object == null) break;
        }
        return object;
    }

    public DBObject getObject(DBObjectType objectType, String name) {
        return getObject(objectType, name, 0);
    }

    public DBObject getObject(DBObjectType objectType, String name, int overload) {
        if (objectType == DBObjectType.SCHEMA) return getSchema(name);
        if (objectType == DBObjectType.USER) return getUser(name);
        if (objectType == DBObjectType.ROLE) return getRole(name);
        if (objectType == DBObjectType.CHARSET) return getCharset(name);
        if (objectType == DBObjectType.SYSTEM_PRIVILEGE) return getSystemPrivilege(name);
        for (DBSchema schema : getSchemas()) {
            if (schema.isPublicSchema() && objectType.isSchemaObject()) {
                DBObject childObject = schema.getChildObject(objectType, name, overload, true);
                if (childObject != null) {
                    return childObject;
                }
            }
        }
        return null;
    }

    private Filter<DBObjectType> getConnectionObjectTypeFilter() {
        return connectionHandler.getSettings().getFilterSettings().getObjectTypeFilterSettings().getTypeFilter();
    }

    public void lookupObjectsOfType(LookupConsumer consumer, DBObjectType objectType) throws ConsumerStoppedException {
        if (getConnectionObjectTypeFilter().accepts(objectType)) {
            if (objectType == DBObjectType.SCHEMA) consumer.consume(getSchemas()); else
            if (objectType == DBObjectType.USER) consumer.consume(getUsers()); else
            if (objectType == DBObjectType.ROLE) consumer.consume(getRoles()); else
            if (objectType == DBObjectType.CHARSET) consumer.consume(getCharsets());
            if (objectType == DBObjectType.SYSTEM_PRIVILEGE) consumer.consume(getSystemPrivileges());
        }
    }

    public void lookupChildObjectsOfType(LookupConsumer consumer, DBObject parentObject, DBObjectType objectType, ObjectTypeFilter filter, DBSchema currentSchema) throws ConsumerStoppedException {
        if (getConnectionObjectTypeFilter().accepts(objectType)) {
            if (parentObject != null && currentSchema != null) {
                if (parentObject instanceof DBSchema) {
                    DBSchema schema = (DBSchema) parentObject;
                    if (objectType.isGeneric()) {
                        Set<DBObjectType> concreteTypes = objectType.getInheritingTypes();
                        for (DBObjectType concreteType : concreteTypes) {
                            consumer.check();
                            if (filter.acceptsObject(schema, currentSchema, concreteType)) {
                                consumer.consume(schema.getChildObjects(concreteType));
                            }
                        }
                    } else {
                        if (filter.acceptsObject(schema, currentSchema, objectType)) {
                            consumer.consume(schema.getChildObjects(objectType));
                        }
                    }

                    boolean synonymsSupported = DatabaseCompatibilityInterface.getInstance(parentObject).supportsObjectType(DBObjectType.SYNONYM.getTypeId());
                    if (synonymsSupported && filter.acceptsObject(schema, currentSchema, DBObjectType.SYNONYM)) {
                        for (DBSynonym synonym : schema.getSynonyms()) {
                            consumer.check();
                            DBObject underlyingObject = synonym.getUnderlyingObject();
                            if (underlyingObject != null && underlyingObject.isOfType(objectType)) {
                                consumer.consume(synonym);
                            }
                        }
                    }
                } else {
                    if (objectType.isGeneric()) {
                        Set<DBObjectType> concreteTypes = objectType.getInheritingTypes();
                        for (DBObjectType concreteType : concreteTypes) {
                            consumer.check();
                            if (filter.acceptsRootObject(objectType)) {
                                consumer.consume(parentObject.getChildObjects(concreteType));
                            }
                        }
                    } else {
                        if (filter.acceptsRootObject(objectType)) {
                            consumer.consume(parentObject.getChildObjects(objectType));
                        }
                    }
                }
            }
        }
    }

    public void refreshObjectsStatus(final DBSchemaObject requester) {
        if (DatabaseFeature.OBJECT_INVALIDATION.isSupported(connectionHandler)) {
            new BackgroundTask(getProject(), "Updating objects status", true) {
                public void execute(@NotNull ProgressIndicator progressIndicator) {
                    try {
                        List<DBSchema> schemas = requester == null ? getSchemas() : requester.getReferencingSchemas();

                        int size = schemas.size();
                        for (int i=0; i<size; i++) {
                            DBSchema schema = schemas.get(i);
                            progressIndicator.setText("Updating object status in schema " + schema.getName() + "... ");
                            progressIndicator.setFraction(CommonUtil.getProgressPercentage(i, size));
                            schema.refreshObjectsStatus();
                        }
                    } catch (SQLException e) {
                        MessageUtil.showErrorDialog(getProject(), "Object Dependencies Refresh", "Could not refresh dependencies", e);
                    }
                }

            }.start();
        }
    }

    public DBObjectListContainer getObjectListContainer() {
        return objectLists;
    }

    public Project getProject() {
        return connectionHandler == null ? null : connectionHandler.getProject();
    }

    public GenericDatabaseElement getUndisposedElement() {
        return this;
    }

    public DynamicContent getDynamicContent(DynamicContentType dynamicContentType) {
        if(dynamicContentType instanceof DBObjectType) {
            DBObjectType objectType = (DBObjectType) dynamicContentType;
            DynamicContent dynamicContent = objectLists.getObjectList(objectType);
            if (dynamicContent == null) dynamicContent = objectLists.getHiddenObjectList(objectType);
            return dynamicContent;
        }

        if (dynamicContentType instanceof DBObjectRelationType) {
            DBObjectRelationType objectRelationType = (DBObjectRelationType) dynamicContentType;
            return objectRelationLists.getObjectRelationList(objectRelationType);
        }

        return null;
    }

    public boolean isDisposed() {
        return isDisposed;
    }

    public void initTreeElement() {}

    @Override
    public String toString() {
        return getConnectionHandler().getName();
    }

    public void dispose() {
        if (!isDisposed) {
            isDisposed = true;
            EventManager.unsubscribe(dataDefinitionChangeListener);
            DisposerUtil.dispose(objectLists);
            DisposerUtil.dispose(objectRelationLists);
            CollectionUtil.clearCollection(visibleTreeChildren);
            CollectionUtil.clearCollection(allPossibleTreeChildren);
            cachedDataTypes.clear();
            treeParent = null;
            connectionHandler = null;
        }
    }

    /*********************************************************
     *                         Loaders                       *
     *********************************************************/
    private static final DynamicContentLoader<DBSchema> SCHEMAS_LOADER = new DynamicContentResultSetLoader<DBSchema>() {
        public ResultSet createResultSet(DynamicContent<DBSchema> dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            return metadataInterface.loadSchemas(connection);
        }

        public DBSchema createElement(DynamicContent<DBSchema> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            return new DBSchemaImpl(dynamicContent.getConnectionHandler(), resultSet);
        }
    };

    private static final DynamicContentLoader<DBUser> USERS_LOADER = new DynamicContentResultSetLoader<DBUser>() {
        public ResultSet createResultSet(DynamicContent<DBUser> dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            return metadataInterface.loadUsers(connection);
        }

        public DBUser createElement(DynamicContent<DBUser> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            return new DBUserImpl(dynamicContent.getConnectionHandler(), resultSet);
        }
    };

    private static final DynamicContentLoader<DBRole> ROLES_LOADER = new DynamicContentResultSetLoader<DBRole>() {
        public ResultSet createResultSet(DynamicContent<DBRole> dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            return metadataInterface.loadRoles(connection);
        }

        public DBRole createElement(DynamicContent<DBRole> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            return new DBRoleImpl(dynamicContent.getConnectionHandler(), resultSet);
        }
    };


    private static final DynamicContentLoader<DBSystemPrivilege> SYSTEM_PRIVILEGES_LOADER = new DynamicContentResultSetLoader<DBSystemPrivilege>() {
        public ResultSet createResultSet(DynamicContent<DBSystemPrivilege> dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            return metadataInterface.loadSystemPrivileges(connection);
        }

        public DBSystemPrivilege createElement(DynamicContent<DBSystemPrivilege> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            return new DBSystemPrivilegeImpl(dynamicContent.getConnectionHandler(), resultSet);
        }
    };

    private static final DynamicContentLoader<DBObjectPrivilege> OBJECT_PRIVILEGES_LOADER = new DynamicContentResultSetLoader<DBObjectPrivilege>() {
        public ResultSet createResultSet(DynamicContent<DBObjectPrivilege> dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            return metadataInterface.loadObjectPrivileges(connection);
        }

        public DBObjectPrivilege createElement(DynamicContent<DBObjectPrivilege> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            return new DBObjectPrivilegeImpl(dynamicContent.getConnectionHandler(), resultSet);
        }
    };

    private static final DynamicContentLoader<DBCharset> CHARSETS_LOADER = new DynamicContentResultSetLoader<DBCharset>() {
        public ResultSet createResultSet(DynamicContent<DBCharset> dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            return metadataInterface.loadCharsets(connection);
        }

        public DBCharset createElement(DynamicContent<DBCharset> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            return new DBCharsetImpl(dynamicContent.getConnectionHandler(), resultSet);
        }
    };

    /*********************************************************
     *                    Relation loaders                   *
     *********************************************************/
    private static final DynamicContentLoader USER_ROLE_RELATION_LOADER = new DynamicContentResultSetLoader() {
        public ResultSet createResultSet(DynamicContent dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            return metadataInterface.loadAllUserRoles(connection);
        }

        public DynamicContentElement createElement(DynamicContent dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            String userName = resultSet.getString("USER_NAME");

            DBObjectBundle objectBundle = (DBObjectBundle) dynamicContent.getParent();
            DBUser user = objectBundle.getUser(userName);
            if (user != null) {
                DBGrantedRole role = new DBGrantedRoleImpl(user, resultSet);
                return new DBUserRoleRelation(user, role);
            }
            return null;
        }
    };

    private static final DynamicContentLoader USER_PRIVILEGE_RELATION_LOADER = new DynamicContentResultSetLoader() {
        public ResultSet createResultSet(DynamicContent dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            return metadataInterface.loadAllUserPrivileges(connection);
        }

        public DynamicContentElement createElement(DynamicContent dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            String userName = resultSet.getString("USER_NAME");

            DBObjectBundle objectBundle = (DBObjectBundle) dynamicContent.getParent();
            DBUser user = objectBundle.getUser(userName);
            if (user != null) {
                DBGrantedPrivilege privilege = new DBGrantedPrivilegeImpl(user, resultSet);
                return new DBUserPrivilegeRelation(user, privilege);
            }
            return null;
        }
    };

    private static final DynamicContentLoader ROLE_ROLE_RELATION_LOADER = new DynamicContentResultSetLoader() {
        public ResultSet createResultSet(DynamicContent dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            return metadataInterface.loadAllRoleRoles(connection);
        }

        public DynamicContentElement createElement(DynamicContent dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            String roleName = resultSet.getString("ROLE_NAME");

            DBObjectBundle objectBundle = (DBObjectBundle) dynamicContent.getParent();
            DBRole role = objectBundle.getRole(roleName);
            if (role != null) {
                DBGrantedRole grantedRole = new DBGrantedRoleImpl(role, resultSet);
                return new DBRoleRoleRelation(role, grantedRole);
            }
            return null;
        }
    };

    private static final DynamicContentLoader ROLE_PRIVILEGE_RELATION_LOADER = new DynamicContentResultSetLoader() {
        public ResultSet createResultSet(DynamicContent dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            return metadataInterface.loadAllRolePrivileges(connection);
        }

        public DynamicContentElement createElement(DynamicContent dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            String userName = resultSet.getString("ROLE_NAME");

            DBObjectBundle objectBundle = (DBObjectBundle) dynamicContent.getParent();
            DBRole role = objectBundle.getRole(userName);
            if (role != null) {
                DBGrantedPrivilege privilege = new DBGrantedPrivilegeImpl(role, resultSet);
                return new DBRolePrivilegeRelation(role, privilege);
            }
            return null;
        }
    };
}
