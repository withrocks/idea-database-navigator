package com.dci.intellij.dbn.object.common;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoader;
import com.dci.intellij.dbn.common.content.loader.DynamicContentResultSetLoader;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionUtil;
import com.dci.intellij.dbn.database.DatabaseDDLInterface;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.ddl.DDLFileType;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.psql.PSQLLanguage;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationList;
import com.dci.intellij.dbn.object.common.loader.DBObjectTimestampLoader;
import com.dci.intellij.dbn.object.common.property.DBObjectProperties;
import com.dci.intellij.dbn.object.common.property.DBObjectProperty;
import com.dci.intellij.dbn.object.common.status.DBObjectStatusHolder;
import com.dci.intellij.dbn.vfs.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;


public abstract class DBSchemaObjectImpl extends DBObjectImpl implements DBSchemaObject {
    private DBObjectList<DBObject> referencedObjects;
    private DBObjectList<DBObject> referencingObjects;
    private DBObjectStatusHolder objectStatus;

    public DBSchemaObjectImpl(DBSchema schema, ResultSet resultSet) throws SQLException {
        super(schema, resultSet);
    }

    public DBSchemaObjectImpl(DBSchemaObject parent, ResultSet resultSet) throws SQLException {
        super(parent, resultSet);
    }

    protected void initProperties() {
        DBObjectProperties properties = getProperties();
        properties.set(DBObjectProperty.EDITABLE);
        properties.set(DBObjectProperty.REFERENCEABLE);
        properties.set(DBObjectProperty.SCHEMA_OBJECT);
    }

    @Override
    protected void initLists() {
        DBObjectProperties properties = getProperties();
        if (properties.is(DBObjectProperty.REFERENCEABLE)) {
            DBObjectListContainer childObjects = initChildObjects();
            referencedObjects = childObjects.createObjectList(DBObjectType.ANY, this, REFERENCED_OBJECTS_LOADER, false, true);
            referencingObjects = childObjects.createObjectList(DBObjectType.ANY, this, REFERENCING_OBJECTS_LOADER, false, true);
        }
    }

    public synchronized DBObjectStatusHolder getStatus() {
        if (objectStatus == null) {
            objectStatus = new DBObjectStatusHolder(getContentType());
        }
        return objectStatus;
    }

    public boolean isEditable(DBContentType contentType) {
        return false;
    }

    public List<DBObject> getReferencedObjects() {
        return referencedObjects.getObjects();
    }

    public List<DBObject> getReferencingObjects() {
        return referencingObjects.getObjects();
    }

    protected List<DBObjectNavigationList> createNavigationLists() {
        return new ArrayList<DBObjectNavigationList>();
    }

    public Timestamp loadChangeTimestamp(DBContentType contentType) throws SQLException {
        if (DatabaseFeature.OBJECT_CHANGE_TRACING.isSupported(this)) {
            return getTimestampLoader(contentType).load(this);
        }
        return null;
    }

    public DBObjectTimestampLoader getTimestampLoader(DBContentType contentType) {
        return new DBObjectTimestampLoader(getTypeName().toUpperCase());
    }

    public DDLFileType getDDLFileType(DBContentType contentType) {
        return null;
    }

    public DDLFileType[] getDDLFileTypes() {
        return null;
    }

    public String loadCodeFromDatabase(DBContentType contentType) throws SQLException {
        return null;
    }

    public DBLanguage getCodeLanguage(DBContentType contentType) {
        return PSQLLanguage.INSTANCE;
    }

    public String getCodeParseRootId(DBContentType contentType) {
        return null;
    }

    @NotNull
    public DBEditableObjectVirtualFile getVirtualFile() {
        return DatabaseFileSystem.getInstance().findDatabaseFile(this);
    }

    @Override
    public List<DBSchema> getReferencingSchemas() throws SQLException {
        List<DBSchema> schemas = new ArrayList<DBSchema>();
        ConnectionHandler connectionHandler = getConnectionHandler();
        if (connectionHandler != null) {
            Connection connection = connectionHandler.getPoolConnection(getSchema());
            ResultSet resultSet = null;
            try {
                DatabaseMetadataInterface metadataInterface = connectionHandler.getInterfaceProvider().getMetadataInterface();
                resultSet = metadataInterface.loadReferencingSchemas(getSchema().getName(), getName(), connection);
                while (resultSet.next()) {
                    String schemaName = resultSet.getString("SCHEMA_NAME");
                    DBSchema schema = getConnectionHandler().getObjectBundle().getSchema(schemaName);
                    if (schema != null)  {
                        schemas.add(schema);
                    }
                }
                if (schemas.isEmpty()) {
                    schemas.add(getSchema());
                }

            } finally {
                ConnectionUtil.closeResultSet(resultSet);
                connectionHandler.freePoolConnection(connection);
            }
        }
        return schemas;
    }

    public void executeUpdateDDL(DBContentType contentType, String oldCode, String newCode) throws SQLException {
        ConnectionHandler connectionHandler = getConnectionHandler();
        if (connectionHandler != null) {
            Connection connection = connectionHandler.getPoolConnection(getSchema());
            try {
                DatabaseDDLInterface ddlInterface = connectionHandler.getInterfaceProvider().getDDLInterface();
                ddlInterface.updateObject(getName(), getObjectType().getName(), oldCode,  newCode, connection);
            } finally {
                connectionHandler.freePoolConnection(connection);
            }
        }
    }

    /*********************************************************
     *                         Loaders                       *
     *********************************************************/
    private static final DynamicContentLoader REFERENCED_OBJECTS_LOADER = new DynamicContentResultSetLoader() {
        public ResultSet createResultSet(DynamicContent dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            DBSchemaObject schemaObject = (DBSchemaObject) dynamicContent.getParent();
            return metadataInterface.loadReferencedObjects(schemaObject.getSchema().getName(), schemaObject.getName(), connection);
        }

        public DBObject createElement(DynamicContent dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            String objectOwner = resultSet.getString("OBJECT_OWNER");
            String objectName = resultSet.getString("OBJECT_NAME");
            String objectTypeName = resultSet.getString("OBJECT_TYPE");
            DBObjectType objectType = DBObjectType.getObjectType(objectTypeName);
            if (objectType == DBObjectType.PACKAGE_BODY) objectType = DBObjectType.PACKAGE;
            if (objectType == DBObjectType.TYPE_BODY) objectType = DBObjectType.TYPE;

            DBSchema schema = (DBSchema) loaderCache.getObject(objectOwner);

            if (schema == null) {
                DBSchemaObject schemaObject = (DBSchemaObject) dynamicContent.getParent();
                ConnectionHandler connectionHandler = schemaObject.getConnectionHandler();
                if (connectionHandler != null) {
                    schema = connectionHandler.getObjectBundle().getSchema(objectOwner);
                    loaderCache.setObject(objectOwner,  schema);
                }
            }

            return schema == null ? null : schema.getChildObject(objectType, objectName, 0, true);
        }
    };

    private static final DynamicContentLoader REFERENCING_OBJECTS_LOADER = new DynamicContentResultSetLoader() {
        public ResultSet createResultSet(DynamicContent dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            DBSchemaObject schemaObject = (DBSchemaObject) dynamicContent.getParent();
            return metadataInterface.loadReferencingObjects(schemaObject.getSchema().getName(), schemaObject.getName(), connection);
        }

        public DBObject createElement(DynamicContent dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            String objectOwner = resultSet.getString("OBJECT_OWNER");
            String objectName = resultSet.getString("OBJECT_NAME");
            String objectTypeName = resultSet.getString("OBJECT_TYPE");
            DBObjectType objectType = DBObjectType.getObjectType(objectTypeName);
            if (objectType == DBObjectType.PACKAGE_BODY) objectType = DBObjectType.PACKAGE;
            if (objectType == DBObjectType.TYPE_BODY) objectType = DBObjectType.TYPE;

            DBSchema schema = (DBSchema) loaderCache.getObject(objectOwner);
            if (schema == null) {
                DBSchemaObject schemaObject = (DBSchemaObject) dynamicContent.getParent();
                ConnectionHandler connectionHandler = schemaObject.getConnectionHandler();
                if (connectionHandler != null) {
                    schema = connectionHandler.getObjectBundle().getSchema(objectOwner);
                    loaderCache.setObject(objectOwner,  schema);
                }
            }
            return schema == null ? null : schema.getChildObject(objectType, objectName, 0, true);
        }
    };
}
