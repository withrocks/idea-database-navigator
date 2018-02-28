package com.dci.intellij.dbn.object.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.object.DBNestedTable;
import com.dci.intellij.dbn.object.DBNestedTableColumn;
import com.dci.intellij.dbn.object.common.DBObjectImpl;
import com.dci.intellij.dbn.object.common.DBObjectType;

public class DBNestedTableColumnImpl extends DBObjectImpl implements DBNestedTableColumn {

    public DBNestedTableColumnImpl(DBNestedTable parent, ResultSet resultSet) throws SQLException {
        super(parent, resultSet);
        // todo !!!
    }

    @Override
    protected void initObject(ResultSet resultSet) throws SQLException {
    }

    public DBObjectType getObjectType() {
        return DBObjectType.NESTED_TABLE_COLUMN;
    }

    public DBNestedTable getNestedTable() {
        return (DBNestedTable) getParentObject();
    }

    @Override
    public void dispose() {
        super.dispose();
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
    }
}
