package com.dci.intellij.dbn.editor.data.filter;

import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBDataset;
import gnu.trove.THashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DatasetFilterInput {
    private DBDataset dataset;
    private List<DBColumn> columns = new ArrayList<DBColumn>();
    private Map<DBColumn, Object> values = new THashMap<DBColumn, Object>();

    public DatasetFilterInput(DBDataset dataset) {
        this.dataset = dataset;
    }

    public DBDataset getDataset() {
        return dataset;
    }

    public List<DBColumn> getColumns() {
        return columns;
    }

    public void setColumnValue(DBColumn column, Object value) {
        columns.add(column);
        values.put(column, value);
    }

    public Object getColumnValue(DBColumn column) {
        return values.get(column);
    }
}
