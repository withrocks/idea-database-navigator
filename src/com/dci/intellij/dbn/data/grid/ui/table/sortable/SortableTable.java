package com.dci.intellij.dbn.data.grid.ui.table.sortable;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.data.grid.ui.table.basic.BasicTable;
import com.dci.intellij.dbn.data.grid.ui.table.basic.BasicTableSpeedSearch;
import com.dci.intellij.dbn.data.model.ColumnInfo;
import com.dci.intellij.dbn.data.model.sortable.SortableDataModel;
import com.dci.intellij.dbn.data.model.sortable.SortableTableHeaderMouseListener;
import com.dci.intellij.dbn.data.model.sortable.SortableTableMouseListener;
import com.dci.intellij.dbn.data.sorting.SortDirection;
import com.intellij.openapi.diagnostic.Logger;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;

public abstract class SortableTable<T extends SortableDataModel> extends BasicTable<T> {
    protected Logger logger = LoggerFactory.createLogger();

    public SortableTable(T dataModel, boolean enableSpeedSearch) {
        super(dataModel.getProject(), dataModel);
        addMouseListener(new SortableTableMouseListener(this));
        JTableHeader tableHeader = getTableHeader();
        tableHeader.setDefaultRenderer(new SortableTableHeaderRenderer());
        tableHeader.addMouseListener(new SortableTableHeaderMouseListener(this));

        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setCellSelectionEnabled(true);
        accommodateColumnsSize();
        if (enableSpeedSearch) {
            new BasicTableSpeedSearch(this);
        }
    }

    public void sort() {
        getModel().sort();
        JTableHeader tableHeader = getTableHeader();
        tableHeader.revalidate();
        tableHeader.repaint();
    }

    public boolean sort(int columnIndex, SortDirection sortDirection, boolean keepExisting) {
        SortableDataModel model = getModel();
        int modelColumnIndex = convertColumnIndexToModel(columnIndex);
        ColumnInfo columnInfo = getModel().getColumnInfo(modelColumnIndex);
        if (columnInfo.isSortable()) {
            boolean sorted = model.sort(modelColumnIndex, sortDirection, keepExisting);
            if (sorted) {
                JTableHeader tableHeader = getTableHeader();
                tableHeader.revalidate();
                tableHeader.repaint();
            }
            return sorted;
        }
        return false;
    }

}
