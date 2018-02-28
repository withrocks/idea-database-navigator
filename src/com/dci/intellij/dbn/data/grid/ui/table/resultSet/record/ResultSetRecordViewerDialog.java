package com.dci.intellij.dbn.data.grid.ui.table.resultSet.record;

import javax.swing.Action;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.data.grid.ui.table.resultSet.ResultSetTable;

public class ResultSetRecordViewerDialog extends DBNDialog<ResultSetRecordViewerForm> {
    public ResultSetRecordViewerDialog(ResultSetTable table, boolean showDataTypes) {
        super(table.getProject(), "View Record", true);
        setModal(true);
        setResizable(true);
        component = new ResultSetRecordViewerForm(this, table, showDataTypes);
        getCancelAction().putValue(Action.NAME, "Close");
        init();
    }


    @Override
    public JComponent getPreferredFocusedComponent() {
        return component.getPreferredFocusedComponent();
    }

    @NotNull
    protected final Action[] createActions() {
        return new Action[]{
                getCancelAction(),
                getHelpAction()
        };
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
    }
}
