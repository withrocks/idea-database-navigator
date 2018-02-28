package com.dci.intellij.dbn.object;

import com.dci.intellij.dbn.object.common.DBObject;

public interface DBGrantedPrivilege extends DBObject{
    DBPrivilegeGrantee getGrantee();
    DBPrivilege getPrivilege();
    boolean isAdminOption();
}
