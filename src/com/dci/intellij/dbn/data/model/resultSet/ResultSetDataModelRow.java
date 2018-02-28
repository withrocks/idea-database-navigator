package com.dci.intellij.dbn.data.model.resultSet;

import com.dci.intellij.dbn.data.model.ColumnInfo;
import com.dci.intellij.dbn.data.model.sortable.SortableDataModelRow;

import java.sql.ResultSet;
import java.sql.SQLException;


public class ResultSetDataModelRow<T extends ResultSetDataModelCell> extends SortableDataModelRow<T> {
    public ResultSetDataModelRow(ResultSetDataModel model, ResultSet resultSet) throws SQLException {
        super(model);
        for (int i = 0; i < model.getColumnCount(); i++) {
            ResultSetColumnInfo columnInfo = (ResultSetColumnInfo) getModel().getColumnInfo(i);
            T cell = createCell(resultSet, columnInfo);
            addCell(cell);
        }
    }

    @Override
    public ResultSetDataModel getModel() {
        return (ResultSetDataModel) super.getModel();
    }

    protected  T createCell(ResultSet resultSet, ColumnInfo columnInfo) throws SQLException {
        return (T) new ResultSetDataModelCell(this, resultSet, (ResultSetColumnInfo) columnInfo);
    }
}
