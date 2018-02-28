package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.object.DBGrantedPrivilege;
import com.dci.intellij.dbn.object.DBRole;
import com.dci.intellij.dbn.object.common.DBObjectRelationType;
import com.dci.intellij.dbn.object.common.list.DBObjectRelationImpl;

public class DBRolePrivilegeRelation extends DBObjectRelationImpl<DBRole, DBGrantedPrivilege> {
    public DBRolePrivilegeRelation(DBRole role, DBGrantedPrivilege privilege) {
        super(DBObjectRelationType.ROLE_PRIVILEGE, role, privilege);
    }

    public DBRole getRole() {
        return getSourceObject();
    }

    public DBGrantedPrivilege getPrivilege() {
        return getTargetObject();
    }
}