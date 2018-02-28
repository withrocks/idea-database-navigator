package com.dci.intellij.dbn.object;

import java.util.List;

import com.dci.intellij.dbn.object.common.DBObject;

public interface DBPrivilege extends DBObject {

    List<DBUser> getUserGrantees();
}
