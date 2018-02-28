package com.dci.intellij.dbn.execution.method.result.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.data.export.ui.ExportDataDialog;
import com.dci.intellij.dbn.data.grid.ui.table.resultSet.ResultSetTable;
import com.dci.intellij.dbn.object.DBArgument;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;

public class CursorResultExportAction extends DumbAwareAction {
    private ResultSetTable table;
    private DBArgument cursorArgument;
    public CursorResultExportAction(ResultSetTable table, DBArgument cursorArgument) {
        super("Export Data", null, Icons.DATA_EXPORT);
        this.cursorArgument = cursorArgument;
        this.table = table;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        ExportDataDialog dialog = new ExportDataDialog(table, cursorArgument);
        dialog.show();
    }
}
