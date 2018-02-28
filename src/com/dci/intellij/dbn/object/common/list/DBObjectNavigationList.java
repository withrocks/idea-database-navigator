package com.dci.intellij.dbn.object.common.list;

import com.dci.intellij.dbn.object.common.DBObject;

import java.util.List;

public interface DBObjectNavigationList<T extends DBObject> {
    DBObjectNavigationList[] EMPTY_ARRAY = new DBObjectNavigationList[0];

    String getName();
    T getObject();
    List<T> getObjects();
    ObjectListProvider<T> getObjectsProvider();
    boolean isLazy();
}
