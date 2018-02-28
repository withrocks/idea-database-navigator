package com.dci.intellij.dbn.object.common.sorting;

import com.dci.intellij.dbn.object.DBArgument;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.common.DBObjectType;

public class DBArgumentPositionComparator extends DBObjectComparator<DBArgument> {
    public DBArgumentPositionComparator() {
        super(DBObjectType.ARGUMENT, SortingType.POSITION);
    }

    @Override
    public int compare(DBArgument argument1, DBArgument argument2) {
        DBMethod method1 = argument1.getMethod();
        DBMethod method2 = argument2.getMethod();
        if (method1.equals(method2)) {
            return argument1.getPosition() - argument2.getPosition();
        } else {
            return method1.compareTo(method2);
        }
    }
}
