package com.dci.intellij.dbn.object;

import com.dci.intellij.dbn.object.common.DBObject;

public interface DBGrantedRole extends DBObject{
    DBRoleGrantee getGrantee();
    DBRole getRole();
    boolean isAdminOption();
    boolean isDefaultRole();
}