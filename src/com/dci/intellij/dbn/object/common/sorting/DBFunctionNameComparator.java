package com.dci.intellij.dbn.object.common.sorting;

import com.dci.intellij.dbn.object.DBProcedure;
import com.dci.intellij.dbn.object.common.DBObjectType;

public class DBFunctionNameComparator extends DBMethodNameComparator<DBProcedure> {
    public DBFunctionNameComparator() {
        super(DBObjectType.FUNCTION);
    }
}
