package com.dci.intellij.dbn.data.export.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.data.export.ui.ExportDataDialog;
import com.dci.intellij.dbn.data.grid.ui.table.resultSet.ResultSetTable;
import com.dci.intellij.dbn.object.DBDataset;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class ExportDataAction extends AnAction {
    private ResultSetTable table;
    private DBDataset dataset;

    public ExportDataAction(ResultSetTable table, DBDataset dataset) {
        super("Export Data", null, Icons.DATA_EXPORT);
        this.table = table;
        this.dataset = dataset;
    }

    public void actionPerformed(AnActionEvent e) {
        ExportDataDialog dialog = new ExportDataDialog(table, dataset);
        dialog.show();
    }
}
