package com.dci.intellij.dbn.object.common.sorting;

import com.dci.intellij.dbn.object.DBProcedure;
import com.dci.intellij.dbn.object.common.DBObjectType;

public class DBFunctionPositionComparator extends DBMethodPositionComparator<DBProcedure> {
    public DBFunctionPositionComparator() {
        super(DBObjectType.FUNCTION);
    }
}
