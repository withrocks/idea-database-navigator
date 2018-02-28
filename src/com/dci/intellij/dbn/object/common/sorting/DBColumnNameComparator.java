package com.dci.intellij.dbn.object.common.sorting;

import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBDataset;
import com.dci.intellij.dbn.object.common.DBObjectType;

public class DBColumnNameComparator extends DBObjectComparator<DBColumn> {

    public DBColumnNameComparator() {
        super(DBObjectType.COLUMN, SortingType.NAME);
    }

    @Override
    public int compare(DBColumn column1, DBColumn column2) {
        DBDataset dataset1 = column1.getDataset();
        DBDataset dataset2 = column2.getDataset();
        if (dataset1.equals(dataset2)) {
            String name2 = column2.getName();
            String name1 = column1.getName();
            if (column1.isPrimaryKey() && column2.isPrimaryKey()) {
                return name1.compareTo(name2);
            } else if (column1.isPrimaryKey()) {
                return -1;
            } else if (column2.isPrimaryKey()){
                return 1;
            } else {
                return name1.compareTo(name2);
            }
        }
        return dataset1.compareTo(dataset2);
    }
}
