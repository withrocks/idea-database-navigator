package com.dci.intellij.dbn.execution.method.result.action;

import java.sql.SQLException;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.data.grid.ui.table.resultSet.ResultSetTable;
import com.dci.intellij.dbn.data.model.resultSet.ResultSetDataModel;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.execution.method.result.MethodExecutionResult;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;

public class CursorResultFetchNextRecordsAction extends DumbAwareAction {
    private ResultSetTable<? extends ResultSetDataModel> table;
    private MethodExecutionResult executionResult;
    public CursorResultFetchNextRecordsAction(MethodExecutionResult executionResult, ResultSetTable<? extends ResultSetDataModel> table) {
        super("Fetch next records", null, Icons.EXEC_RESULT_RESUME);
        this.table = table;
        this.executionResult = executionResult;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        try {
            ResultSetDataModel model = table.getModel();
            if (!model.isResultSetExhausted()) {
                ExecutionEngineSettings settings = ExecutionEngineSettings.getInstance(executionResult.getProject());
                int fetchBlockSize = settings.getStatementExecutionSettings().getResultSetFetchBlockSize();

                model.fetchNextRecords(fetchBlockSize, false);
            }

        } catch (SQLException ex) {
            Project project = getEventProject(e);
            MessageUtil.showErrorDialog(project, "Could not perform operation.", ex);
        }

    }

    @Override
    public void update(AnActionEvent e) {
        ResultSetDataModel model = table.getModel();
        boolean enabled = !model.isResultSetExhausted();
        e.getPresentation().setEnabled(enabled);
    }
}
