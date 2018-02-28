package com.dci.intellij.dbn.data.model.sortable;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import com.dci.intellij.dbn.data.grid.ui.table.sortable.SortableTable;

public class SortableTableMouseListener extends MouseAdapter{
    private SortableTable table;

    public SortableTableMouseListener(SortableTable table) {
        this.table = table;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
    }
}
