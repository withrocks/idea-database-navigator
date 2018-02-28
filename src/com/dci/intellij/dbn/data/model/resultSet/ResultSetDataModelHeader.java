package com.dci.intellij.dbn.data.model.resultSet;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.model.ColumnInfo;
import com.dci.intellij.dbn.data.model.DataModelHeader;
import com.dci.intellij.dbn.data.model.basic.BasicDataModelHeader;

public class ResultSetDataModelHeader extends BasicDataModelHeader implements DataModelHeader {

    public ResultSetDataModelHeader() {
    }

    public ResultSetDataModelHeader(ConnectionHandler connectionHandler, ResultSet resultSet) throws SQLException {
        super();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        for (int i = 0; i < columnCount; i++) {
            ColumnInfo columnInfo = createColumnInfo(connectionHandler, resultSet, i);
            addColumnInfo(columnInfo);
        }
    }

    @NotNull
    public ResultSetColumnInfo createColumnInfo(ConnectionHandler connectionHandler, ResultSet resultSet, int columnIndex) throws SQLException {
        return new ResultSetColumnInfo(connectionHandler, resultSet, columnIndex);
    }
}
