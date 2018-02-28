package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.object.DBGrantedPrivilege;
import com.dci.intellij.dbn.object.DBUser;
import com.dci.intellij.dbn.object.common.DBObjectRelationType;
import com.dci.intellij.dbn.object.common.list.DBObjectRelationImpl;

public class DBUserPrivilegeRelation extends DBObjectRelationImpl<DBUser, DBGrantedPrivilege> {
    public DBUserPrivilegeRelation(DBUser user, DBGrantedPrivilege privilege) {
        super(DBObjectRelationType.USER_PRIVILEGE, user, privilege);
    }

    public DBUser getUser() {
        return getSourceObject();
    }

    public DBGrantedPrivilege getPrivilege() {
        return getTargetObject();
    }
}