package com.dci.intellij.dbn.object;

import java.util.List;

import com.dci.intellij.dbn.object.common.DBObject;

public interface DBNestedTable extends DBObject {
    List<DBNestedTableColumn> getColumns();
    DBNestedTableColumn getColumn(String name);

    DBTable getTable();
}