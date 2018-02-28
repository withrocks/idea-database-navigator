package com.dci.intellij.dbn.object.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.ui.HtmlToolTipBuilder;
import com.dci.intellij.dbn.object.DBNestedTable;
import com.dci.intellij.dbn.object.DBNestedTableColumn;
import com.dci.intellij.dbn.object.DBTable;
import com.dci.intellij.dbn.object.DBType;
import com.dci.intellij.dbn.object.common.DBObjectImpl;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;

public class DBNestedTableImpl extends DBObjectImpl implements DBNestedTable {
    private List<DBNestedTableColumn> columns;
    private DBObjectRef<DBType> typeRef;

    public DBNestedTableImpl(DBTable parent, ResultSet resultSet) throws SQLException {
        super(parent, resultSet);

    }

    @Override
    protected void initObject(ResultSet resultSet) throws SQLException {
        name = resultSet.getString("NESTED_TABLE_NAME");

        String typeOwner = resultSet.getString("DATA_TYPE_OWNER");
        String typeName = resultSet.getString("DATA_TYPE_NAME");
        typeRef = DBObjectRef.from(getConnectionHandler().getObjectBundle().getSchema(typeOwner).getType(typeName));
        // todo !!!
    }

    public DBObjectType getObjectType() {
        return DBObjectType.NESTED_TABLE;
    }

    public List<DBNestedTableColumn> getColumns() {
        if (columns == null) {
            columns = new ArrayList<DBNestedTableColumn>();
            //todo
        }
        return columns;
    }

    public DBNestedTableColumn getColumn(String name) {
        return (DBNestedTableColumn) getObjectByName(getColumns(), name);
    }

    public DBTable getTable() {
        return (DBTable) getParentObject();
    }

    public DBType getType() {
        return DBObjectRef.get(typeRef);
    }

    public void buildToolTip(HtmlToolTipBuilder ttb) {
        ttb.append(true, getObjectType().getName(), true);
        ttb.createEmptyRow();
        super.buildToolTip(ttb);
    }

    @Override
    public void dispose() {
        super.dispose();
        typeRef = null;
    }

    /*********************************************************
     *                     TreeElement                       *
     *********************************************************/

    public boolean isLeafTreeElement() {
        return true;
    }

    @NotNull
    public List<BrowserTreeNode> buildAllPossibleTreeChildren() {
        return EMPTY_TREE_NODE_LIST;
        //return getColumns();
    }
}
