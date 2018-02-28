package com.dci.intellij.dbn.object.common.list;

import com.dci.intellij.dbn.object.common.DBObject;

import java.util.List;

public interface ObjectListProvider<T extends DBObject> {
    List<T> getObjects();
}
