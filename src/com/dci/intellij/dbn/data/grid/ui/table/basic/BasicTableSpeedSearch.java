package com.dci.intellij.dbn.data.grid.ui.table.basic;

import com.dci.intellij.dbn.data.model.ColumnInfo;
import com.dci.intellij.dbn.data.model.DataModelHeader;
import com.dci.intellij.dbn.data.model.basic.BasicDataModel;
import com.intellij.ui.SpeedSearchBase;

public class BasicTableSpeedSearch extends SpeedSearchBase<BasicTable<? extends BasicDataModel>> {

    private ColumnInfo[] columnInfos;
    private int columnIndex = 0;

    public BasicTableSpeedSearch(BasicTable<? extends BasicDataModel> table) {
        super(table);
    }

    BasicTable<? extends BasicDataModel> getTable() {
        return getComponent();
    }

    protected int getSelectedIndex() {
        return columnIndex;
    }

    protected Object[] getAllElements() {
        if (columnInfos == null) {
            DataModelHeader modelHeader = getTable().getModel().getHeader();
            columnInfos = modelHeader.getColumnInfos().toArray(new ColumnInfo[modelHeader.getColumnCount()]);
        }
        return columnInfos;
    }

    protected String getElementText(Object o) {
        ColumnInfo columnInfo = (ColumnInfo) o;
        return columnInfo.getName();
    }

    protected void selectElement(Object o, String s) {
        for(ColumnInfo columnInfo : columnInfos) {
            if (columnInfo == o) {
                columnIndex = columnInfo.getColumnIndex();
                BasicTable table = getTable();
                int rowIndex = table.getSelectedRow();
                if (rowIndex == -1) rowIndex = 0;
                table.scrollRectToVisible(table.getCellRect(rowIndex, columnIndex, true));
                table.setColumnSelectionInterval(columnIndex, columnIndex);
                break;
            }
        }
    }

    
}