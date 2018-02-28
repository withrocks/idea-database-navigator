package com.dci.intellij.dbn.editor.data.model;


import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellEditor;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.locale.Formatter;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.model.resultSet.ResultSetDataModelCell;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.data.value.ValueAdapter;
import com.dci.intellij.dbn.editor.EditorProviderId;
import com.dci.intellij.dbn.editor.data.DatasetEditorError;
import com.dci.intellij.dbn.editor.data.ui.DatasetEditorErrorForm;
import com.dci.intellij.dbn.editor.data.ui.table.DatasetEditorTable;
import com.dci.intellij.dbn.editor.data.ui.table.cell.DatasetTableCellEditor;
import com.dci.intellij.dbn.object.DBDataset;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;

public class DatasetEditorModelCell extends ResultSetDataModelCell implements ChangeListener {
    private Object originalUserValue;
    private DatasetEditorError error;
    private boolean isModified;

    public DatasetEditorModelCell(DatasetEditorModelRow row, ResultSet resultSet, DatasetEditorColumnInfo columnInfo) throws SQLException {
        super(row, resultSet, columnInfo);
        originalUserValue = getUserValue();
    }

    @Override
    public DatasetEditorColumnInfo getColumnInfo() {
        return (DatasetEditorColumnInfo) super.getColumnInfo();
    }

    public void updateUserValue(Object newUserValue, boolean bulk) {
        boolean valueChanged = userValueChanged(newUserValue);
        if (hasError() || valueChanged) {
            DatasetEditorModelRow row = getRow();
            ResultSet resultSet;
            boolean isInsertRow = row.isInsert();
            try {
                resultSet = isInsertRow ? row.getResultSet() : row.scrollResultSet();
            } catch (Exception e) {
                e.printStackTrace();
                MessageUtil.showErrorDialog(getProject(), "Could not update cell value for " + getColumnInfo().getName() + ".", e);
                return;
            }
            boolean isValueAdapter = userValue instanceof ValueAdapter || newUserValue instanceof ValueAdapter;

            ConnectionHandler connectionHandler = getConnectionHandler();
            try {
                clearError();
                int columnIndex = getColumnInfo().getResultSetColumnIndex();

                if (isValueAdapter) {
                    if (userValue == null) {
                        userValue = newUserValue.getClass().newInstance();
                    }
                    ValueAdapter valueAdapter = (ValueAdapter) userValue;
                    Connection connection = connectionHandler.getStandaloneConnection();
                    if (newUserValue instanceof ValueAdapter) {
                        ValueAdapter newUserValueAdapter = (ValueAdapter) newUserValue;
                        newUserValue = newUserValueAdapter.read();
                    }
                    valueAdapter.write(connection, resultSet, columnIndex, newUserValue);

                } else {
                    DBDataType dataType = getColumnInfo().getDataType();
                    dataType.setValueToResultSet(resultSet, columnIndex, newUserValue);
                }

                if (!isInsertRow) resultSet.updateRow();
            } catch (Exception e) {
                DatasetEditorError error = new DatasetEditorError(connectionHandler, e);

                // error may affect other cells in the row (e.g. foreign key constraint for multiple primary key)
                if (e instanceof SQLException) getRow().notifyError(error, false, !bulk);

                // if error was not notified yet on row level, notify it on cell isolation level
                if (!error.isNotified()) notifyError(error, !bulk);
            } finally {
                if (valueChanged) {
                    if (!isValueAdapter) {
                        setUserValue(newUserValue);
                    }
                    connectionHandler.notifyChanges(getDataset().getVirtualFile());
                    EventManager.notify(getProject(), DatasetEditorModelCellValueListener.TOPIC).valueChanged(this);
                }
                try {
                    if (!isInsertRow) resultSet.refreshRow();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (!isInsertRow && !connectionHandler.isAutoCommit()) {
                isModified = true;
                row.setModified(true);
            }
        }
    }

    public void setUserValueToResultSet(ResultSet resultSet) throws SQLException {
        boolean isValueAdapter = userValue instanceof ValueAdapter;
        int columnIndex = getColumnInfo().getResultSetColumnIndex();
        if (isValueAdapter) {
            ValueAdapter valueAdapter = (ValueAdapter) userValue;
            ConnectionHandler connectionHandler = getConnectionHandler();
            Connection connection = connectionHandler.getStandaloneConnection();
            valueAdapter.write(connection, resultSet, columnIndex, valueAdapter.read());
        } else {
            DBDataType dataType = getColumnInfo().getDataType();
            dataType.setValueToResultSet(resultSet, columnIndex, userValue);
        }
    }

    protected DBDataset getDataset() {
        return getRow().getModel().getDataset();
    }

    private boolean userValueChanged(Object newUserValue) {
        if (userValue instanceof ValueAdapter) {
            ValueAdapter valueAdapter = (ValueAdapter) userValue;
            try {
                return !CommonUtil.safeEqual(valueAdapter.read(), newUserValue);
            } catch (SQLException e) {
                return true;
            }
        }

        if (userValue != null && newUserValue != null) {
            if (userValue.equals(newUserValue)) {
                return false;
            }
            // user input may not contain the entire precision (e.g. date time format)
            String formattedValue1 = Formatter.getInstance(getProject()).formatObject(userValue);
            String formattedValue2 = Formatter.getInstance(getProject()).formatObject(newUserValue);
            return !formattedValue1.equals(formattedValue2);
        }
        
        return !CommonUtil.safeEqual(userValue, newUserValue);
    }

    public void updateUserValue(Object userValue, String errorMessage) {
        if (!CommonUtil.safeEqual(userValue, getUserValue()) || hasError()) {
            DatasetEditorModelRow row = getRow();
            DatasetEditorError error = new DatasetEditorError(errorMessage, getColumnInfo().getColumn());
            getRow().notifyError(error, true, true);
            setUserValue(userValue);
            ConnectionHandler connectionHandler = getConnectionHandler();
            if (!row.isInsert() && !connectionHandler.isAutoCommit()) {
                isModified = true;
                row.setModified(true);
                connectionHandler.ping(false);
            }
        }
    }

    public boolean matches(DatasetEditorModelCell remoteCell, boolean lenient) {
        if (CommonUtil.safeEqual(getUserValue(), remoteCell.getUserValue())){
            return true;
        }
        if (lenient && (getRow().isNew() || getRow().isModified()) && getUserValue() == null && remoteCell.getUserValue() != null) {
            return true;
        }
        return false;
    }

    public ConnectionHandler getConnectionHandler() {
        return getRow().getModel().getConnectionHandler();
    }

    private DatasetEditorTable getEditorTable() {
        return getRow().getModel().getEditorTable();
    }

    public void edit() {
        int index = getEditorTable().convertColumnIndexToView(getIndex());
        if (index > 0) {
            DatasetEditorTable table = getEditorTable();
            table.editCellAt(getRow().getIndex(), index);
        }
    }

    public void editPrevious() {
        int index = getEditorTable().convertColumnIndexToView(getIndex());
        if (index > 0) {
            DatasetEditorTable table = getEditorTable();
            table.editCellAt(getRow().getIndex(), index -1);
        }
    }

    public void editNext(){
        int index = getEditorTable().convertColumnIndexToView(getIndex());
        if (index < getRow().getCells().size()-1) {
            DatasetEditorTable table = getEditorTable();
            table.editCellAt(getRow().getIndex(), index + 1);
        }
    }

    public DatasetEditorModelRow getRow() {
        return (DatasetEditorModelRow) super.getRow();
    }

    public void setOriginalUserValue(Object value) {
        if (originalUserValue == null) {
            isModified = value != null;
        } else {
            isModified = !originalUserValue.equals(value);
        }
        this.originalUserValue = value;
    }

    public Object getOriginalUserValue() {
        return originalUserValue;
    }

    public boolean isModified() {
        return isModified;
    }

    public boolean isEditing() {
        DatasetEditorTable table = getEditorTable();
        return table.isEditing() &&
               table.isCellSelected(getRow().getIndex(), getIndex());
    }

    public boolean isNavigable() {
        return getColumnInfo().getColumn().isForeignKey() && getUserValue() != null;
    }

    public void notifyCellUpdated() {
        getRow().getModel().notifyCellUpdated(getRow().getIndex(), getIndex());
    }

    public void scrollToVisible() {
        DatasetEditorTable table = getEditorTable();
        table.scrollRectToVisible(table.getCellRect(getRow().getIndex(), getIndex(), true));
    }

    /*********************************************************
     *                    ChangeListener                     *
     *********************************************************/
    public void stateChanged(ChangeEvent e) {
        notifyCellUpdated();
    }


    /*********************************************************
     *                        ERROR                          *
     *********************************************************/
    public boolean hasError() {
        if (error != null && error.isDirty()) {
            error = null;
        }
        return error != null;
    }

    public boolean notifyError(DatasetEditorError error, final boolean showPopup) {
        error.setNotified(true);
        if(!CommonUtil.safeEqual(this.error, error)) {
            clearError();
            this.error = error;
            notifyCellUpdated();
            if (showPopup) scrollToVisible();
            if (isEditing()) {
                DatasetEditorTable table = getEditorTable();
                TableCellEditor tableCellEditor = table.getCellEditor();
                if (tableCellEditor instanceof DatasetTableCellEditor) {
                    DatasetTableCellEditor cellEditor = (DatasetTableCellEditor) tableCellEditor;
                    cellEditor.highlight(DatasetTableCellEditor.HIGHLIGHT_TYPE_ERROR);
                }
            }
            error.addChangeListener(this);
            if (showPopup) showErrorPopup();
            return true;
        }
        return false;
    }

    public void showErrorPopup() {
        new SimpleLaterInvocator() {
            public void execute() {
                if (!isDisposed()) {
                    DatasetEditorModelRow row = getRow();
                    if (row != null) {
                        DatasetEditorModel model = row.getModel();
                        if (model != null) {
                            DatasetEditorTable editorTable = model.getEditorTable();
                            if (editorTable != null) {
                                if (!editorTable.isShowing()) {
                                    DBDataset dataset = getDataset();
                                    DatabaseFileSystem.getInstance().openEditor(dataset, EditorProviderId.DATA, true);
                                }
                                if (error != null) {
                                    DatasetEditorErrorForm errorForm = new DatasetEditorErrorForm(DatasetEditorModelCell.this);
                                    errorForm.show();
                                }
                            }
                        }
                    }
                }
            }
        }.start();
    }

    public void clearError() {
        if (error != null ) {
            error.markDirty();
            error = null;
        }
    }

    public DatasetEditorError getError() {
        return error;
    }

    @Override
    public void dispose() {
        super.dispose();
        originalUserValue = null;
        error = null;
    }

    public void revertChanges() {
        if (isModified) {
            updateUserValue(originalUserValue, false);
            this.isModified = false;
        }
    }
}
