package com.dci.intellij.dbn.data.model.resultSet;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.dci.intellij.dbn.data.model.sortable.SortableDataModelCell;
import com.dci.intellij.dbn.data.type.DBDataType;

public class ResultSetDataModelCell extends SortableDataModelCell {
    public ResultSetDataModelCell(ResultSetDataModelRow row, ResultSet resultSet, ResultSetColumnInfo columnInfo) throws SQLException {
        super(row, null, columnInfo.getColumnIndex());
        DBDataType dataType = columnInfo.getDataType();
        if (!getRow().getModel().isInserting()) {
            Object userValue = dataType.getValueFromResultSet(resultSet, columnInfo.getResultSetColumnIndex());
            setUserValue(userValue);
        }
    }

    @Override
    public ResultSetDataModelRow getRow() {
        return (ResultSetDataModelRow) super.getRow();
    }
}
