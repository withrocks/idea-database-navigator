package com.dci.intellij.dbn.object.common.sorting;

import com.dci.intellij.dbn.object.DBArgument;
import com.dci.intellij.dbn.object.DBFunction;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.common.DBObjectType;

public class DBArgumentNameComparator extends DBObjectComparator<DBArgument> {
    public DBArgumentNameComparator() {
        super(DBObjectType.ARGUMENT, SortingType.NAME);
    }

    @Override
    public int compare(DBArgument argument1, DBArgument argument2) {
        DBMethod method1 = argument1.getMethod();
        DBMethod method2 = argument2.getMethod();
        if (method1.equals(method2)) {
            if (method1 instanceof DBFunction) {
                if (method1.getPosition() == 1) {
                    return -1;
                }
                if (method2.getPosition() == 1) {
                    return 1;
                }
            }
            return argument1.getName().compareTo(argument2.getName());
        } else {
            return method1.compareTo(method2);
        }
    }
}
