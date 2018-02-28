package com.dci.intellij.dbn.object;

import java.util.List;

public interface DBSystemPrivilege extends DBPrivilege {

    List<DBUser> getUserGrantees();
}
