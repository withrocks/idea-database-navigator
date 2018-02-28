package com.dci.intellij.dbn.editor.data.model;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

import com.dci.intellij.dbn.data.model.ColumnInfo;
import com.dci.intellij.dbn.data.model.DataModelHeader;
import com.dci.intellij.dbn.data.model.basic.BasicDataModelHeader;
import com.dci.intellij.dbn.editor.data.DatasetEditor;
import com.dci.intellij.dbn.editor.data.state.column.DatasetColumnState;
import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBDataset;

public class DatasetEditorModelHeader extends BasicDataModelHeader implements DataModelHeader {
    public DatasetEditorModelHeader(DatasetEditor datasetEditor, ResultSet resultSet) throws SQLException {
        DBDataset dataset = datasetEditor.getDataset();
        if (dataset != null) {
            if (resultSet == null) {
                List<DatasetColumnState> columnStates = datasetEditor.initColumnStates();

                int index = 0;
                for (DatasetColumnState columnState : columnStates) {
                    DBColumn column = dataset.getColumn(columnState.getName());
                    ColumnInfo columnInfo = new DatasetEditorColumnInfo(column, index, column.getPosition());
                    addColumnInfo(columnInfo);
                    index++;
                }
            } else {
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();
                for (int i = 0; i < columnCount; i++) {
                    String name = metaData.getColumnName(i+1);
                    DBColumn column = dataset.getColumn(name);
                    ColumnInfo columnInfo = new DatasetEditorColumnInfo(column, i, i+1);
                    addColumnInfo(columnInfo);
                }
            }

        }
    }

    public int indexOfColumn(DBColumn column) {
        for (int i=0; i<getColumnCount(); i++) {
            ColumnInfo info = getColumnInfo(i);
            DatasetEditorColumnInfo columnInfo = (DatasetEditorColumnInfo) info;
            DBColumn col = columnInfo.getColumn();
            if (col != null && col.equals(column)) return i;
        }
        return -1;
    }

    private static final Comparator<DBColumn> COLUMN_POSITION_COMPARATOR = new Comparator<DBColumn>() {
        public int compare(DBColumn column1, DBColumn column2) {
            return column1.getPosition()-column2.getPosition();
        }
    };
}
