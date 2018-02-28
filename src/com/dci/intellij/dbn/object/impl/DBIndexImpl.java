package com.dci.intellij.dbn.object.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.ui.HtmlToolTipBuilder;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoader;
import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBIndex;
import com.dci.intellij.dbn.object.DBTable;
import com.dci.intellij.dbn.object.common.DBObjectRelationType;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.DBSchemaObjectImpl;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationList;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationListImpl;
import com.dci.intellij.dbn.object.common.list.loader.DBObjectListFromRelationListLoader;
import com.dci.intellij.dbn.object.common.property.DBObjectProperty;
import com.dci.intellij.dbn.object.common.status.DBObjectStatus;

public class DBIndexImpl extends DBSchemaObjectImpl implements DBIndex {
    private DBObjectList<DBColumn> columns;
    private boolean isUnique;

    public DBIndexImpl(DBTable table, ResultSet resultSet) throws SQLException {
        super(table, resultSet);
    }

    @Override
    protected void initObject(ResultSet resultSet) throws SQLException {
        name = resultSet.getString("INDEX_NAME");
        isUnique = resultSet.getString("IS_UNIQUE").equals("Y");
    }

    public void initStatus(ResultSet resultSet) throws SQLException {
        boolean valid = resultSet.getString("IS_VALID").equals("Y");
        getStatus().set(DBObjectStatus.VALID, valid);
    }

    @Override
    public void initProperties() {
        getProperties().set(DBObjectProperty.SCHEMA_OBJECT);
    }

    @Override
    protected void initLists() {
        super.initLists();
        DBTable table = getTable();
        if (table != null) {
            columns = initChildObjects().createSubcontentObjectList(DBObjectType.COLUMN, this, COLUMNS_LOADER, table, DBObjectRelationType.INDEX_COLUMN, false);
        }
    }

    public DBObjectType getObjectType() {
        return DBObjectType.INDEX;
    }

    public DBTable getTable() {
        return (DBTable) getParentObject();
    }

    public List<DBColumn> getColumns() {
        return columns.getObjects();
    }

    public boolean isUnique() {
        return isUnique;
    }

    protected List<DBObjectNavigationList> createNavigationLists() {
        List<DBObjectNavigationList> objectNavigationLists = super.createNavigationLists();

        if (columns.size() > 0) {
            objectNavigationLists.add(new DBObjectNavigationListImpl<DBColumn>("Columns", columns.getObjects()));
        }
        objectNavigationLists.add(new DBObjectNavigationListImpl<DBTable>("Table", getTable()));

        return objectNavigationLists;
    }

    public void buildToolTip(HtmlToolTipBuilder ttb) {
        ttb.append(true, getObjectType().getName(), true);
        ttb.createEmptyRow();
        super.buildToolTip(ttb);
    }

    /********************************************************
     *                   TreeeElement                       *
     * ******************************************************/

    public boolean isLeafTreeElement() {
        return true;
    }

    @NotNull
    public List<BrowserTreeNode> buildAllPossibleTreeChildren() {
        return EMPTY_TREE_NODE_LIST;
    }

    /**
     * ******************************************************
     * Loaders                       *
     * *******************************************************
     */
    private static final DynamicContentLoader COLUMNS_LOADER = new DBObjectListFromRelationListLoader();
}
