package com.dci.intellij.dbn.execution.method.result.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.data.grid.ui.table.resultSet.ResultSetTable;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;

public class CursorResultViewRecordAction extends DumbAwareAction {
    private ResultSetTable table;
    public CursorResultViewRecordAction(ResultSetTable table) {
        super("View record", null, Icons.EXEC_RESULT_VIEW_RECORD);
        this.table = table;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        table.showRecordViewDialog();
    }

    @Override
    public void update(AnActionEvent e) {
        boolean enabled = table.getSelectedColumn() > -1;
        e.getPresentation().setEnabled(enabled);
    }
}
