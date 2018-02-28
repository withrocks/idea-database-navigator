package com.dci.intellij.dbn.editor.data.filter.global;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBDataset;
import com.dci.intellij.dbn.object.DBTable;

public class GlobalDataFilter implements SelectStatementFilter{
    
    private DBTable filterTable;
    private DBColumn filterColumn;
    private Object filterValue;

    public GlobalDataFilter(DBColumn filterColumn, Object filterValue) {
        this.filterColumn = filterColumn;
        this.filterValue = filterValue;
        this.filterTable = (DBTable) filterColumn.getDataset();
    }

    public ConnectionHandler getConnectionHandler() {
        return filterColumn.getConnectionHandler();
    }

    public DBDataset getFilterTable() {
        return filterTable;
    }

    public DBColumn getFilterColumn() {
        return filterColumn;
    }

    public Object getFilterValue() {
        return filterValue;
    }

    public String createSelectStatement(DBDataset dataset) {
        return null;
    }

    private void buildDependencyLink(DBTable fromTable, DBTable toTable) {
        for (DBColumn column : fromTable.getForeignKeyColumns()) {
            
        }
    }
}
