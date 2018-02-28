package com.dci.intellij.dbn.editor.session.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.model.ColumnInfo;
import com.dci.intellij.dbn.data.model.resultSet.ResultSetColumnInfo;
import com.dci.intellij.dbn.data.model.resultSet.ResultSetDataModelRow;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.editor.session.SessionStatus;

public class SessionBrowserModelRow extends ResultSetDataModelRow<SessionBrowserModelCell> {

    public SessionBrowserModelRow(SessionBrowserModel model, ResultSet resultSet) throws SQLException {
        super(model, resultSet);
    }

    @Override
    public SessionBrowserModel getModel() {
        return (SessionBrowserModel) super.getModel();
    }

    @Override
    protected SessionBrowserModelCell createCell(ResultSet resultSet, ColumnInfo columnInfo) throws SQLException {
        return new SessionBrowserModelCell(this, resultSet, (ResultSetColumnInfo) columnInfo);
    }

    public ResultSet getResultSet() {
        return getModel().getResultSet();
    }

    public String getUser() {
        return (String) getCellValue("USER");
    }

    public String getHost() {
        return (String) getCellValue("HOST");
    }

    public String getStatus() {
        return (String) getCellValue("STATUS");
    }

    public Object getSessionId() {
        return getCellValue("SESSION_ID");
    }

    public Object getSerialNumber() {
        return getCellValue("SERIAL_NUMBER");
    }

    public String getSchema() {
        return (String) getCellValue("SCHEMA");
    }

    public SessionStatus getSessionStatus() {
        ConnectionHandler connectionHandler = getModel().getConnectionHandler();
        if (connectionHandler != null) {
            DatabaseCompatibilityInterface compatibilityInterface = connectionHandler.getInterfaceProvider().getCompatibilityInterface();
            return compatibilityInterface.getSessionStatus(getStatus());
        }
        return SessionStatus.INACTIVE;
    }

}
