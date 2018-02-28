package com.dci.intellij.dbn.object.common.loader;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionUtil;
import com.dci.intellij.dbn.object.common.DBObject;
import com.intellij.openapi.diagnostic.Logger;

public abstract class DBSourceCodeLoader {
    protected Logger logger = Logger.getInstance(getClass().getName());

    private DBObject object;
    private boolean lenient;

    protected DBSourceCodeLoader(DBObject object, boolean lenient) {
        this.object = object;
        this.lenient = lenient;
    }

    public String load() throws SQLException {
        Connection connection = null;
        ResultSet resultSet = null;
        ConnectionHandler connectionHandler = object.getConnectionHandler();
        try {
            connection = connectionHandler.getPoolConnection();
            resultSet = loadSourceCode(connection);
                                                 
            StringBuilder sourceCode = new StringBuilder();
            while (resultSet.next()) {
                String codeLine = resultSet.getString(1);
                sourceCode.append(codeLine);
            }

            if (sourceCode.length() == 0 && !lenient)
                throw new SQLException("Object not found in database.");

            return StringUtil.removeCharacter(sourceCode.toString(), '\r');
        } finally {
            ConnectionUtil.closeResultSet(resultSet);
            connectionHandler.freePoolConnection(connection);
        }
    }

    @Nullable
    public abstract ResultSet loadSourceCode(Connection connection) throws SQLException;
}
