package com.dci.intellij.dbn.editor.data.state.sorting.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.ValueSelector;
import com.dci.intellij.dbn.common.ui.ValueSelectorListener;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.data.sorting.SortingInstruction;
import com.dci.intellij.dbn.editor.data.state.sorting.action.ChangeSortingDirectionAction;
import com.dci.intellij.dbn.editor.data.state.sorting.action.DeleteSortingCriteriaAction;
import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBDataset;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.util.ui.UIUtil;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DatasetSortingColumnForm extends DBNFormImpl<DatasetEditorSortingForm> {
    private JPanel actionsPanel;
    private JPanel mainPanel;
    private JPanel columnPanel;
    private JLabel indexLabel;
    private JLabel dataTypeLabel;

    private SortingInstruction sortingInstruction;

    public DatasetSortingColumnForm(final DatasetEditorSortingForm parent, SortingInstruction sortingInstruction) {
        super(parent);
        this.sortingInstruction = sortingInstruction;

        DBColumn column = parent.getDataset().getColumn(sortingInstruction.getColumnName());
        ColumnSelector columnSelector = new ColumnSelector(column);
        columnPanel.add(columnSelector, BorderLayout.CENTER);
        dataTypeLabel.setText(column.getDataType().getQualifiedName());
        dataTypeLabel.setForeground(UIUtil.getInactiveTextColor());

        ActionToolbar actionToolbar = ActionUtil.createActionToolbar(
                "DBNavigator.DataEditor.Sorting.Instruction", true,
                new ChangeSortingDirectionAction(this),
                new DeleteSortingCriteriaAction(this));
        actionsPanel.add(actionToolbar.getComponent(), BorderLayout.CENTER);
    }

    private class ColumnSelector extends ValueSelector<DBColumn>{
        public ColumnSelector(DBColumn selectedColumn) {
            super(Icons.DBO_COLUMN_HIDDEN, "Select column...", selectedColumn, true);
            addListener(new ValueSelectorListener<DBColumn>() {
                @Override
                public void valueSelected(DBColumn column) {
                    sortingInstruction.setColumnName(column.getName());
                    dataTypeLabel.setText(column.getDataType().getQualifiedName());
                }
            });
        }

        @Override
        public List<DBColumn> loadValues() {
            DBDataset dataset = getParentComponent().getDataset();
            List<DBColumn> columns = new ArrayList<DBColumn>(dataset.getColumns());
            Collections.sort(columns);
            return columns;
        }

        @Override
        public boolean isVisible(DBColumn value) {
            List<DatasetSortingColumnForm> sortingInstructionForms = getParentComponent().getSortingInstructionForms();
            for (DatasetSortingColumnForm sortingColumnForm : sortingInstructionForms) {
                if (sortingColumnForm.getSortingInstruction().getColumnName().equalsIgnoreCase(value.getName())) {
                    return false;
                }
            }
            return true;

        }
    }

    public void setIndex(int index) {
        sortingInstruction.setIndex(index);
        indexLabel.setText(Integer.toString(index));
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    public SortingInstruction getSortingInstruction() {
        return sortingInstruction;
    }

    public void remove() {
        getParentComponent().removeSortingColumn(this);
    }

    public DBDataset getDataset() {
        return getParentComponent().getDataset();
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
