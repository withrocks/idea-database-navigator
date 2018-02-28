package com.dci.intellij.dbn.object;

import com.dci.intellij.dbn.object.common.DBSchemaObject;

public interface DBDatabaseLink extends DBSchemaObject {
    String getUserName();
    String getHost();
}