package com.dci.intellij.dbn.object.common.sorting;

import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBDataset;
import com.dci.intellij.dbn.object.common.DBObjectType;

public class DBColumnPositionComparator extends DBObjectComparator<DBColumn> {

    public DBColumnPositionComparator() {
        super(DBObjectType.COLUMN, SortingType.POSITION);
    }

    @Override
    public int compare(DBColumn column1, DBColumn column2) {
        DBDataset dataset1 = column1.getDataset();
        DBDataset dataset2 = column2.getDataset();
        if (dataset1.equals(dataset2)) {
            return column1.getPosition()-column2.getPosition();
        }
        return dataset1.compareTo(dataset2);
    }
}
