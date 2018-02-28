package com.dci.intellij.dbn.editor.session.model;


import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.model.resultSet.ResultSetColumnInfo;
import com.dci.intellij.dbn.data.model.resultSet.ResultSetDataModelCell;

public class SessionBrowserModelCell extends ResultSetDataModelCell implements ChangeListener {

    public SessionBrowserModelCell(SessionBrowserModelRow row, ResultSet resultSet, ResultSetColumnInfo columnInfo) throws SQLException {
        super(row, resultSet, columnInfo);
    }

    @Override
    public ResultSetColumnInfo getColumnInfo() {
        return (ResultSetColumnInfo) super.getColumnInfo();
    }

    public ConnectionHandler getConnectionHandler() {
        return getRow().getModel().getConnectionHandler();
    }

    public SessionBrowserModelRow getRow() {
        return (SessionBrowserModelRow) super.getRow();
    }

    /*********************************************************
     *                    ChangeListener                     *
     *********************************************************/
    public void stateChanged(ChangeEvent e) {
    }


    /*********************************************************
     *                        ERROR                          *
     *********************************************************/

    @Override
    public void dispose() {
        super.dispose();
    }
}
