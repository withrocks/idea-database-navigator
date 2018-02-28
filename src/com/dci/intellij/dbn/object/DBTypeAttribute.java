package com.dci.intellij.dbn.object;

import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.object.common.DBObject;

public interface DBTypeAttribute extends DBObject {
    DBType getType();
    DBDataType getDataType();
    int getPosition();
}
