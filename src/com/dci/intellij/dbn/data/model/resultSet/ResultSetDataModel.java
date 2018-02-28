package com.dci.intellij.dbn.data.model.resultSet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.thread.SimpleBackgroundTask;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionUtil;
import com.dci.intellij.dbn.data.model.sortable.SortableDataModel;
import com.dci.intellij.dbn.data.model.sortable.SortableDataModelState;
import com.dci.intellij.dbn.database.DatabaseInterface;
import com.intellij.openapi.util.Disposer;

public class ResultSetDataModel<T extends ResultSetDataModelRow> extends SortableDataModel<T> {
    protected ResultSet resultSet;
    protected ConnectionHandler connectionHandler;
    protected boolean resultSetExhausted = false;
    protected final Object DISPOSE_LOCK = new Object();

    public ResultSetDataModel(ConnectionHandler connectionHandler) {
        super(connectionHandler.getProject());
        this.connectionHandler = connectionHandler;
    }

    public ResultSetDataModel(ResultSet resultSet, ConnectionHandler connectionHandler, int maxRecords) throws SQLException {
        super(connectionHandler.getProject());
        this.connectionHandler = connectionHandler;
        this.resultSet = resultSet;
        setHeader(new ResultSetDataModelHeader(connectionHandler, resultSet));
        fetchNextRecords(maxRecords, false);
    }

    protected T createRow(int resultSetRowIndex) throws SQLException {
        return (T) new ResultSetDataModelRow(this, resultSet);
    }

    public ResultSet getResultSet() {
        return resultSet;
    }

    public int fetchNextRecords(int records, boolean reset) throws SQLException {
        checkDisposed();
        int originalRowCount = getRowCount();
        if (resultSetExhausted) return originalRowCount;

        int initialIndex = reset ? 0 : originalRowCount;
        int count = 0;

        final List<T> oldRows = getRows();
        List<T> newRows = reset ? new ArrayList<T>(oldRows.size()) : new ArrayList<T>(oldRows);

        if (resultSet == null) {
            resultSetExhausted = true;
        } else {
            while (count < records) {
                synchronized (DISPOSE_LOCK) {
                    checkDisposed();
                    if (resultSet.next()) {
                        count++;
                        T row = createRow(initialIndex + count);
                        newRows.add(row);
                    } else {
                        resultSetExhausted = true;
                        break;
                    }
                }
            }
        }

        synchronized (DISPOSE_LOCK) {
            checkDisposed();

            sort(newRows);
            setRows(newRows);

            if (reset) {
                disposeRows(oldRows);
            }

            int newRowCount = getRowCount();
            if (newRowCount > originalRowCount) notifyRowsInserted(originalRowCount, newRowCount);
            if (newRowCount < originalRowCount) notifyRowsDeleted(newRowCount, originalRowCount);
            int updateIndex = Math.min(originalRowCount, newRowCount);
            if (updateIndex > 0) notifyRowsUpdated(0, updateIndex);

            return newRowCount;
        }
    }

    private void disposeRows(final List<T> oldRows) {
        new SimpleBackgroundTask("dispose result-set mocel") {
            @Override
            public void execute() {
                // dispose old content
                for (T row : oldRows) {
                    disposeRow(row);
                }
            }
        }.start();
    }

    protected void checkDisposed() throws SQLException {
        if (isDisposed()) throw DatabaseInterface.DBN_INTERRUPTED_EXCEPTION;
    }

    protected void disposeRow(T row) {
        Disposer.dispose(row);
    }

    public boolean isResultSetExhausted() {
        return resultSetExhausted;
    }

    public void closeResultSet() {
        ConnectionUtil.closeResultSet(resultSet);
    }

    @Nullable
    public ConnectionHandler getConnectionHandler() {
        return connectionHandler;
    }

    @Override
    protected SortableDataModelState createState() {
        return new SortableDataModelState();
    }

    public boolean isReadonly() {
        return true;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return getColumnInfo(columnIndex).getDataType().getTypeClass();
    }

    @Override
    public void dispose() {
        if (!isDisposed()) {
            synchronized (DISPOSE_LOCK) {
                super.dispose();
                closeResultSet();
                resultSet = null;
                connectionHandler = null;
            }
        }
    }

    public boolean isInserting() {
        return false;
    }
}
