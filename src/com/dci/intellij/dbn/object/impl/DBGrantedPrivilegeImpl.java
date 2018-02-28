package com.dci.intellij.dbn.object.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.object.DBGrantedPrivilege;
import com.dci.intellij.dbn.object.DBPrivilege;
import com.dci.intellij.dbn.object.DBPrivilegeGrantee;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectImpl;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;

public class DBGrantedPrivilegeImpl extends DBObjectImpl implements DBGrantedPrivilege {
    private DBObjectRef<DBPrivilege> privilegeRef;
    private boolean isAdminOption;

    public DBGrantedPrivilegeImpl(DBPrivilegeGrantee grantee, ResultSet resultSet) throws SQLException {
        super(grantee, resultSet);
    }

    @Override
    protected void initObject(ResultSet resultSet) throws SQLException {
        this.name = resultSet.getString("GRANTED_PRIVILEGE_NAME");
        this.privilegeRef = DBObjectRef.from(getConnectionHandler().getObjectBundle().getPrivilege(name));
        this.isAdminOption = resultSet.getString("IS_ADMIN_OPTION").equals("Y");
    }

    public DBObjectType getObjectType() {
        return DBObjectType.GRANTED_PRIVILEGE;
    }

    public DBPrivilegeGrantee getGrantee() {
        return (DBPrivilegeGrantee) getParentObject();
    }

    public DBPrivilege getPrivilege() {
        return DBObjectRef.get(privilegeRef);
    }

    public boolean isAdminOption() {
        return isAdminOption;
    }

    @Override
    public DBObject getDefaultNavigationObject() {
        return getPrivilege();
    }

    /*********************************************************
     *                     TreeElement                       *
     *********************************************************/
    public boolean isLeafTreeElement() {
        return true;
    }


    @NotNull
    public List<BrowserTreeNode> buildAllPossibleTreeChildren() {
        return EMPTY_TREE_NODE_LIST;
    }

}
