package com.dci.intellij.dbn.object.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.object.DBSystemPrivilege;
import com.dci.intellij.dbn.object.DBUser;
import com.dci.intellij.dbn.object.common.DBObjectType;

public class DBSystemPrivilegeImpl extends DBPrivilegeImpl implements DBSystemPrivilege {

    public DBSystemPrivilegeImpl(ConnectionHandler connectionHandler, ResultSet resultSet) throws SQLException {
        super(connectionHandler, resultSet);
    }

    public DBObjectType getObjectType() {
        return DBObjectType.SYSTEM_PRIVILEGE;
    }

    public List<DBUser> getUserGrantees() {
        List<DBUser> grantees = new ArrayList<DBUser>();
        for (DBUser user : getConnectionHandler().getObjectBundle().getUsers()) {
            if (user.hasSystemPrivilege(this)) {
                grantees.add(user);
            }
        }
        return grantees;
    }
}
