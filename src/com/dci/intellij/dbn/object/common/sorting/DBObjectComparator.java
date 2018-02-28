package com.dci.intellij.dbn.object.common.sorting;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;

public abstract class DBObjectComparator<T extends DBObject> implements Comparator<T>{
    public static final List<DBObjectComparator> REGISTRY = new ArrayList<DBObjectComparator>();
    static {
        new DBColumnNameComparator();
        new DBColumnPositionComparator();
        new DBProcedureNameComparator();
        new DBProcedurePositionComparator();
        new DBFunctionNameComparator();
        new DBFunctionPositionComparator();
        new DBArgumentNameComparator();
        new DBArgumentPositionComparator();
    }

    @Nullable
    public static DBObjectComparator get(DBObjectType objectType, SortingType sortingType) {
        for (DBObjectComparator comparator : REGISTRY) {
            if (comparator.objectType == objectType && comparator.sortingType == sortingType) {
                return comparator;
            }
        }
        return null;
    }

    public static List<SortingType> getSortingTypes(DBObjectType objectType) {
        List<SortingType> sortingTypes = new ArrayList<SortingType>();
        for (DBObjectComparator comparator : REGISTRY) {
            if (comparator.objectType == objectType) {
                sortingTypes.add(comparator.sortingType);
            }
        }
        return sortingTypes;
    }

    private DBObjectType objectType;
    private SortingType sortingType;

    public DBObjectComparator(DBObjectType objectType, SortingType sortingType) {
        this.objectType = objectType;
        this.sortingType = sortingType;
        REGISTRY.add(this);
    }

    public DBObjectType getObjectType() {
        return objectType;
    }

    public SortingType getSortingType() {
        return sortingType;
    }
}
