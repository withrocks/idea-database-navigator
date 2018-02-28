package com.dci.intellij.dbn.object.common.sorting;

import com.dci.intellij.dbn.object.DBProcedure;
import com.dci.intellij.dbn.object.common.DBObjectType;

public class DBProcedurePositionComparator extends DBMethodPositionComparator<DBProcedure> {
    public DBProcedurePositionComparator() {
        super(DBObjectType.PROCEDURE);
    }
}
