package com.dci.intellij.dbn.object;

import java.util.List;

public interface DBObjectPrivilege extends DBPrivilege {

    List<DBUser> getUserGrantees();
}
