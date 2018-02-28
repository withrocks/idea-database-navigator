package com.dci.intellij.dbn.data.grid.ui.table.resultSet;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ResultSetTableMouseListener extends MouseAdapter{
    private ResultSetTable table;

    public ResultSetTableMouseListener(ResultSetTable table) {
        this.table = table;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
            table.showRecordViewDialog();
        }

    }
}
