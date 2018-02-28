package com.dci.intellij.dbn.object.common.sorting;

import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBProgram;
import com.dci.intellij.dbn.object.common.DBObjectType;

public abstract class DBMethodPositionComparator<T extends DBMethod> extends DBObjectComparator<T> {
    public DBMethodPositionComparator(DBObjectType objectType) {
        super(objectType, SortingType.POSITION);
    }

    @Override
    public int compare(DBMethod method1, DBMethod method2) {
        DBProgram program1 = method1.getProgram();
        DBProgram program2 = method2.getProgram();
        int position1 = method1.getPosition();
        int position2 = method2.getPosition();
        if (program1 != null && program2 != null) {
            if (program1.equals(program2)) {
                return position1 - position2;
            } else {
                return program1.compareTo(program2);
            }
        } else {
            if (position1 == position2) {
                int overload1 = method1.getOverload();
                int overload2 = method2.getOverload();
                int nameComparison = method1.getName().compareTo(method2.getName());
                if (nameComparison == 0) {
                    return overload1 - overload2;
                } else {
                    return nameComparison;
                }
            } else {
                return position1 - position2;
            }
        }
    }
}
