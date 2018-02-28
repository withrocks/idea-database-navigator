package com.dci.intellij.dbn.execution.statement.result;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.action.DBNDataKeys;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.grid.ui.table.resultSet.ResultSetTable;
import com.dci.intellij.dbn.data.model.resultSet.ResultSetDataModel;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.execution.statement.options.StatementExecutionSettings;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionCursorProcessor;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.dci.intellij.dbn.execution.statement.result.ui.StatementExecutionResultForm;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.util.Disposer;

public class StatementExecutionCursorResult extends StatementExecutionBasicResult {
    private StatementExecutionResultForm resultPanel;
    private ResultSetDataModel dataModel;

    public StatementExecutionCursorResult(
            @NotNull StatementExecutionProcessor executionProcessor,
            @NotNull String resultName,
            ResultSet resultSet,
            int updateCount) throws SQLException {
        super(executionProcessor, resultName, updateCount);
        int fetchBlockSize = getQueryExecutionSettings().getResultSetFetchBlockSize();
        dataModel = new ResultSetDataModel(resultSet, executionProcessor.getConnectionHandler(), fetchBlockSize);
        resultPanel = new StatementExecutionResultForm(this);
        resultPanel.updateVisibleComponents();

        Disposer.register(this, dataModel);
    }

    private StatementExecutionSettings getQueryExecutionSettings() {
        ExecutionEngineSettings settings = ExecutionEngineSettings.getInstance(getProject());
        return settings.getStatementExecutionSettings();
    }

    public StatementExecutionCursorResult(
            StatementExecutionProcessor executionProcessor,
            @NotNull String resultName,
            int updateCount) throws SQLException {
        super(executionProcessor, resultName, updateCount);
    }

    public StatementExecutionCursorProcessor getExecutionProcessor() {
        return (StatementExecutionCursorProcessor) super.getExecutionProcessor();
    }

    public void reload() {
        new BackgroundTask(getProject(), "Reloading data", true) {
            public void execute(@NotNull ProgressIndicator progressIndicator) {
                initProgressIndicator(progressIndicator, true, "Reloading results for " + getExecutionProcessor().getStatementName());

                resultPanel.highlightLoading(true);
                long startTimeMillis = System.currentTimeMillis();
                try {
                    ConnectionHandler connectionHandler = getConnectionHandler();
                    if (connectionHandler != null) {
                        Connection connection = connectionHandler.getStandaloneConnection(getExecutionProcessor().getCurrentSchema());
                        Statement statement = connection.createStatement();
                        statement.setQueryTimeout(getQueryExecutionSettings().getExecutionTimeout());
                        statement.execute(getExecutionInput().getExecutableStatementText());
                        ResultSet resultSet = statement.getResultSet();
                        loadResultSet(resultSet);
                    }
                } catch (final SQLException e) {
                    MessageUtil.showErrorDialog(getProject(), "Could not perform reload operation.", e);
                }
                setExecutionDuration((int) (System.currentTimeMillis() - startTimeMillis));
                resultPanel.highlightLoading(false);
            }
        }.start();
    }

    public void loadResultSet(ResultSet resultSet) throws SQLException {
        int rowCount = Math.max(dataModel.getRowCount() + 1, 100);
        dataModel = new ResultSetDataModel(resultSet, getConnectionHandler(), rowCount);
        resultPanel.reloadTableModel();
        resultPanel.updateVisibleComponents();
    }

    public StatementExecutionResultForm getForm(boolean create) {
        return resultPanel;
    }

    public void fetchNextRecords() {
        new BackgroundTask(getProject(), "Loading data", true) {
            public void execute(@NotNull ProgressIndicator progressIndicator) {
                initProgressIndicator(progressIndicator, true, "Loading next records for " + getExecutionProcessor().getStatementName());
                resultPanel.highlightLoading(true);
                try {
                    if (hasResult() && !dataModel.isResultSetExhausted()) {
                        int fetchBlockSize = getQueryExecutionSettings().getResultSetFetchBlockSize();
                        dataModel.fetchNextRecords(fetchBlockSize, false);
                        //tResult.accommodateColumnsSize();
                        if (dataModel.isResultSetExhausted()) {
                            dataModel.closeResultSet();
                        }
                        resultPanel.updateVisibleComponents();
                    }

                } catch (SQLException e) {
                    MessageUtil.showErrorDialog(getProject(), "Could not perform operation.", e);
                } finally {
                    resultPanel.highlightLoading(false);
                }

            }
        }.start();
    }

    public ResultSetDataModel getTableModel() {
        return dataModel;
    }

    @Nullable
    public ResultSetTable getResultTable() {
        return resultPanel == null ? null : resultPanel.getResultTable();
    }

    public boolean hasResult() {
        return dataModel != null;
    }

    public void navigateToResult() {
        if (resultPanel != null) {
            resultPanel.show();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        dataModel = null;
        resultPanel = null;
        dataProvider = null;
    }


    /********************************************************
     *                    Data Provider                     *
     ********************************************************/
    public DataProvider dataProvider = new DataProvider() {
        @Override
        public Object getData(@NonNls String dataId) {
            if (DBNDataKeys.STATEMENT_EXECUTION_RESULT.is(dataId)) {
                return StatementExecutionCursorResult.this;
            }
            if (PlatformDataKeys.PROJECT.is(dataId)) {
                return getProject();
            }
            return null;
        }
    };

    public DataProvider getDataProvider() {
        return dataProvider;
    }

}
