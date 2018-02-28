package com.dci.intellij.dbn.object;

import java.util.List;

public interface DBUser extends DBRoleGrantee, DBPrivilegeGrantee {
    boolean isExpired();
    boolean isLocked();
    DBSchema getSchema();
    List<DBGrantedPrivilege> getPrivileges();
    List<DBGrantedRole> getRoles();

    boolean hasSystemPrivilege(DBSystemPrivilege systemPrivilege);
    boolean hasRole(DBRole role);
}
