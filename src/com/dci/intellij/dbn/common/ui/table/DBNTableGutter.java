package com.dci.intellij.dbn.common.ui.table;

import javax.swing.JList;
import javax.swing.ListCellRenderer;

import com.dci.intellij.dbn.common.dispose.Disposable;
import com.intellij.util.ui.UIUtil;

public abstract class DBNTableGutter<T extends DBNTable> extends JList implements Disposable {
    private boolean disposed;
    private T table;

    public DBNTableGutter(T table) {
        super(table.getModel());
        this.table = table;
        int rowHeight = table.getRowHeight();
        if (rowHeight != 0) setFixedCellHeight(rowHeight);
        setBackground(UIUtil.getPanelBackground());

        setCellRenderer(createCellRenderer());
    }

    protected abstract ListCellRenderer createCellRenderer();

    @Override
    public DBNTableModel getModel() {
        DBNTableModel cachedModel = (DBNTableModel) super.getModel();
        if (table == null) {
            return cachedModel;
        } else {
            DBNTableModel tableModel = table.getModel();
            if (tableModel != cachedModel) {
                setModel(tableModel);
            }
            return tableModel;
        }
    }

    public T getTable() {
        return table;
    }


    @Override
    public boolean isDisposed() {
        return disposed;
    }

    @Override
    public void dispose() {
        disposed = true;
        table = null;
    }
}
