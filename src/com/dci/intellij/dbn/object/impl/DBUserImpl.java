package com.dci.intellij.dbn.object.impl;

import javax.swing.Icon;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.browser.DatabaseBrowserUtils;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.ui.HtmlToolTipBuilder;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoader;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.object.DBGrantedPrivilege;
import com.dci.intellij.dbn.object.DBGrantedRole;
import com.dci.intellij.dbn.object.DBRole;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.DBSystemPrivilege;
import com.dci.intellij.dbn.object.DBUser;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import com.dci.intellij.dbn.object.common.DBObjectImpl;
import com.dci.intellij.dbn.object.common.DBObjectRelationType;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationList;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationListImpl;
import com.dci.intellij.dbn.object.common.list.loader.DBObjectListFromRelationListLoader;

public class DBUserImpl extends DBObjectImpl implements DBUser {
    DBObjectList<DBGrantedRole> roles;
    DBObjectList<DBGrantedPrivilege> privileges;

    private boolean isExpired;
    private boolean isLocked;

    public DBUserImpl(ConnectionHandler connectionHandler, ResultSet resultSet) throws SQLException {
        super(connectionHandler.getObjectBundle(), resultSet);
    }

    @Override
    public DBUser getOwner() {
        return this;
    }

    @Override
    protected void initObject(ResultSet resultSet) throws SQLException {
        name = resultSet.getString("USER_NAME");
        isExpired = resultSet.getString("IS_EXPIRED").equals("Y");
        isLocked = resultSet.getString("IS_LOCKED").equals("Y");
    }

    @Override
    protected void initLists() {
        DBObjectListContainer childObjects = initChildObjects();
        DBObjectBundle sourceContentHolder = getConnectionHandler().getObjectBundle();
        roles = childObjects.createSubcontentObjectList(DBObjectType.GRANTED_ROLE, this, ROLES_LOADER, sourceContentHolder, DBObjectRelationType.USER_ROLE, true);
        privileges = childObjects.createSubcontentObjectList(DBObjectType.GRANTED_PRIVILEGE, this, PRIVILEGES_LOADER, sourceContentHolder, DBObjectRelationType.USER_PRIVILEGE, true);
    }


    public DBObjectType getObjectType() {
        return DBObjectType.USER;
    }

    public DBSchema getSchema() {
        return getObjectBundle().getSchema(name);
    }

    public boolean isExpired() {
        return isExpired;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return isExpired ?
               (isLocked ? Icons.DBO_USER_EXPIRED_LOCKED : Icons.DBO_USER_EXPIRED) :
               (isLocked ? Icons.DBO_USER_LOCKED : Icons.DBO_USER);
    }

    @Override
    public DBObject getDefaultNavigationObject() {
        return getSchema();
    }

    public boolean isLocked() {
        return isLocked;
    }

    public List<DBGrantedPrivilege> getPrivileges() {
        return privileges.getObjects();
    }

    public List<DBGrantedRole> getRoles() {
        return roles.getObjects();
    }

    public boolean hasSystemPrivilege(DBSystemPrivilege systemPrivilege) {
        for (DBGrantedPrivilege grantedPrivilege : getPrivileges()) {
            if (grantedPrivilege.getPrivilege().equals(systemPrivilege)) {
                return true;
            }
        }
        DatabaseCompatibilityInterface compatibilityInterface = getConnectionHandler().getInterfaceProvider().getCompatibilityInterface();
        if (compatibilityInterface.supportsObjectType(DBObjectType.GRANTED_ROLE.getTypeId())) {
            for (DBGrantedRole grantedRole : getRoles()) {
                if (grantedRole.getRole().hasPrivilege(systemPrivilege)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasRole(DBRole role) {
        for (DBGrantedRole grantedRole : getRoles()) {
            if (grantedRole.getRole().equals(role)) {
                return true;
            }
        }
        return false;
    }

    public void buildToolTip(HtmlToolTipBuilder ttb) {
        ttb.append(true, getObjectType().getName(), true);
        if (isLocked || isExpired) {
            if (isLocked && isExpired)
                ttb.append(false, " - expired & locked" , true);
            else if (isLocked)
                ttb.append(false, " - locked" , true); else
                ttb.append(false, " - expired" , true);


        }

        ttb.createEmptyRow();
        super.buildToolTip(ttb);
    }

    @Override
    protected List<DBObjectNavigationList> createNavigationLists() {
        DBSchema schema = getSchema();
        if(schema != null) {
            List<DBObjectNavigationList> objectNavigationLists = new ArrayList<DBObjectNavigationList>();
            objectNavigationLists.add(new DBObjectNavigationListImpl("Schema", schema));
            return objectNavigationLists;
        }
        return null;
    }

    /*********************************************************
     *                     TreeElement                       *
     *********************************************************/
    @NotNull
    public List<BrowserTreeNode> buildAllPossibleTreeChildren() {
        return DatabaseBrowserUtils.createList(roles, privileges);
    }

    /*********************************************************
     *                         Loaders                       *
     *********************************************************/
    private static final DynamicContentLoader ROLES_LOADER = new DBObjectListFromRelationListLoader();
    private static final DynamicContentLoader PRIVILEGES_LOADER = new DBObjectListFromRelationListLoader();
}
