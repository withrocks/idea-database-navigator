package com.dci.intellij.dbn.object.impl;

import javax.swing.Icon;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.browser.DatabaseBrowserUtils;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.ui.HtmlToolTipBuilder;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoader;
import com.dci.intellij.dbn.common.content.loader.DynamicContentResultSetLoader;
import com.dci.intellij.dbn.common.content.loader.DynamicSubcontentLoader;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.data.type.DBNativeDataType;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.ddl.DDLFileManager;
import com.dci.intellij.dbn.ddl.DDLFileType;
import com.dci.intellij.dbn.ddl.DDLFileTypeId;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.DBType;
import com.dci.intellij.dbn.object.DBTypeAttribute;
import com.dci.intellij.dbn.object.DBTypeFunction;
import com.dci.intellij.dbn.object.DBTypeProcedure;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationList;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationListImpl;
import com.dci.intellij.dbn.object.common.loader.DBObjectTimestampLoader;
import com.dci.intellij.dbn.object.common.loader.DBSourceCodeLoader;
import com.dci.intellij.dbn.object.common.status.DBObjectStatus;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.object.properties.DBDataTypePresentableProperty;
import com.dci.intellij.dbn.object.properties.PresentableProperty;

public class DBTypeImpl extends DBProgramImpl implements DBType {
    public static final List<DBTypeAttribute> EMPTY_ATTRIBUTE_LIST = Collections.unmodifiableList(new ArrayList<DBTypeAttribute>(0));

    protected DBObjectList<DBTypeAttribute> attributes;
    protected DBObjectList<DBType> subTypes;

    private String superTypeOwner;
    private String superTypeName;
    private DBObjectRef<DBType> superType;

    private DBDataType.Ref collectionElementTypeRef;
    private DBDataType collectionElementType;

    private DBNativeDataType nativeDataType;
    private boolean isCollection;

    DBTypeImpl(DBSchemaObject parent, ResultSet resultSet) throws SQLException {
        // type functions are not editable independently
        super(parent, resultSet);
        assert this.getClass() != DBTypeImpl.class;
    }

    public DBTypeImpl(DBSchema schema, ResultSet resultSet) throws SQLException {
        super(schema, resultSet);
    }

    @Override
    protected void initObject(ResultSet resultSet) throws SQLException {
        name = resultSet.getString("TYPE_NAME");
        superTypeOwner = resultSet.getString("SUPERTYPE_OWNER");
        superTypeName = resultSet.getString("SUPERTYPE_NAME");

        String typecode = resultSet.getString("TYPECODE");
        isCollection = "COLLECTION".equals(typecode);
        ConnectionHandler connectionHandler = getConnectionHandler();
        if  (connectionHandler != null) {
            nativeDataType = connectionHandler.getObjectBundle().getNativeDataType(typecode);
            if (isCollection) {
                collectionElementTypeRef = new DBDataType.Ref(resultSet,  "COLLECTION_");
            }
        }
    }

    protected void initLists() {
        super.initLists();
        if (!isCollection) {
            DBObjectListContainer container = initChildObjects();
            DBSchema schema = getSchema();
            attributes = container.createSubcontentObjectList(DBObjectType.TYPE_ATTRIBUTE, this, ATTRIBUTES_LOADER, schema, true);
            procedures = container.createSubcontentObjectList(DBObjectType.TYPE_PROCEDURE, this, PROCEDURES_LOADER, schema, false);
            functions = container.createSubcontentObjectList(DBObjectType.TYPE_FUNCTION, this, FUNCTIONS_LOADER, schema, false);
            subTypes = container.createSubcontentObjectList(DBObjectType.TYPE, this, SUB_TYPES_LOADER, schema, true);
        }
    }

    @Override
    public DBContentType getContentType() {
        return DBContentType.CODE_SPEC_AND_BODY;
    }

    public DBObjectType getObjectType() {
        return DBObjectType.TYPE;
    }

    @Nullable
    public Icon getIcon() {
        if (getStatus().is(DBObjectStatus.VALID)) {
            if (getStatus().is(DBObjectStatus.DEBUG))  {
                return Icons.DBO_TYPE_DEBUG;
            } else {
                return isCollection ? Icons.DBO_TYPE_COLLECTION : Icons.DBO_TYPE;
            }
        } else {
            return isCollection ? Icons.DBO_TYPE_COLLECTION_ERR : Icons.DBO_TYPE_ERR;
        }
    }

    public Icon getOriginalIcon() {
        return isCollection ? Icons.DBO_TYPE_COLLECTION : Icons.DBO_TYPE;
    }

    public List<DBTypeAttribute> getAttributes() {
        return attributes == null ? EMPTY_ATTRIBUTE_LIST : attributes.getObjects();
    }

    public DBType getSuperType() {
        ConnectionHandler connectionHandler = getConnectionHandler();
        if (superType == null && superTypeOwner != null && superTypeName != null && connectionHandler != null) {
            DBType type = connectionHandler.getObjectBundle().getSchema(superTypeOwner).getType(superTypeName);
            superType = DBObjectRef.from(type);
            superTypeOwner = null;
            superTypeName = null;
        }
        return DBObjectRef.get(superType);
    }

    public DBDataType getCollectionElementType() {
        ConnectionHandler connectionHandler = getConnectionHandler();
        if (collectionElementType == null && collectionElementTypeRef != null && connectionHandler != null) {
            collectionElementType = collectionElementTypeRef.get(connectionHandler);
            collectionElementTypeRef = null;
        }
        return collectionElementType;
    }

    public DBObject getDefaultNavigationObject() {
        if (isCollection) {
            DBDataType dataType = getCollectionElementType();
            if (dataType != null && dataType.isDeclared()) {
                return dataType.getDeclaredType();
            }

        }
        return null;
    }

    @Override
    public List<PresentableProperty> getPresentableProperties() {
        List<PresentableProperty> properties = super.getPresentableProperties();
        DBDataType collectionElementType = getCollectionElementType();
        if (collectionElementType != null) {
            properties.add(0, new DBDataTypePresentableProperty("Collection element type", collectionElementType));
        }
        return properties;
    }

    public List<DBType> getSubTypes() {
        return subTypes.getObjects();
    }

    public DBNativeDataType getNativeDataType() {
        return nativeDataType;
    }

    public boolean isCollection() {
        return isCollection;
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
        return isCollection ?
                EMPTY_TREE_NODE_LIST :
                DatabaseBrowserUtils.createList(attributes, procedures, functions);
    }

    /*********************************************************
     *                         Loaders                       *
     *********************************************************/

    private static final DynamicContentLoader<DBTypeAttribute> ATTRIBUTES_ALTERNATIVE_LOADER = new DynamicContentResultSetLoader<DBTypeAttribute>() {
        public ResultSet createResultSet(DynamicContent<DBTypeAttribute> dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            DBType type = (DBType) dynamicContent.getParent();
            return metadataInterface.loadTypeAttributes(type.getSchema().getName(), type.getName(), connection);
        }

        public DBTypeAttribute createElement(DynamicContent<DBTypeAttribute> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            DBTypeImpl type = (DBTypeImpl) dynamicContent.getParent();
            return new DBTypeAttributeImpl(type, resultSet);
        }
    };

    private static final DynamicSubcontentLoader ATTRIBUTES_LOADER = new DynamicSubcontentLoader<DBTypeAttribute>(true) {
        public boolean match(DBTypeAttribute typeAttribute, DynamicContent dynamicContent) {
            DBType type = (DBType) dynamicContent.getParent();
            return typeAttribute.getType().equals(type);
        }

        public DynamicContentLoader<DBTypeAttribute> getAlternativeLoader() {
            return ATTRIBUTES_ALTERNATIVE_LOADER;
        }
    };

    private static final DynamicContentLoader<DBTypeFunction> FUNCTIONS_ALTERNATIVE_LOADER = new DynamicContentResultSetLoader<DBTypeFunction>() {
        public ResultSet createResultSet(DynamicContent<DBTypeFunction> dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            DBType type = (DBType) dynamicContent.getParent();
            return metadataInterface.loadTypeFunctions(type.getSchema().getName(), type.getName(), connection);
        }

        public DBTypeFunction createElement(DynamicContent<DBTypeFunction> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            DBType type = (DBType) dynamicContent.getParent();
            return new DBTypeFunctionImpl(type, resultSet);
        }
    };

    private static final DynamicSubcontentLoader FUNCTIONS_LOADER = new DynamicSubcontentLoader<DBTypeFunction>(true) {
        public DynamicContentLoader<DBTypeFunction> getAlternativeLoader() {
            return FUNCTIONS_ALTERNATIVE_LOADER;
        }

        public boolean match(DBTypeFunction function, DynamicContent dynamicContent) {
            DBType type = (DBType) dynamicContent.getParent();
            return function.getType() == type;
        }
    };

    private static final DynamicContentLoader<DBTypeProcedure> PROCEDURES_ALTERNATIVE_LOADER = new DynamicContentResultSetLoader<DBTypeProcedure>() {
        public ResultSet createResultSet(DynamicContent<DBTypeProcedure> dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            DBType type = (DBType) dynamicContent.getParent();
            return metadataInterface.loadTypeProcedures(type.getSchema().getName(), type.getName(), connection);
        }

        public DBTypeProcedure createElement(DynamicContent<DBTypeProcedure> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            DBType type = (DBType) dynamicContent.getParent();
            return new DBTypeProcedureImpl(type, resultSet);
        }
    };

    private static final DynamicSubcontentLoader PROCEDURES_LOADER = new DynamicSubcontentLoader<DBTypeProcedure>(true) {
        public DynamicContentLoader<DBTypeProcedure> getAlternativeLoader() {
            return PROCEDURES_ALTERNATIVE_LOADER;
        }

        public boolean match(DBTypeProcedure procedure, DynamicContent dynamicContent) {
            DBType type = (DBType) dynamicContent.getParent();
            return procedure.getType() == type;
        }
    };

    private static final DynamicSubcontentLoader SUB_TYPES_LOADER = new DynamicSubcontentLoader<DBType>(false) {
        public boolean match(DBType type, DynamicContent dynamicContent) {
            DBType superType = type.getSuperType();
            DBType thisType = (DBType) dynamicContent.getParent();
            return superType != null && superType.equals(thisType);
        }

        @Override
        public DynamicContentLoader<DBType> getAlternativeLoader() {
            return null;
        }
    };

    private class SpecSourceCodeLoader extends DBSourceCodeLoader {
        protected SpecSourceCodeLoader(DBObject object) {
            super(object, false);
        }

        public ResultSet loadSourceCode(Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            return metadataInterface.loadObjectSourceCode(getSchema().getName(), getName(), "TYPE", connection);
        }
    }

    private class BodySourceCodeLoader extends DBSourceCodeLoader {
        protected BodySourceCodeLoader(DBObject object) {
            super(object, true);
        }

        public ResultSet loadSourceCode(Connection connection) throws SQLException {
            ConnectionHandler connectionHandler = getConnectionHandler();
            DatabaseMetadataInterface metadataInterface = connectionHandler.getInterfaceProvider().getMetadataInterface();
            return metadataInterface.loadObjectSourceCode(getSchema().getName(), getName(), "TYPE BODY", connection);
        }
    }

    private static DBObjectTimestampLoader SPEC_TIMESTAMP_LOADER = new DBObjectTimestampLoader("TYPE") {};
    private static DBObjectTimestampLoader BODY_TIMESTAMP_LOADER = new DBObjectTimestampLoader("TYPE BODY") {};

   /*********************************************************
    *                   DBEditableObject                    *
    *********************************************************/
    public String loadCodeFromDatabase(DBContentType contentType) throws SQLException {
       DBSourceCodeLoader loader =
               contentType == DBContentType.CODE_SPEC ? new SpecSourceCodeLoader(this) :
               contentType == DBContentType.CODE_BODY ? new BodySourceCodeLoader(this) : null;

       return loader == null ? null : loader.load();
    }

    public String getCodeParseRootId(DBContentType contentType) {
        if (getParentObject() instanceof DBSchema) {
            return contentType == DBContentType.CODE_SPEC ? "type_spec" :
                   contentType == DBContentType.CODE_BODY ? "type_body" : null;
        }
        return null;
    }

    public DDLFileType getDDLFileType(DBContentType contentType) {
        DDLFileManager ddlFileManager = DDLFileManager.getInstance(getProject());
        return contentType == DBContentType.CODE_SPEC ? ddlFileManager.getDDLFileType(DDLFileTypeId.TYPE_SPEC) :
               contentType == DBContentType.CODE_BODY ? ddlFileManager.getDDLFileType(DDLFileTypeId.TYPE_BODY) :
               ddlFileManager.getDDLFileType(DDLFileTypeId.TYPE);
    }

    public DDLFileType[] getDDLFileTypes() {
        DDLFileManager ddlFileManager = DDLFileManager.getInstance(getProject());
        return new DDLFileType[]{
                ddlFileManager.getDDLFileType(DDLFileTypeId.TYPE),
                ddlFileManager.getDDLFileType(DDLFileTypeId.TYPE_SPEC),
                ddlFileManager.getDDLFileType(DDLFileTypeId.TYPE_BODY)};
    }

    @Override
    public int compareTo(@NotNull Object o) {
        if (o instanceof DBType) {
            DBType type = (DBType) o;
            if (getParentObject().equals(type.getParentObject())) {
                if ((type.isCollection() && isCollection) ||
                        (!type.isCollection() && !isCollection)) return super.compareTo(o); else
                return type.isCollection() ? -1 : 1;
            }
        }
        return super.compareTo(o);
    }

    @Override
    protected List<DBObjectNavigationList> createNavigationLists() {
        List<DBObjectNavigationList> objectNavigationLists = super.createNavigationLists();

        DBType superType = getSuperType();
        if (superType != null) {
            objectNavigationLists.add(new DBObjectNavigationListImpl<DBType>("Super Type", superType));
        }
        if (subTypes != null && subTypes.size() > 0) {
            objectNavigationLists.add(new DBObjectNavigationListImpl<DBType>("Sub Types", subTypes.getObjects()));
        }
        if (isCollection) {
            DBDataType dataType = getCollectionElementType();
            if (dataType != null && dataType.isDeclared()) {
                DBType collectionElementType = dataType.getDeclaredType();
                if (collectionElementType != null) {
                    objectNavigationLists.add(new DBObjectNavigationListImpl("Collection element type", collectionElementType));
                }
            }
        }



        return objectNavigationLists;
    }

    public DBObjectTimestampLoader getTimestampLoader(DBContentType contentType) {
        return contentType == DBContentType.CODE_SPEC ? SPEC_TIMESTAMP_LOADER :
               contentType == DBContentType.CODE_BODY ? BODY_TIMESTAMP_LOADER : null;
    }

}
