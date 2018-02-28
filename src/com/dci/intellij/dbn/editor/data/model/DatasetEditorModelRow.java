package com.dci.intellij.dbn.editor.data.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.data.model.ColumnInfo;
import com.dci.intellij.dbn.data.model.DataModelCell;
import com.dci.intellij.dbn.data.model.DataModelRow;
import com.dci.intellij.dbn.data.model.resultSet.ResultSetDataModelRow;
import com.dci.intellij.dbn.editor.data.DatasetEditorError;
import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBConstraint;
import com.dci.intellij.dbn.object.DBTable;
import com.dci.intellij.dbn.object.common.DBObject;

public class DatasetEditorModelRow extends ResultSetDataModelRow<DatasetEditorModelCell> {
    private int resultSetRowIndex;
    private boolean isNew;
    private boolean isInsert;
    private boolean isDeleted;
    private boolean isModified;


    public DatasetEditorModelRow(DatasetEditorModel model, ResultSet resultSet, int resultSetRowIndex) throws SQLException {
        super(model, resultSet);
        this.resultSetRowIndex = resultSetRowIndex;
    }

    @Override
    public DatasetEditorModel getModel() {
        return (DatasetEditorModel) super.getModel();
    }

    public DatasetEditorModelCell getCellForColumn(DBColumn column) {
        int columnIndex = getModel().getHeader().indexOfColumn(column);
        return getCellAtIndex(columnIndex);
    }

    @Override
    protected DatasetEditorModelCell createCell(ResultSet resultSet, ColumnInfo columnInfo) throws SQLException {
        return new DatasetEditorModelCell(this, resultSet, (DatasetEditorColumnInfo) columnInfo);
    }

    public void updateStatusFromRow(DatasetEditorModelRow oldRow) {
        if (oldRow != null) {
            isNew = oldRow.isNew;
            isDeleted = oldRow.isDeleted;
            isModified = oldRow.isModified;
            setIndex(oldRow.getIndex());
            if (oldRow.isModified) {
                for (int i=1; i<getCells().size(); i++) {
                    DatasetEditorModelCell oldCell = oldRow.getCellAtIndex(i);
                    DatasetEditorModelCell newCell = getCellAtIndex(i);
                    newCell.setOriginalUserValue(oldCell.getOriginalUserValue());
                }
            }
        }
    }

    public void updateDataFromRow(DataModelRow oldRow) throws SQLException {
        for (int i=0; i<getCells().size(); i++) {
            DataModelCell oldCell = oldRow.getCellAtIndex(i);
            DatasetEditorModelCell newCell = getCellAtIndex(i);
            newCell.updateUserValue(oldCell.getUserValue(), false);
        }
    }

    public void setUserValuesToResultSet(ResultSet resultSet) throws SQLException {
        for (int i=0; i<getCells().size(); i++) {
            DatasetEditorModelCell newCell = getCellAtIndex(i);
            newCell.setUserValueToResultSet(resultSet);
        }
    }

    public void delete() {
        try {
            ResultSet resultSet = scrollResultSet();
            resultSet.deleteRow();
            isDeleted = true;
            isModified = false;
            isNew = false;
        } catch (SQLException e) {
            MessageUtil.showErrorDialog(getProject(), "Could not delete row at index " + getIndex() + '.', e);
        }
    }

    public boolean matches(DataModelRow row, boolean lenient) {
        // try fast match by primary key
        if (getModel().getDataset() instanceof DBTable) {
            DBTable table = (DBTable) getModel().getDataset();
            List<DBColumn> uniqueColumns = table.getPrimaryKeyColumns();
            if (uniqueColumns.size() == 0) {
                uniqueColumns = table.getUniqueKeyColumns();
            }
            if (uniqueColumns.size() > 0) {
                for (DBColumn uniqueColumn : uniqueColumns) {
                    int index = getModel().getHeader().indexOfColumn(uniqueColumn);
                    DatasetEditorModelCell localCell = getCellAtIndex(index);
                    DatasetEditorModelCell remoteCell = (DatasetEditorModelCell) row.getCellAtIndex(index);
                    if (!localCell.matches(remoteCell, false)) return false;
                }
                return true;
            }
        }

        // try to match all columns
        for (int i=0; i<getCells().size(); i++) {
            DatasetEditorModelCell localCell = getCellAtIndex(i);
            DatasetEditorModelCell remoteCell = (DatasetEditorModelCell) row.getCellAtIndex(i);

            //local cell is usually the cell on client side.
            // remote cell may have been changed by a trigger on update/insert
            /*if (!localCell.equals(remoteCell) && (localCell.getUserValue()!= null || !ignoreNulls)) {
                return false;
            }*/
            if (!localCell.matches(remoteCell, lenient)) {
                return false;
            }
        }
        return true;
    }

    public void notifyError(DatasetEditorError error, boolean startEditing, boolean showPopup) {
        DBObject messageObject = error.getMessageObject();
        if (messageObject != null) {
            if (messageObject instanceof DBColumn) {
                DBColumn column = (DBColumn) messageObject;
                DatasetEditorModelCell cell = getCellForColumn(column);
                boolean isErrorNew = cell.notifyError(error, true);
                if (isErrorNew && startEditing) cell.edit();
            } else if (messageObject instanceof DBConstraint) {
                DBConstraint constraint = (DBConstraint) messageObject;
                DatasetEditorModelCell firstCell = null;
                boolean isErrorNew = false;
                for (DBColumn column : constraint.getColumns()) {
                    DatasetEditorModelCell cell = getCellForColumn(column);
                    isErrorNew = cell.notifyError(error, false);
                    if (firstCell == null) firstCell = cell;
                }
                if (isErrorNew && showPopup) {
                    firstCell.showErrorPopup();
                    error.setNotified(true);
                }
            }
        }
    }

    public void revertChanges() {
        if (isModified) {
            for (DatasetEditorModelCell cell : getCells()) {
                cell.revertChanges();
            }
        }
    }


    public int getResultSetRowIndex() {
        return isDeleted ? -1 : resultSetRowIndex;
    }

    public void shiftResultSetRowIndex(int delta) {
        assert !isDeleted;
        resultSetRowIndex = resultSetRowIndex + delta;
    }

    public ResultSet scrollResultSet() throws SQLException {
        ResultSet resultSet = getResultSet();
        resultSet.absolute(resultSetRowIndex);
        return resultSet;
    }

    public ResultSet getResultSet() {
        return getModel().getResultSet();
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setInsert(boolean insert) {
        isInsert = insert;
    }

    public boolean isInsert() {
        return isInsert;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setModified(boolean modified) {
        this.isModified = modified;
        if (modified) getModel().setModified(true);
    }

    public boolean isModified() {
        return isModified;
    }
}
