package com.dci.intellij.dbn.object.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.browser.DatabaseBrowserUtils;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoader;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.object.DBGrantedPrivilege;
import com.dci.intellij.dbn.object.DBGrantedRole;
import com.dci.intellij.dbn.object.DBPrivilege;
import com.dci.intellij.dbn.object.DBRole;
import com.dci.intellij.dbn.object.DBUser;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import com.dci.intellij.dbn.object.common.DBObjectImpl;
import com.dci.intellij.dbn.object.common.DBObjectRelationType;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationList;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationListImpl;
import com.dci.intellij.dbn.object.common.list.loader.DBObjectListFromRelationListLoader;

public class DBRoleImpl extends DBObjectImpl implements DBRole {
    DBObjectList<DBGrantedPrivilege> privileges;
    DBObjectList<DBGrantedRole> grantedRoles;

    public DBRoleImpl(ConnectionHandler connectionHandler, ResultSet resultSet) throws SQLException {
        super(connectionHandler.getObjectBundle(), resultSet);
    }

    @Override
    protected void initObject(ResultSet resultSet) throws SQLException {
        name = resultSet.getString("ROLE_NAME");
    }

    @Override
    protected void initLists() {
        DBObjectListContainer ol = initChildObjects();
        DBObjectBundle sourceContentHolder = getConnectionHandler().getObjectBundle();
        privileges = ol.createSubcontentObjectList(DBObjectType.GRANTED_PRIVILEGE, this, PRIVILEGES_LOADER, sourceContentHolder, DBObjectRelationType.ROLE_PRIVILEGE, true);
        grantedRoles = ol.createSubcontentObjectList(DBObjectType.GRANTED_ROLE, this, ROLES_LOADER, sourceContentHolder, DBObjectRelationType.ROLE_ROLE, true);
    }

    public DBObjectType getObjectType() {
        return DBObjectType.ROLE;
    }

    public List<DBGrantedPrivilege> getPrivileges() {
        return privileges.getObjects();
    }

    public List<DBGrantedRole> getGrantedRoles() {
        return grantedRoles.getObjects();
    }

    public List<DBUser> getUserGrantees() {
        List<DBUser> grantees = new ArrayList<DBUser>();
        for (DBUser user : getConnectionHandler().getObjectBundle().getUsers()) {
            if (user.hasRole(this)) {
                grantees.add(user);
            }
        }
        return grantees;
    }

    public List<DBRole> getRoleGrantees() {
        List<DBRole> grantees = new ArrayList<DBRole>();
        for (DBRole role : getConnectionHandler().getObjectBundle().getRoles()) {
            if (role.hasRole(this)) {
                grantees.add(role);
            }
        }
        return grantees;
    }

    public boolean hasPrivilege(DBPrivilege privilege) {
        for (DBGrantedPrivilege rolePrivilege : getPrivileges()) {
            if (rolePrivilege.getPrivilege().equals(privilege)) {
                return true;
            }
        }
        for (DBGrantedRole inheritedRole : getGrantedRoles()) {
            if (inheritedRole.getRole().hasPrivilege(privilege)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasRole(DBRole role) {
        for (DBGrantedRole inheritedRole : getGrantedRoles()) {
            if (inheritedRole.getRole().equals(role)) {
                return true;
            }
        }
        return false;
    }

    protected List<DBObjectNavigationList> createNavigationLists() {
        List<DBObjectNavigationList> navigationLists = new ArrayList<DBObjectNavigationList>();
        navigationLists.add(new DBObjectNavigationListImpl<DBUser>("User grantees", getUserGrantees()));
        if (getConnectionHandler().getInterfaceProvider().getCompatibilityInterface().supportsObjectType(DBObjectType.ROLE.getTypeId())) {
            navigationLists.add(new DBObjectNavigationListImpl<DBRole>("Role grantees", getRoleGrantees()));
        }
        return navigationLists;
    }

    /*********************************************************
     *                     TreeElement                       *
     *********************************************************/
    @NotNull
    public List<BrowserTreeNode> buildAllPossibleTreeChildren() {
        return DatabaseBrowserUtils.createList(privileges, grantedRoles);
    }

    /*********************************************************
     *                         Loaders                       *
     *********************************************************/
    private static final DynamicContentLoader PRIVILEGES_LOADER = new DBObjectListFromRelationListLoader();
    private static final DynamicContentLoader ROLES_LOADER = new DBObjectListFromRelationListLoader();
}
