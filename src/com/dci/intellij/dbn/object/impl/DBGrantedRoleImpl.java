package com.dci.intellij.dbn.object.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.object.DBGrantedRole;
import com.dci.intellij.dbn.object.DBRole;
import com.dci.intellij.dbn.object.DBRoleGrantee;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectImpl;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;

public class DBGrantedRoleImpl extends DBObjectImpl implements DBGrantedRole {
    private DBObjectRef<DBRole> roleRef;
    private boolean isAdminOption;
    private boolean isDefaultRole;

    public DBGrantedRoleImpl(DBRoleGrantee grantee, ResultSet resultSet) throws SQLException {
        super(grantee, resultSet);
    }

    @Override
    protected void initObject(ResultSet resultSet) throws SQLException {
        this.name = resultSet.getString("GRANTED_ROLE_NAME");
        this.roleRef = DBObjectRef.from(getConnectionHandler().getObjectBundle().getRole(name));
        this.isAdminOption = resultSet.getString("IS_ADMIN_OPTION").equals("Y");
        this.isDefaultRole = resultSet.getString("IS_DEFAULT_ROLE").equals("Y");
    }

    public DBObjectType getObjectType() {
        return DBObjectType.GRANTED_ROLE;
    }

    public DBRoleGrantee getGrantee() {
        return (DBRoleGrantee) getParentObject();
    }

    public DBRole getRole() {
        return DBObjectRef.get(roleRef);
    }

    public boolean isAdminOption() {
        return isAdminOption;
    }

    public boolean isDefaultRole() {
        return isDefaultRole;
    }

    @Override
    public DBObject getDefaultNavigationObject() {
        return getRole();
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
