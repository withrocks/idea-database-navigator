package com.dci.intellij.dbn.data.model.resultSet;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.model.basic.BasicColumnInfo;
import com.dci.intellij.dbn.data.type.DBDataType;

public class ResultSetColumnInfo extends BasicColumnInfo {
    int resultSetColumnIndex;
    public ResultSetColumnInfo(ConnectionHandler connectionHandler, ResultSet resultSet, int columnIndex) throws SQLException {
        this(columnIndex);
        ResultSetMetaData metaData = resultSet.getMetaData();
        name = translateName(metaData.getColumnName(resultSetColumnIndex), connectionHandler);

        String dataTypeName = metaData.getColumnTypeName(resultSetColumnIndex);
        int precision = getPrecision(metaData);
        int scale = metaData.getScale(resultSetColumnIndex);

        dataType = DBDataType.get(connectionHandler, dataTypeName, precision, precision, scale, false);
    }

    public ResultSetColumnInfo(int columnIndex) {
        super(null, null, columnIndex);
        resultSetColumnIndex = columnIndex + 1;
    }

    public ResultSetColumnInfo(int columnIndex, int resultSetColumnIndex ) {
        super(null, null, columnIndex);
        this.resultSetColumnIndex = resultSetColumnIndex;
    }


    // lenient approach for oracle bug returning the size of LOBs instead of the precision.
    private int getPrecision(ResultSetMetaData metaData) throws SQLException {
        try {
            return metaData.getPrecision(resultSetColumnIndex);
        } catch (NumberFormatException e) {
            return 4000;
        }
    }

    public int getResultSetColumnIndex() {
        return resultSetColumnIndex;
    }

    public String translateName(String name, ConnectionHandler connectionHandler) {
        return name;
    }
}
