package com.dci.intellij.dbn.object.impl;

import javax.swing.Icon;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.browser.DatabaseBrowserUtils;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoader;
import com.dci.intellij.dbn.common.content.loader.DynamicContentResultSetLoader;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.object.DBPackage;
import com.dci.intellij.dbn.object.DBPackageType;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.property.DBObjectProperty;

public class DBPackageTypeImpl extends DBTypeImpl implements DBPackageType {

    public DBPackageTypeImpl(DBPackage packagee, ResultSet resultSet) throws SQLException {
        super(packagee, resultSet);
    }

    @Override
    protected void initObject(ResultSet resultSet) throws SQLException {
        name = resultSet.getString("TYPE_NAME");
    }

    @Override
    public void initStatus(ResultSet resultSet) throws SQLException {}

    @Override
    public void initProperties() {
        getProperties().set(DBObjectProperty.NAVIGABLE);
    }

    @Override
    protected void initLists() {
        attributes = initChildObjects().createObjectList(DBObjectType.TYPE_ATTRIBUTE, this, ATTRIBUTES_LOADER, true, false);
    }

    public DBPackage getPackage() {
        return (DBPackage) getParentObject();
    }

    @Override
    public DBObjectType getObjectType() {
        return DBObjectType.PACKAGE_TYPE;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return isCollection() ? Icons.DBO_TYPE_COLLECTION : Icons.DBO_TYPE;
    }

    @NotNull
    public List<BrowserTreeNode> buildAllPossibleTreeChildren() {
        return DatabaseBrowserUtils.createList(attributes);
    }

    public boolean isEmbedded() {
        return true;
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    private static final DynamicContentLoader ATTRIBUTES_LOADER = new DynamicContentResultSetLoader() {
        public ResultSet createResultSet(DynamicContent dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            DBPackageTypeImpl type = (DBPackageTypeImpl) dynamicContent.getParent();
            return metadataInterface.loadProgramTypeAttributes(
                    type.getSchema().getName(),
                    type.getPackage().getName(),
                    type.getName(), connection);
        }

        public DBObject createElement(DynamicContent dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            DBTypeImpl type = (DBTypeImpl) dynamicContent.getParent();
            return new DBTypeAttributeImpl(type, resultSet);
        }
    };

}
