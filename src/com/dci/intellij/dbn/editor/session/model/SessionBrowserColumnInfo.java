package com.dci.intellij.dbn.editor.session.model;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.model.resultSet.ResultSetColumnInfo;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.editor.session.SessionBrowserFilterType;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SessionBrowserColumnInfo extends ResultSetColumnInfo{
    public SessionBrowserColumnInfo(ConnectionHandler connectionHandler, ResultSet resultSet, int columnIndex) throws SQLException {
        super(connectionHandler, resultSet, columnIndex);
    }

    @Override
    public String translateName(String columnName, ConnectionHandler connectionHandler) {
        DatabaseCompatibilityInterface compatibilityInterface = connectionHandler.getInterfaceProvider().getCompatibilityInterface();
        return compatibilityInterface.getSessionBrowserColumnName(columnName);
    }

    public SessionBrowserFilterType getFilterType() {
        String name = getName();
        if ("USER".equalsIgnoreCase(name)) return SessionBrowserFilterType.USER;
        if ("HOST".equalsIgnoreCase(name)) return SessionBrowserFilterType.HOST;
        if ("STATUS".equalsIgnoreCase(name)) return SessionBrowserFilterType.STATUS;
        return null;
    }
}
