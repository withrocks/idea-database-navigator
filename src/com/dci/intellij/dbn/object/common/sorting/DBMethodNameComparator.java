package com.dci.intellij.dbn.object.common.sorting;

import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBProgram;
import com.dci.intellij.dbn.object.common.DBObjectType;

public abstract class DBMethodNameComparator<T extends DBMethod> extends DBObjectComparator<T> {
    public DBMethodNameComparator(DBObjectType objectType) {
        super(objectType, SortingType.NAME);
    }

    @Override
    public int compare(DBMethod method1, DBMethod method2) {
        DBProgram program1 = method1.getProgram();
        DBProgram program2 = method2.getProgram();
        if (program1 != null && program2 != null) {
            if (program1.equals(program2)) {
                return compareByName(method1, method2);
            } else {
                return program1.compareTo(program2);
            }
        } else {
            return compareByName(method1, method2);
        }
    }

    private static int compareByName(DBMethod method1, DBMethod method2) {
        int overload1 = method1.getOverload();
        int overload2 = method2.getOverload();
        int nameComparison = method1.getName().compareTo(method2.getName());
        if (nameComparison == 0) {
            return overload1 - overload2;
        } else {
            return nameComparison;
        }
    }
}
