package com.dci.intellij.dbn.object.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.browser.DatabaseBrowserUtils;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoader;
import com.dci.intellij.dbn.common.content.loader.DynamicContentResultSetLoader;
import com.dci.intellij.dbn.common.content.loader.DynamicSubcontentLoader;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.object.DBArgument;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBProgram;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.common.DBSchemaObjectImpl;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.common.property.DBObjectProperty;
import com.dci.intellij.dbn.object.common.status.DBObjectStatus;
import com.dci.intellij.dbn.object.common.status.DBObjectStatusHolder;

public abstract class DBMethodImpl extends DBSchemaObjectImpl implements DBMethod {
    protected DBObjectList<DBArgument> arguments;
    protected int position;
    protected int overload;
    protected boolean isDeterministic;
    private DBLanguage language;

    public DBMethodImpl(DBSchemaObject parent, ResultSet resultSet) throws SQLException {
        super(parent, resultSet);
    }

    public DBMethodImpl(DBSchema schema, ResultSet resultSet) throws SQLException {
        super(schema, resultSet);
    }

    @Override
    protected void initObject(ResultSet resultSet) throws SQLException {
        isDeterministic = resultSet.getString("IS_DETERMINISTIC").equals("Y");
        overload = resultSet.getInt("OVERLOAD");
        position = resultSet.getInt("POSITION");
        language = DBLanguage.getLanguage(resultSet.getString("LANGUAGE"));
    }

    @Override
    public void initProperties() {
        super.initProperties();
        getProperties().set(DBObjectProperty.COMPILABLE);
    }

    @Override
    public void initStatus(ResultSet resultSet) throws SQLException {
        boolean isValid = "Y".equals(resultSet.getString("IS_VALID"));
        boolean isDebug = "Y".equals(resultSet.getString("IS_DEBUG"));
        DBObjectStatusHolder objectStatus = getStatus();
        objectStatus.set(DBObjectStatus.VALID, isValid);
        objectStatus.set(DBObjectStatus.DEBUG, isDebug);
    }

    protected void initLists() {
        super.initLists();
        DBObjectListContainer container = initChildObjects();
        arguments = container.createSubcontentObjectList(DBObjectType.ARGUMENT, this, ARGUMENTS_LOADER, getSchema(), true);
    }

    @NotNull
    public DBLanguage getLanguage() {
        return language;
    }

    @Override
    public boolean isEditable(DBContentType contentType) {
        return getContentType() == DBContentType.CODE && contentType == DBContentType.CODE;
    }

    public boolean isDeterministic() {
        return isDeterministic;
    }

    public boolean hasDeclaredArguments() {
        for (DBArgument argument : getArguments()) {
            if (argument.getDataType().isDeclared()) {
                return true;
            }
        }
        return false; 
    }

    public List<DBArgument> getArguments() {
        return arguments.getObjects();
    }

    @Override
    public DBArgument getReturnArgument() {
        return null;
    }

    public DBArgument getArgument(String name) {
        return (DBArgument) getObjectByName(getArguments(), name);
    }

    public int getOverload() {
        return overload;
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public String getPresentableTextDetails() {
        return overload > 0 ? " #" + overload : "";
    }

    public boolean isProgramMethod() {
        return false;
    }

    @Override
    public void reload() {
        arguments.reload();
    }

    @Override
    public int compareTo(@NotNull Object o) {
        int result = super.compareTo(o);
        if (result == 0) {
            DBMethod method = (DBMethod) o;
            return overload - method.getOverload();
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (super.equals(obj)) {
            DBMethod method = (DBMethod) obj;
            return method.getOverload() == overload;
        }
        return false;
    }

    /*********************************************************
     *                     TreeElement                       *
     *********************************************************/
    @NotNull
    public List<BrowserTreeNode> buildAllPossibleTreeChildren() {
        return DatabaseBrowserUtils.createList(arguments);
    }

    /*********************************************************
     *                         Loaders                       *
     *********************************************************/

    private static final DynamicContentLoader<DBArgument> ARGUMENTS_ALTERNATIVE_LOADER = new DynamicContentResultSetLoader<DBArgument>() {
        public ResultSet createResultSet(DynamicContent<DBArgument> dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            DBMethod method = (DBMethod) dynamicContent.getParent();
            String ownerName = method.getSchema().getName();
            int overload = method.getOverload();
            DBProgram program = method.getProgram();
            if (program == null) {
                return metadataInterface.loadMethodArguments(
                        ownerName,
                        method.getName(),
                        method.getMethodType(),
                        overload,
                        connection);
            } else {
                return metadataInterface.loadProgramMethodArguments(
                        ownerName,
                        program.getName(),
                        method.getName(),
                        overload,
                        connection);
            }
        }

        public DBArgument createElement(DynamicContent<DBArgument> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            DBMethod method = (DBMethod) dynamicContent.getParent();
            return new DBArgumentImpl(method, resultSet);
        }
    };

    private static final DynamicSubcontentLoader<DBArgument> ARGUMENTS_LOADER = new DynamicSubcontentLoader<DBArgument>(true) {
        public DynamicContentLoader<DBArgument> getAlternativeLoader() {
            return ARGUMENTS_ALTERNATIVE_LOADER;
        }

        public boolean match(DBArgument argument, DynamicContent dynamicContent) {
            DBMethod method = (DBMethod) dynamicContent.getParent();
            DBMethod argumentMethod = argument.getMethod();
            return argumentMethod != null && argumentMethod.equals(method) && argument.getOverload() == method.getOverload();
        }
    };
}
