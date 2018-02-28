package com.dci.intellij.dbn.object;

import java.util.List;

public interface DBTable extends DBDataset {
    boolean isTemporary();
    List<DBIndex> getIndexes();
    List<DBNestedTable> getNestedTables();
    DBIndex getIndex(String name);
    DBNestedTable getNestedTable(String name);
    List<DBColumn> getPrimaryKeyColumns();
    List<DBColumn> getForeignKeyColumns();
    List<DBColumn> getUniqueKeyColumns();
}