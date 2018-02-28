package com.dci.intellij.dbn.object.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.browser.DatabaseBrowserUtils;
import com.dci.intellij.dbn.browser.model.BrowserTreeChangeListener;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.ui.HtmlToolTipBuilder;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentElement;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoader;
import com.dci.intellij.dbn.common.content.loader.DynamicContentResultSetLoader;
import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionUtil;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.object.DBArgument;
import com.dci.intellij.dbn.object.DBCluster;
import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBConstraint;
import com.dci.intellij.dbn.object.DBDatabaseLink;
import com.dci.intellij.dbn.object.DBDatabaseTrigger;
import com.dci.intellij.dbn.object.DBDataset;
import com.dci.intellij.dbn.object.DBDatasetTrigger;
import com.dci.intellij.dbn.object.DBDimension;
import com.dci.intellij.dbn.object.DBFunction;
import com.dci.intellij.dbn.object.DBIndex;
import com.dci.intellij.dbn.object.DBMaterializedView;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBNestedTable;
import com.dci.intellij.dbn.object.DBPackage;
import com.dci.intellij.dbn.object.DBPackageFunction;
import com.dci.intellij.dbn.object.DBPackageProcedure;
import com.dci.intellij.dbn.object.DBPackageType;
import com.dci.intellij.dbn.object.DBProcedure;
import com.dci.intellij.dbn.object.DBProgram;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.DBSequence;
import com.dci.intellij.dbn.object.DBSynonym;
import com.dci.intellij.dbn.object.DBTable;
import com.dci.intellij.dbn.object.DBType;
import com.dci.intellij.dbn.object.DBTypeAttribute;
import com.dci.intellij.dbn.object.DBTypeFunction;
import com.dci.intellij.dbn.object.DBTypeProcedure;
import com.dci.intellij.dbn.object.DBUser;
import com.dci.intellij.dbn.object.DBView;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectImpl;
import com.dci.intellij.dbn.object.common.DBObjectRelationType;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.common.list.DBObjectListVisitor;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationList;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationListImpl;
import com.dci.intellij.dbn.object.common.list.DBObjectRelation;
import com.dci.intellij.dbn.object.common.list.DBObjectRelationListContainer;
import com.dci.intellij.dbn.object.common.property.DBObjectProperty;
import com.dci.intellij.dbn.object.common.status.DBObjectStatus;
import com.dci.intellij.dbn.object.common.status.DBObjectStatusHolder;

public class DBSchemaImpl extends DBObjectImpl implements DBSchema {
    DBObjectList<DBTable> tables;
    DBObjectList<DBView> views;
    DBObjectList<DBMaterializedView> materializedViews;
    DBObjectList<DBSynonym> synonyms;
    DBObjectList<DBSequence> sequences;
    DBObjectList<DBProcedure> procedures;
    DBObjectList<DBFunction> functions;
    DBObjectList<DBPackage> packages;
    DBObjectList<DBType> types;
    DBObjectList<DBDatabaseTrigger> databaseTriggers;
    DBObjectList<DBDimension> dimensions;
    DBObjectList<DBCluster> clusters;
    DBObjectList<DBDatabaseLink> databaseLinks;

    boolean isUserSchema;
    boolean isPublicSchema;
    boolean isSystemSchema;
    boolean isEmptySchema;

    public DBSchemaImpl(ConnectionHandler connectionHandler, ResultSet resultSet) throws SQLException {
        super(connectionHandler.getObjectBundle(), resultSet);
    }

    @Override
    protected void initObject(ResultSet resultSet) throws SQLException {
        name = resultSet.getString("SCHEMA_NAME");
        isPublicSchema = resultSet.getString("IS_PUBLIC").equals("Y");
        isSystemSchema = resultSet.getString("IS_SYSTEM").equals("Y");
        isEmptySchema = resultSet.getString("IS_EMPTY").equals("Y");
        isUserSchema = getName().equalsIgnoreCase(getConnectionHandler().getUserName());
    }

    @Override
    protected void initLists() {
        DBObjectListContainer ol = initChildObjects();
        DBObjectRelationListContainer orl = initChildObjectRelations();

        tables = ol.createObjectList(DBObjectType.TABLE, this, TABLES_LOADER, true, false);
        views = ol.createObjectList(DBObjectType.VIEW, this, VIEWS_LOADER, true, false);
        materializedViews = ol.createObjectList(DBObjectType.MATERIALIZED_VIEW, this, MATERIALIZED_VIEWS_LOADER, true, false);
        synonyms = ol.createObjectList(DBObjectType.SYNONYM, this, SYNONYMS_LOADER, true, false);
        sequences = ol.createObjectList(DBObjectType.SEQUENCE, this, SEQUENCES_LOADER, true, false);
        procedures = ol.createObjectList(DBObjectType.PROCEDURE, this, PROCEDURES_LOADER, true, false);
        functions = ol.createObjectList(DBObjectType.FUNCTION, this, FUNCTIONS_LOADER, true, false);
        packages = ol.createObjectList(DBObjectType.PACKAGE, this, PACKAGES_LOADER, true, false);
        types = ol.createObjectList(DBObjectType.TYPE, this, TYPES_LOADER, true, false);
        databaseTriggers = ol.createObjectList(DBObjectType.DATABASE_TRIGGER, this, DATABASE_TRIGGERS_LOADER, true, false);
        dimensions = ol.createObjectList(DBObjectType.DIMENSION, this, DIMENSIONS_LOADER, true, false);
        clusters = ol.createObjectList(DBObjectType.CLUSTER, this, CLUSTERS_LOADER, true, false);
        databaseLinks = ol.createObjectList(DBObjectType.DBLINK, this, DATABASE_LINKS_LOADER, true, false);

        DBObjectList constraints = ol.createObjectList(DBObjectType.CONSTRAINT, this, CONSTRAINTS_LOADER, true, false);
        DBObjectList indexes = ol.createObjectList(DBObjectType.INDEX, this, INDEXES_LOADER, true, false);
        DBObjectList columns = ol.createObjectList(DBObjectType.COLUMN, this, COLUMNS_LOADER, false, true);
        ol.createObjectList(DBObjectType.DATASET_TRIGGER, this, DATASET_TRIGGERS_LOADER, true, false);
        ol.createObjectList(DBObjectType.NESTED_TABLE, this, ALL_NESTED_TABLES_LOADER, true, false);
        ol.createObjectList(DBObjectType.PACKAGE_FUNCTION, this, ALL_PACKAGE_FUNCTIONS_LOADER, false, true);
        ol.createObjectList(DBObjectType.PACKAGE_PROCEDURE, this, ALL_PACKAGE_PROCEDURES_LOADER, false, true);
        ol.createObjectList(DBObjectType.PACKAGE_TYPE, this, ALL_PACKAGE_TYPES_LOADER, false, true);
        ol.createObjectList(DBObjectType.TYPE_ATTRIBUTE, this, ALL_TYPE_ATTRIBUTES_LOADER, false, true);
        ol.createObjectList(DBObjectType.TYPE_FUNCTION, this, ALL_TYPE_FUNCTIONS_LOADER, false, true);
        ol.createObjectList(DBObjectType.TYPE_PROCEDURE, this, ALL_TYPE_PROCEDURES_LOADER, false, true);
        ol.createObjectList(DBObjectType.ARGUMENT, this, ALL_ARGUMENTS_LOADER, false, true);

        //ol.createHiddenObjectList(DBObjectType.TYPE_METHOD, this, TYPE_METHODS_LOADER);

        orl.createObjectRelationList(
                DBObjectRelationType.CONSTRAINT_COLUMN, this,
                "Constraint relations",
                CONSTRAINT_COLUMN_RELATION_LOADER,
                constraints,
                columns);

        orl.createObjectRelationList(
                DBObjectRelationType.INDEX_COLUMN, this,
                "Index relations",
                INDEX_COLUMN_RELATION_LOADER,
                indexes,
                columns);
    }

    @Override
    public void initProperties() {}

    @Override
    public DBUser getOwner() {
        return getObjectBundle().getUser(name);
    }

    public DBObjectType getObjectType() {
        return DBObjectType.SCHEMA;
    }

    public boolean isPublicSchema() {
        return isPublicSchema;
    }

    public boolean isUserSchema() {
        return isUserSchema;
    }

    public boolean isSystemSchema() {
        return isSystemSchema;
    }

    @Override
    public boolean isEmptySchema() {
        return isEmptySchema;
    }

    @Override
    public DBObject getDefaultNavigationObject() {
        return getOwner();
    }

    public DBObject getChildObject(DBObjectType objectType, String name, int overload, boolean lookupHidden) {
        if (objectType.isSchemaObject()) {
            DBObject object = super.getChildObject(objectType, name, overload, lookupHidden);
            if (object == null) {
                DBSynonym synonym = (DBSynonym) super.getChildObject(DBObjectType.SYNONYM, name, overload, lookupHidden);
                if (synonym != null) {
                    DBObject underlyingObject = synonym.getUnderlyingObject();
                    if (underlyingObject != null && underlyingObject.isOfType(objectType)) {
                        return synonym;
                    }
                }
            } else {
                return object;
            }
        }
        return null;
    }

    @Override
    protected List<DBObjectNavigationList> createNavigationLists() {
        DBUser user = getOwner();
        if (user != null) {
            List<DBObjectNavigationList> objectNavigationLists = new ArrayList<DBObjectNavigationList>();
            objectNavigationLists.add(new DBObjectNavigationListImpl("User", user));
            return objectNavigationLists;
        }
        return null;
    }

    private class ConstraintColumnComparator implements Comparator {
        private DBConstraint constraint;
        ConstraintColumnComparator(DBConstraint constraint) {
            this.constraint = constraint;
        }
        public int compare(Object o1, Object o2) {
            DBColumn column1 = (DBColumn) o1;
            DBColumn column2 = (DBColumn) o2;
            return column1.getConstraintPosition(constraint)-
                    column2.getConstraintPosition(constraint);
        }
    }

    public List<DBTable> getTables() {
        return tables.getObjects();
    }

    public List<DBView> getViews() {
        return views.getObjects();
    }

    public List<DBMaterializedView> getMaterializedViews() {
        return materializedViews.getObjects();
    }

    public List<DBIndex> getIndexes() {
        return initChildObjects().getObjectList(DBObjectType.INDEX).getObjects();
    }

    public List<DBSynonym> getSynonyms() {
        return synonyms.getObjects();
    }

    public List<DBSequence> getSequences() {
        return sequences.getObjects();
    }

    public List<DBProcedure> getProcedures() {
        return procedures.getObjects();
    }

    public List<DBFunction> getFunctions() {
        return functions.getObjects();
    }

    public List<DBPackage> getPackages() {
        return packages.getObjects();
    }

    public List<DBDatasetTrigger> getDatasetTriggers() {
        return initChildObjects().getObjectList(DBObjectType.DATASET_TRIGGER).getObjects();
    }

    public List<DBDatabaseTrigger> getDatabaseTriggers() {
        return initChildObjects().getObjectList(DBObjectType.DATABASE_TRIGGER).getObjects();
    }

    public List<DBType> getTypes() {
        return types.getObjects();
    }

    public List<DBDimension> getDimensions() {
        return dimensions.getObjects();
    }

    public List<DBCluster> getClusters() {
        return clusters.getObjects();
    }

    public List<DBDatabaseLink> getDatabaseLinks() {
        return databaseLinks.getObjects();
    }


    public DBTable getTable(String name) {
        return tables.getObject(name);
    }

    public DBView getView(String name) {
        return views.getObject(name);
    }

    public DBMaterializedView getMaterializedView(String name) {
        return materializedViews.getObject(name);
    }

    public DBIndex getIndex(String name) {
        return (DBIndex) initChildObjects().getObjectList(DBObjectType.INDEX).getObject(name);
    }

    public DBCluster getCluster(String name) {
        return clusters.getObject(name);
    }

    public DBDatabaseLink getDatabaseLink(String name) {
        return databaseLinks.getObject(name);
    }

    public List<DBDataset> getDatasets() {
        List<DBDataset> datasets = new ArrayList<DBDataset>();
        datasets.addAll(getTables());
        datasets.addAll(getViews());
        datasets.addAll(getMaterializedViews());
        return datasets;
    }


    public DBDataset getDataset(String name) {
        DBDataset dataset = getTable(name);
        if (dataset == null) {
            dataset = getView(name);
            if (dataset == null && DatabaseCompatibilityInterface.getInstance(this).supportsObjectType(DBObjectType.MATERIALIZED_VIEW.getTypeId())) {
                dataset = getMaterializedView(name);
            }
        }
        if (dataset == null) {
            //System.out.println("unknown dataset: " + getName() + "." + name);
        }
        return dataset;
    }

    @Nullable
    private <T extends DBSchemaObject> T getObjectFallbackOnSynonym(DBObjectList<T> objects, String name) {
        T object = objects.getObject(name);
        if (object == null && DatabaseCompatibilityInterface.getInstance(this).supportsObjectType(DBObjectType.SYNONYM.getTypeId())) {
            DBSynonym synonym = synonyms.getObject(name);
            if (synonym != null) {
                DBObject underlyingObject = synonym.getUnderlyingObject();
                if (underlyingObject != null) {
                    if (underlyingObject.getObjectType() == objects.getObjectType()) {
                        return (T) underlyingObject;
                    }
                }
            }
        } else {
            return object;
        }
        return null;
    }

    public DBType getType(String name) {
        return getObjectFallbackOnSynonym(types, name);
    }

    public DBPackage getPackage(String name) {
        return getObjectFallbackOnSynonym(packages, name);
    }

    public DBProcedure getProcedure(String name, int overload) {
        return overload > 0 ?
                procedures.getObject(name, overload) :
                getObjectFallbackOnSynonym(procedures, name);
    }

    public DBFunction getFunction(String name, int overload) {
        return overload > 0 ?
                functions.getObject(name, overload) :
                getObjectFallbackOnSynonym(functions, name);
    }

    public DBProgram getProgram(String name) {
        DBProgram program = getPackage(name);
        if (program == null) program = getType(name);
        return program;
    }

    public DBMethod getMethod(String name, DBObjectType methodType, int overload) {
        if (methodType == null) {
            DBMethod method = getProcedure(name, overload);
            if (method == null) method = getFunction(name, overload);
            return method;
        } else if (methodType == DBObjectType.PROCEDURE) {
            return getProcedure(name, overload);
        } else if (methodType == DBObjectType.FUNCTION) {
            return getFunction(name, overload);
        }
        return null;
    }

    public DBMethod getMethod(String name, int overload) {
        return getMethod(name, null, overload);
    }

    @Override
    public boolean isParentOf(DBObject object) {
        if (object instanceof DBSchemaObject) {
            DBSchemaObject schemaObject = (DBSchemaObject) object;
            return schemaObject.getProperties().is(DBObjectProperty.SCHEMA_OBJECT) && this.equals(schemaObject.getSchema());

        }
        return false;
    }

    public synchronized void refreshObjectsStatus() throws SQLException {
        final Set<BrowserTreeNode> refreshNodes = resetObjectsStatus();
        Connection connection = null;
        ResultSet resultSet = null;
        ConnectionHandler connectionHandler = getConnectionHandler();
        if (connectionHandler != null) {
            try {
                connection = connectionHandler.getPoolConnection();
                DatabaseMetadataInterface metadataInterface = connectionHandler.getInterfaceProvider().getMetadataInterface();
                resultSet = metadataInterface.loadInvalidObjects(getName(), connection);
                while (resultSet != null && resultSet.next()) {
                    String objectName = resultSet.getString("OBJECT_NAME");
                    DBSchemaObject schemaObject = (DBSchemaObject) getChildObjectNoLoad(objectName);
                    if (schemaObject != null && schemaObject.getStatus().has(DBObjectStatus.VALID)) {
                        DBObjectStatusHolder objectStatus = schemaObject.getStatus();
                        boolean statusChanged;

                        if (schemaObject.getContentType().isBundle()) {
                            String objectType = resultSet.getString("OBJECT_TYPE");
                            statusChanged = objectType.contains("BODY") ?
                                    objectStatus.set(DBContentType.CODE_BODY, DBObjectStatus.VALID, false) :
                                    objectStatus.set(DBContentType.CODE_SPEC, DBObjectStatus.VALID, false);
                        }
                        else {
                            statusChanged = objectStatus.set(DBObjectStatus.VALID, false);
                        }
                        if (statusChanged) {
                            refreshNodes.add(schemaObject.getTreeParent());
                        }
                    }
                }

                resultSet = metadataInterface.loadDebugObjects(getName(), connection);
                while (resultSet != null && resultSet.next()) {
                    String objectName = resultSet.getString("OBJECT_NAME");
                    DBSchemaObject schemaObject = (DBSchemaObject) getChildObjectNoLoad(objectName);
                    if (schemaObject != null && schemaObject.getStatus().has(DBObjectStatus.DEBUG)) {
                        DBObjectStatusHolder objectStatus = schemaObject.getStatus();
                        boolean statusChanged;

                        if (schemaObject.getContentType().isBundle()) {
                            String objectType = resultSet.getString("OBJECT_TYPE");
                            statusChanged = objectType.contains("BODY") ?
                                    objectStatus.set(DBContentType.CODE_BODY, DBObjectStatus.DEBUG, true) :
                                    objectStatus.set(DBContentType.CODE_SPEC, DBObjectStatus.DEBUG, true);
                        }
                        else {
                            statusChanged = objectStatus.set(DBObjectStatus.DEBUG, true);
                        }
                        if (statusChanged) {
                            refreshNodes.add(schemaObject.getTreeParent());
                        }
                    }
                }

            } finally {
                ConnectionUtil.closeResultSet(resultSet);
                connectionHandler.freePoolConnection(connection);
            }

            for (BrowserTreeNode treeNode : refreshNodes) {
                EventManager.notify(getProject(), BrowserTreeChangeListener.TOPIC).nodeChanged(treeNode, TreeEventType.NODES_CHANGED);
            }
        }

    }

    private Set<BrowserTreeNode> resetObjectsStatus() {
        ObjectStatusUpdater updater = new ObjectStatusUpdater();
        initChildObjects().visitLists(updater, true);
        return updater.getRefreshNodes();
    }

    class ObjectStatusUpdater implements DBObjectListVisitor {
        private Set<BrowserTreeNode> refreshNodes = new HashSet<BrowserTreeNode>();

        public void visitObjectList(DBObjectList<DBObject> objectList) {
            if (objectList.isLoaded() && !objectList.isDirty() && !objectList.isLoading()) {
                List<DBObject> objects = objectList.getObjects();
                for (DBObject object : objects) {
                    if (object instanceof DBSchemaObject) {
                        DBSchemaObject schemaObject = (DBSchemaObject) object;
                        DBObjectStatusHolder objectStatus = schemaObject.getStatus();
                        if (objectStatus.has(DBObjectStatus.VALID)) {
                            if (objectStatus.set(DBObjectStatus.VALID, true)) {
                                refreshNodes.add(object.getTreeParent());
                            }
                        }
                        if (objectStatus.has(DBObjectStatus.DEBUG)) {
                            if (objectStatus.set(DBObjectStatus.DEBUG, false)) {
                                refreshNodes.add(object.getTreeParent());
                            }
                        }
                    } else {
                        break;
                    }
                }
            }
        }


        public Set<BrowserTreeNode> getRefreshNodes() {
            return refreshNodes;
        }
    }

    public void buildToolTip(HtmlToolTipBuilder ttb) {
        ttb.append(true, getObjectType().getName(), true);
        ttb.createEmptyRow();
        super.buildToolTip(ttb);
    }

    /*********************************************************
     *                     TreeElement                       *
     *********************************************************/
    @NotNull
    public List<BrowserTreeNode> buildAllPossibleTreeChildren() {
        return DatabaseBrowserUtils.createList(
                tables,
                views,
                materializedViews,
                synonyms,
                sequences,
                procedures,
                functions,
                packages,
                types,
                databaseTriggers,
                dimensions,
                clusters,
                databaseLinks);
    }

    /*********************************************************
     *                      Relation builders                *
     *********************************************************/
    private static final DynamicContentLoader CONSTRAINT_COLUMN_RELATION_LOADER = new DynamicContentResultSetLoader() {
        public ResultSet createResultSet(DynamicContent dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            DBSchema schema = (DBSchema) dynamicContent.getParent();
            return metadataInterface.loadAllConstraintRelations(schema.getName(), connection);
        }

        public DynamicContentElement createElement(DynamicContent dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            String datasetName = resultSet.getString("DATASET_NAME");
            String columnName = resultSet.getString("COLUMN_NAME");
            String constraintName = resultSet.getString("CONSTRAINT_NAME");
            int position = resultSet.getInt("POSITION");

/*
            DBSchema schema = (DBSchema) dynamicContent.getParent();
            DBObjectList<DBConstraint> constraints = schema.getObjectLists().getObjectList(DBObjectType.CONSTRAINT);
            DBConstraint constraint = constraints.getObject(constraintName, datasetName);

            if (constraint != null) {
                DBObjectList<DBColumn> columns = schema.getObjectLists().getHiddenObjectList(DBObjectType.COLUMN);
                DBColumn column = columns.getObject(columnName, datasetName);

                if (column != null) {
                    return new DBConstraintColumnRelation(constraint, column, position);
                }
            }
*/
            DBDataset dataset = (DBDataset) loaderCache.getObject(datasetName);
            if (dataset == null) {
                DBSchema schema = (DBSchema) dynamicContent.getParent();
                dataset = schema.getDataset(datasetName);
                loaderCache.setObject(datasetName, dataset);
            }

            if (dataset != null) {
                DBConstraint constraint = dataset.getConstraint(constraintName);
                DBColumn column = dataset.getColumn(columnName);
                if (column != null && constraint != null) {
                    return new DBConstraintColumnRelation(constraint, column, position);
                }
            }

            return null;
        }
    };

    private static final DynamicContentLoader INDEX_COLUMN_RELATION_LOADER = new DynamicContentResultSetLoader() {
        public ResultSet createResultSet(DynamicContent dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            DBSchema schema = (DBSchema) dynamicContent.getParent();
            return metadataInterface.loadAllIndexRelations(schema.getName(), connection);
        }

        public DBObjectRelation createElement(DynamicContent dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            String tableName = resultSet.getString("TABLE_NAME");
            String columnName = resultSet.getString("COLUMN_NAME");
            String indexName = resultSet.getString("INDEX_NAME");

            /*DBSchema schema = (DBSchema) dynamicContent.getParent();
            DBObjectList<DBIndex> indexes = schema.getObjectLists().getObjectList(DBObjectType.INDEX);
            DBIndex index = indexes.getObject(indexName, tableName);

            if (index != null) {
                DBObjectList<DBColumn> columns = schema.getObjectLists().getHiddenObjectList(DBObjectType.COLUMN);
                DBColumn column = columns.getObject(columnName, tableName);

                if (column != null) {
                    return new DBIndexColumnRelation(index, column);
                }
            }*/

            DBTable table = (DBTable) loaderCache.getObject(tableName);
            if (table == null) {
                DBSchema schema = (DBSchema) dynamicContent.getParent();
                table = schema.getTable(tableName);
                loaderCache.setObject(tableName, table);
            }

            if (table != null) {
                DBIndex index = table.getIndex(indexName);
                DBColumn column = table.getColumn(columnName);

                if (column != null && index != null) {
                    return new DBIndexColumnRelation(index, column);
                }
            }
            return null;
        }
    };

    /*********************************************************
     *                         Loaders                       *
     *********************************************************/

    private static final DynamicContentLoader TABLES_LOADER = new DynamicContentResultSetLoader<DBTable>() {
        public ResultSet createResultSet(DynamicContent<DBTable> dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            DBSchema schema = (DBSchema) dynamicContent.getParent();
            return metadataInterface.loadTables(schema.getName(), connection);
        }

        public DBTable createElement(DynamicContent<DBTable> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            DBSchema schema = (DBSchema) dynamicContent.getParent();
            return new DBTableImpl(schema, resultSet);
        }
    };

    private static final DynamicContentLoader VIEWS_LOADER = new DynamicContentResultSetLoader<DBView>(){
        public ResultSet createResultSet(DynamicContent<DBView> dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            DBSchema schema = (DBSchema) dynamicContent.getParent();
            return metadataInterface.loadViews(schema.getName(), connection);
        }

        public DBView createElement(DynamicContent<DBView> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            DBSchema schema = (DBSchema) dynamicContent.getParent();
            return new DBViewImpl(schema, resultSet);
        }
    };

    private static final DynamicContentLoader MATERIALIZED_VIEWS_LOADER = new DynamicContentResultSetLoader<DBMaterializedView>(){
        public ResultSet createResultSet(DynamicContent<DBMaterializedView> dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            DBSchema schema = (DBSchema) dynamicContent.getParent();
            return metadataInterface.loadMaterializedViews(schema.getName(), connection);
        }

        public DBMaterializedView createElement(DynamicContent<DBMaterializedView> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            DBSchema schema = (DBSchema) dynamicContent.getParent();
            return new DBMaterializedViewImpl(schema, resultSet);
        }
    };

    private static final DynamicContentLoader SYNONYMS_LOADER = new DynamicContentResultSetLoader<DBSynonym>() {
        public ResultSet createResultSet(DynamicContent<DBSynonym> dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            DBSchema schema = (DBSchema) dynamicContent.getParent();
            return metadataInterface.loadSynonyms(schema.getName(), connection);
        }

        public DBSynonym createElement(DynamicContent<DBSynonym> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            DBSchema schema = (DBSchema) dynamicContent.getParent();
            return new DBSynonymImpl(schema, resultSet);
        }
    };

    private static final DynamicContentLoader SEQUENCES_LOADER = new DynamicContentResultSetLoader<DBSequence>() {
        public ResultSet createResultSet(DynamicContent<DBSequence> dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            DBSchema schema = (DBSchema) dynamicContent.getParent();
            return metadataInterface.loadSequences(schema.getName(), connection);
        }

        public DBSequence createElement(DynamicContent<DBSequence> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            DBSchema schema = (DBSchema) dynamicContent.getParent();
            return new DBSequenceImpl(schema, resultSet);
        }
    };

    private static final DynamicContentLoader PROCEDURES_LOADER = new DynamicContentResultSetLoader<DBProcedure>() {
        public ResultSet createResultSet(DynamicContent<DBProcedure> dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            DBSchema schema = (DBSchema) dynamicContent.getParent();
            return metadataInterface.loadProcedures(schema.getName(), connection);
        }

        public DBProcedure createElement(DynamicContent<DBProcedure> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            DBSchema schema = (DBSchema) dynamicContent.getParent();
            return new DBProcedureImpl(schema, resultSet);
        }
    };

    private static final DynamicContentLoader FUNCTIONS_LOADER = new  DynamicContentResultSetLoader<DBFunction>() {
        public ResultSet createResultSet(DynamicContent<DBFunction> dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            DBSchema schema = (DBSchema) dynamicContent.getParent();
            return metadataInterface.loadFunctions(schema.getName(), connection);
        }
        public DBFunction createElement(DynamicContent<DBFunction> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            DBSchema schema = (DBSchema) dynamicContent.getParent();
            return new DBFunctionImpl(schema, resultSet);
        }
    };

    private static final DynamicContentLoader PACKAGES_LOADER = new  DynamicContentResultSetLoader<DBPackage>() {
        public ResultSet createResultSet(DynamicContent<DBPackage> dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            DBSchema schema = (DBSchema) dynamicContent.getParent();
            return metadataInterface.loadPackages(schema.getName(), connection);
        }

        public DBPackage createElement(DynamicContent<DBPackage> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            DBSchema schema = (DBSchema) dynamicContent.getParent();
            return new DBPackageImpl(schema, resultSet);
        }
    };

    private static final DynamicContentLoader TYPES_LOADER = new  DynamicContentResultSetLoader<DBType>() {
        public ResultSet createResultSet(DynamicContent<DBType> dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            DBSchema schema = (DBSchema) dynamicContent.getParent();
            return metadataInterface.loadTypes(schema.getName(), connection);
        }

        public DBType createElement(DynamicContent<DBType> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            DBSchema schema = (DBSchema) dynamicContent.getParent();
            return new DBTypeImpl(schema, resultSet);
        }
    };


    private static final DynamicContentLoader DATABASE_TRIGGERS_LOADER = new  DynamicContentResultSetLoader<DBDatabaseTrigger>() {
        public ResultSet createResultSet(DynamicContent<DBDatabaseTrigger> dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            DBSchema schema = (DBSchema) dynamicContent.getParent();
            return metadataInterface.loadDatabaseTriggers(schema.getName(), connection);
        }

        public DBDatabaseTrigger createElement(DynamicContent<DBDatabaseTrigger> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            DBSchema schema = (DBSchema) dynamicContent.getParent();
            return new DBDatabaseTriggerImpl(schema, resultSet);
        }
    };

    private static final DynamicContentLoader DIMENSIONS_LOADER = new  DynamicContentResultSetLoader<DBDimension>() {
        public ResultSet createResultSet(DynamicContent<DBDimension> dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            DBSchema schema = (DBSchema) dynamicContent.getParent();
            return metadataInterface.loadDimensions(schema.getName(), connection);
        }

        public DBDimension createElement(DynamicContent<DBDimension> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            DBSchema schema = (DBSchema) dynamicContent.getParent();
            return new DBDimensionImpl(schema, resultSet);
        }
    };

    private static final DynamicContentLoader CLUSTERS_LOADER = new  DynamicContentResultSetLoader<DBCluster>() {
        public ResultSet createResultSet(DynamicContent<DBCluster> dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            DBSchema schema = (DBSchema) dynamicContent.getParent();
            return metadataInterface.loadClusters(schema.getName(), connection);
        }

        public DBCluster createElement(DynamicContent<DBCluster> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            DBSchema schema = (DBSchema) dynamicContent.getParent();
            return new DBClusterImpl(schema, resultSet);
        }
    };

    private static final DynamicContentLoader DATABASE_LINKS_LOADER = new  DynamicContentResultSetLoader<DBDatabaseLink>() {
        public ResultSet createResultSet(DynamicContent dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            DBSchema schema = (DBSchema) dynamicContent.getParent();
            return metadataInterface.loadDatabaseLinks(schema.getName(), connection);
        }

        public DBDatabaseLink createElement(DynamicContent<DBDatabaseLink> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            DBSchema schema = (DBSchema) dynamicContent.getParent();
            return new DBDatabaseLinkImpl(schema, resultSet);
        }
    };


    private static final DynamicContentLoader COLUMNS_LOADER = new  DynamicContentResultSetLoader<DBColumn>() {
        public ResultSet createResultSet(DynamicContent<DBColumn> dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            DBSchema schema = (DBSchema) dynamicContent.getParent();
            return metadataInterface.loadAllColumns(schema.getName(), connection);
        }

        public DBColumn createElement(DynamicContent<DBColumn> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            String datasetName = resultSet.getString("DATASET_NAME");

            DBDataset dataset = (DBDataset) loaderCache.getObject(datasetName);
            if (dataset == null) {
                DBSchema schema = (DBSchema) dynamicContent.getParent();
                dataset = schema.getDataset(datasetName);
                loaderCache.setObject(datasetName, dataset);
            }

            // dataset may be null if cluster column!!
            return dataset == null ? null : new DBColumnImpl(dataset, resultSet);
        }
    };

    private static final DynamicContentLoader CONSTRAINTS_LOADER = new  DynamicContentResultSetLoader<DBConstraint>() {
        public ResultSet createResultSet(DynamicContent<DBConstraint> dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            DBSchema schema = (DBSchema) dynamicContent.getParent();
            return metadataInterface.loadAllConstraints(schema.getName(), connection);
        }

        public DBConstraint createElement(DynamicContent<DBConstraint> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            String datasetName = resultSet.getString("DATASET_NAME");

            DBDataset dataset = (DBDataset) loaderCache.getObject(datasetName);
            if (dataset == null) {
                DBSchema schema = (DBSchema) dynamicContent.getParent();
                dataset = schema.getDataset(datasetName);
                loaderCache.setObject(datasetName, dataset);
            }

            return dataset == null ? null : new DBConstraintImpl(dataset, resultSet);
        }
    };

    private static final DynamicContentLoader INDEXES_LOADER = new DynamicContentResultSetLoader<DBIndex>() {
        public ResultSet createResultSet(DynamicContent<DBIndex> dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            DBSchema schema = (DBSchema) dynamicContent.getParent();
            return metadataInterface.loadAllIndexes(schema.getName(), connection);
        }

        public DBIndex createElement(DynamicContent<DBIndex> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            String tableName = resultSet.getString("TABLE_NAME");

            DBTable table = (DBTable) loaderCache.getObject(tableName);
            if (table == null) {
                DBSchema schema = (DBSchema) dynamicContent.getParent();
                table = schema.getTable(tableName);
                loaderCache.setObject(tableName, table);
            }

            return table == null ? null : new DBIndexImpl(table, resultSet);
        }
    };

    private static final DynamicContentLoader DATASET_TRIGGERS_LOADER = new  DynamicContentResultSetLoader<DBDatasetTrigger>() {
        public ResultSet createResultSet(DynamicContent<DBDatasetTrigger> dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            DBSchema schema = (DBSchema) dynamicContent.getParent();
            return metadataInterface.loadAllDatasetTriggers(schema.getName(), connection);
        }

        public DBDatasetTrigger createElement(DynamicContent<DBDatasetTrigger> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            String datasetName = resultSet.getString("DATASET_NAME");
            DBDataset dataset = (DBDataset) loaderCache.getObject(datasetName);
            if (dataset == null) {
                DBSchema schema = (DBSchema) dynamicContent.getParent();
                dataset = schema.getDataset(datasetName);
                loaderCache.setObject(datasetName, dataset);
            }
            return new DBDatasetTriggerImpl(dataset, resultSet);
        }
    };

    private static final DynamicContentLoader ALL_NESTED_TABLES_LOADER = new  DynamicContentResultSetLoader<DBNestedTable>() {
        public ResultSet createResultSet(DynamicContent dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            DBSchema schema = (DBSchema) dynamicContent.getParent();
            return metadataInterface.loadAllNestedTables(schema.getName(), connection);
        }

        public DBNestedTable createElement(DynamicContent<DBNestedTable> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            String tableName = resultSet.getString("TABLE_NAME");
            DBTable table = (DBTable) loaderCache.getObject(tableName);
            if (table == null) {
                DBSchema schema = (DBSchema) dynamicContent.getParent();
                table = schema.getTable(tableName);
                loaderCache.setObject(tableName, table);
            }
            return new DBNestedTableImpl(table, resultSet);
        }
    };

    private static final DynamicContentLoader ALL_PACKAGE_FUNCTIONS_LOADER = new DynamicContentResultSetLoader<DBPackageFunction>() {
        public ResultSet createResultSet(DynamicContent dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            DBSchema schema = (DBSchema) dynamicContent.getParent();
            return metadataInterface.loadAllPackageFunctions(schema.getName(), connection);
        }

        public DBPackageFunction createElement(DynamicContent<DBPackageFunction> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            String packageName = resultSet.getString("PACKAGE_NAME");
            DBPackage packagee = (DBPackage) loaderCache.getObject(packageName);
            if (packagee == null) {
                DBSchema schema = (DBSchema) dynamicContent.getParent();
                packagee = schema.getPackage(packageName);
                loaderCache.setObject(packageName, packagee);
            }
            return new DBPackageFunctionImpl(packagee, resultSet);
        }
    };

    private static final DynamicContentLoader ALL_PACKAGE_PROCEDURES_LOADER = new DynamicContentResultSetLoader<DBPackageProcedure>() {
        public ResultSet createResultSet(DynamicContent dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            DBSchema schema = (DBSchema) dynamicContent.getParent();
            return metadataInterface.loadAllPackageProcedures(schema.getName(), connection);
        }

        public DBPackageProcedure createElement(DynamicContent<DBPackageProcedure> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            String packageName = resultSet.getString("PACKAGE_NAME");
            DBPackage packagee = (DBPackage) loaderCache.getObject(packageName);
            if (packagee == null) {
                DBSchema schema = (DBSchema) dynamicContent.getParent();
                packagee = schema.getPackage(packageName);
                loaderCache.setObject(packageName, packagee);
            }
            return new DBPackageProcedureImpl(packagee, resultSet);
        }
    };

    private static final DynamicContentLoader ALL_PACKAGE_TYPES_LOADER = new DynamicContentResultSetLoader<DBPackageType>() {
        public ResultSet createResultSet(DynamicContent<DBPackageType> dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            DBSchema schema = (DBSchema) dynamicContent.getParent();
            return metadataInterface.loadAllPackageTypes(schema.getName(), connection);
        }

        public DBPackageType createElement(DynamicContent<DBPackageType> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            String packageName = resultSet.getString("PACKAGE_NAME");
            DBPackage packagee = (DBPackage) loaderCache.getObject(packageName);
            if (packagee == null) {
                DBSchema schema = (DBSchema) dynamicContent.getParent();
                packagee = schema.getPackage(packageName);
                loaderCache.setObject(packageName, packagee);
            }
            return new DBPackageTypeImpl(packagee, resultSet);
        }
    };



    private static final DynamicContentLoader ALL_TYPE_ATTRIBUTES_LOADER = new DynamicContentResultSetLoader<DBTypeAttribute>() {
        public ResultSet createResultSet(DynamicContent<DBTypeAttribute> dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            DBSchema schema = (DBSchema) dynamicContent.getParent();
            return metadataInterface.loadAllTypeAttributes(schema.getName(), connection);
    }

        public DBTypeAttribute createElement(DynamicContent<DBTypeAttribute> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            String typeName = resultSet.getString("TYPE_NAME");
            DBType type = (DBType) loaderCache.getObject(typeName);
            if (type == null) {
                DBSchema schema = (DBSchema) dynamicContent.getParent();
                type = schema.getType(typeName);
                loaderCache.setObject(typeName, type);
            }
            return new DBTypeAttributeImpl(type, resultSet);
        }
    };

    private static final DynamicContentLoader ALL_TYPE_FUNCTIONS_LOADER = new DynamicContentResultSetLoader<DBTypeFunction>() {
        public ResultSet createResultSet(DynamicContent dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            DBSchema schema = (DBSchema) dynamicContent.getParent();
            return metadataInterface.loadAllTypeFunctions(schema.getName(), connection);
        }

        public DBTypeFunction createElement(DynamicContent<DBTypeFunction> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            String typeName = resultSet.getString("TYPE_NAME");
            DBType type = (DBType) loaderCache.getObject(typeName);
            if (type == null) {
                DBSchema schema = (DBSchema) dynamicContent.getParent();
                type = schema.getType(typeName);
                loaderCache.setObject(typeName, type);
            }
            return type == null ?  null : new DBTypeFunctionImpl(type, resultSet);
        }
    };

    private static final DynamicContentLoader ALL_TYPE_PROCEDURES_LOADER = new DynamicContentResultSetLoader<DBTypeProcedure>() {
        public ResultSet createResultSet(DynamicContent dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            DBSchema schema = (DBSchema) dynamicContent.getParent();
            return metadataInterface.loadAllTypeProcedures(schema.getName(), connection);
        }

        public DBTypeProcedure createElement(DynamicContent<DBTypeProcedure> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            String typeName = resultSet.getString("TYPE_NAME");
            DBType type = (DBType) loaderCache.getObject(typeName);
            if (type == null) {
                DBSchema schema = (DBSchema) dynamicContent.getParent();
                type = schema.getType(typeName);
                loaderCache.setObject(typeName, type);
            }
            return type == null ? null : new DBTypeProcedureImpl(type, resultSet);
        }
    };

    private static final DynamicContentLoader ALL_ARGUMENTS_LOADER = new DynamicContentResultSetLoader<DBArgument>() {
        public ResultSet createResultSet(DynamicContent<DBArgument> dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            DBSchema schema = (DBSchema) dynamicContent.getParent();
            return metadataInterface.loadAllMethodArguments(schema.getName(), connection);
        }

        public DBArgument createElement(DynamicContent<DBArgument> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            String programName = resultSet.getString("PROGRAM_NAME");
            String methodName = resultSet.getString("METHOD_NAME");
            String methodType = resultSet.getString("METHOD_TYPE");
            int overload = resultSet.getInt("OVERLOAD");
            DBSchema schema = (DBSchema) dynamicContent.getParent();
            DBProgram program = programName == null ? null : schema.getProgram(programName);

            String cacheKey = methodName + methodType + overload;
            DBMethod method = (DBMethod) loaderCache.getObject(cacheKey);
            DBObjectType objectType = DBObjectType.getObjectType(methodType);

            if (method == null || method.getProgram() != program || method.getOverload() != overload) {
                if (programName == null) {
                    method = schema.getMethod(methodName, objectType, overload);
                } else {
                    method = program == null ? null : program.getMethod(methodName, overload);
                }
                loaderCache.setObject(cacheKey, method);
            }
            return method == null ? null : new DBArgumentImpl(method, resultSet);
        }
    };
}
