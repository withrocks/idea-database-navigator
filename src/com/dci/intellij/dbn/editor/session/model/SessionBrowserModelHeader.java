package com.dci.intellij.dbn.editor.session.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.model.DataModelHeader;
import com.dci.intellij.dbn.data.model.resultSet.ResultSetColumnInfo;
import com.dci.intellij.dbn.data.model.resultSet.ResultSetDataModelHeader;

public class SessionBrowserModelHeader extends ResultSetDataModelHeader implements DataModelHeader {
    public SessionBrowserModelHeader() {
    }

    public SessionBrowserModelHeader(ConnectionHandler connectionHandler, ResultSet resultSet) throws SQLException {
        super(connectionHandler, resultSet);
    }

    @NotNull
    @Override
    public ResultSetColumnInfo createColumnInfo(ConnectionHandler connectionHandler, ResultSet resultSet, int columnIndex) throws SQLException {
        return new SessionBrowserColumnInfo(connectionHandler, resultSet, columnIndex);
    }
}
