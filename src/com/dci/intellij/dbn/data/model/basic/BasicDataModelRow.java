package com.dci.intellij.dbn.data.model.basic;

import java.util.ArrayList;
import java.util.List;

import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.data.model.DataModelCell;
import com.dci.intellij.dbn.data.model.DataModelRow;
import com.intellij.openapi.project.Project;

public class BasicDataModelRow<T extends DataModelCell> implements DataModelRow<T> {
    protected BasicDataModel model;
    protected List<T> cells;
    private int index;

    public BasicDataModelRow(BasicDataModel model) {
        cells = new ArrayList<T>(model.getColumnCount());
        this.model = model;
    }

    protected void addCell(T cell) {
        cells.add(cell);
    }

    public BasicDataModel getModel() {
        return model;
    }

    public List<T> getCells() {
        return cells;
    }


    @Override
    public final T getCell(String columnName) {
        List<T> cells = this.cells;
        if (cells != null) {
            for (T cell : cells) {
                if (cell.getColumnInfo().getName().equals(columnName)) {
                    return cell;
                }
            }
        }
        return null;
    }

    @Override
    public final Object getCellValue(String columnName) {
        T cell = getCell(columnName);
        if (cell != null) {
            return cell.getUserValue();
        }
        return null;
    }



    public T getCellAtIndex(int index) {
        return cells.get(index);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int indexOf(T cell) {
        return cells.indexOf(cell);
    }


    /********************************************************
     *                    Disposable                        *
     ********************************************************/
    private boolean disposed;

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    public void dispose() {
        if (!disposed) {
            disposed = true;
            DisposerUtil.dispose(cells);
            cells = null;
            model = null;
        }
    }

    public Project getProject() {
        return model == null ? null : model.getProject();
    }
}
