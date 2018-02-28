package com.dci.intellij.dbn.object.common.sorting;

import com.dci.intellij.dbn.object.DBProcedure;
import com.dci.intellij.dbn.object.common.DBObjectType;

public class DBProcedureNameComparator extends DBMethodNameComparator<DBProcedure> {
    public DBProcedureNameComparator() {
        super(DBObjectType.PROCEDURE);
    }
}
