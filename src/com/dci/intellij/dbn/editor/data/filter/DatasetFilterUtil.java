package com.dci.intellij.dbn.editor.data.filter;

import java.util.List;

import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.grid.options.DataGridSettings;
import com.dci.intellij.dbn.data.sorting.SortDirection;
import com.dci.intellij.dbn.data.sorting.SortingInstruction;
import com.dci.intellij.dbn.data.sorting.SortingState;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBDataset;
import com.dci.intellij.dbn.object.DBTable;

public class DatasetFilterUtil {

    public static void addOrderByClause(DBDataset dataset, StringBuilder buffer, SortingState sortingState) {
        DataGridSettings dataGridSettings = DataGridSettings.getInstance(dataset.getProject());
        boolean nullsFirst = dataGridSettings.getSortingSettings().isNullsFirst();
        List<SortingInstruction> sortingInstructions = sortingState.getSortingInstructions();
        if (sortingInstructions.size() > 0) {
            buffer.append(" order by ");
            boolean instructionAdded = false;
            for (SortingInstruction sortingInstruction : sortingInstructions) {
                SortDirection sortDirection = sortingInstruction.getDirection();
                DBColumn column = dataset.getColumn(sortingInstruction.getColumnName());
                if (column != null && !column.isDisposed() && !sortDirection.isIndefinite()) {
                    ConnectionHandler connectionHandler = FailsafeUtil.get(column.getConnectionHandler());
                    DatabaseCompatibilityInterface compatibilityInterface = connectionHandler.getInterfaceProvider().getCompatibilityInterface();
                    String orderByClause = compatibilityInterface.getOrderByClause(column.getName(), sortDirection, nullsFirst);
                    buffer.append(instructionAdded ? ", " : "");
                    buffer.append(orderByClause);
                    instructionAdded = true;
                }
            }
        }
    }

    public static void addForUpdateClause(DBDataset dataset, StringBuilder buffer) {
        if (dataset instanceof DBTable && dataset.hasLobColumns()) {
            buffer.append(" for update");
        }
    }

    public static void createSelectStatement(DBDataset dataset, StringBuilder buffer) {
        buffer.append("select ");
        int index = 0;
        for (DBColumn column : dataset.getColumns()) {
            if (index > 0) {
                buffer.append(", ");
            }
            buffer.append(column.getQuotedName(false));
            index++;
        }
        buffer.append(" from ");
        buffer.append(dataset.getSchema().getQuotedName(false));
        buffer.append(".");
        buffer.append(dataset.getQuotedName(false));

    }

    public static void createSimpleSelectStatement(DBDataset dataset, StringBuilder buffer) {
        buffer.append("select a.* from ");
        buffer.append(dataset.getSchema().getQuotedName(false));
        buffer.append(".");
        buffer.append(dataset.getQuotedName(false));
        buffer.append(" a");

    }
}
