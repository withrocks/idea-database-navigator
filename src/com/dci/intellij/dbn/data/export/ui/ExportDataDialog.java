package com.dci.intellij.dbn.data.export.ui;

import javax.swing.Action;
import java.awt.event.ActionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.thread.ModalTask;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.export.DataExportInstructions;
import com.dci.intellij.dbn.data.export.DataExportManager;
import com.dci.intellij.dbn.data.grid.ui.table.resultSet.ResultSetTable;
import com.dci.intellij.dbn.execution.ExecutionResult;
import com.dci.intellij.dbn.object.common.DBObject;
import com.intellij.openapi.progress.ProgressIndicator;

public class ExportDataDialog extends DBNDialog<ExportDataForm> {
    private ResultSetTable table;
    private ConnectionHandler connectionHandler;

    public ExportDataDialog(ResultSetTable table, @NotNull DBObject sourceObject) {
        this(table, sourceObject, sourceObject.getConnectionHandler());
    }

    public ExportDataDialog(ResultSetTable table, @NotNull ExecutionResult executionResult) {
        this(table, null, executionResult.getConnectionHandler());
    }


    private ExportDataDialog(ResultSetTable table, @Nullable DBObject sourceObject, ConnectionHandler connectionHandler) {
        super(connectionHandler.getProject(), "Export Data", true);
        this.table = table;
        this.connectionHandler = connectionHandler;
        DataExportManager exportManager = DataExportManager.getInstance(connectionHandler.getProject());
        DataExportInstructions instructions = exportManager.getExportInstructions();
        boolean hasSelection = table.getSelectedRowCount() > 1 || table.getSelectedColumnCount() > 1;
        instructions.setBaseName(table.getName());
        component = new ExportDataForm(this, instructions, hasSelection, connectionHandler, sourceObject);
        init();
    }


    @NotNull
    @Override
    protected Action[] createActions() {
        return new Action[]{
                new DialogWrapperAction("Export") {
                    @Override
                    protected void doAction(ActionEvent actionEvent) {
                        doOKAction();
                    }
                },
                getCancelAction()};
    }

    protected void doOKAction() {
        component.validateEntries(new ModalTask(getProject(), "Creating export file", true) {
                                           @Override
                                           protected void execute(@NotNull ProgressIndicator progressIndicator) {
                                               DataExportManager exportManager = DataExportManager.getInstance(connectionHandler.getProject());
                                               DataExportInstructions exportInstructions = component.getExportInstructions();
                                               exportManager.setExportInstructions(exportInstructions);
                                               exportManager.exportSortableTableContent(
                                                       table,
                                                       exportInstructions,
                                                       connectionHandler,
                                                       new SimpleLaterInvocator() {
                                                           @Override
                                                           protected void execute() {
                                                               ExportDataDialog.super.doOKAction();
                                                           }
                                                       });
                                           }

                                       }
        );
    }

    @Override
    public void dispose() {
        super.dispose();
        connectionHandler = null;
    }
}
