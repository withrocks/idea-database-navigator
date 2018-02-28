package com.dci.intellij.dbn.execution.method.result.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.table.DBNTable;
import com.dci.intellij.dbn.execution.method.ArgumentValue;
import com.intellij.ui.RowIcon;
import com.intellij.util.ui.UIUtil;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Component;

@Deprecated
public class ArgumentValuesTable extends DBNTable {
    private static final Border EMPTY_BORDER = new EmptyBorder(0, 2, 0, 2);

    public ArgumentValuesTable(ArgumentValuesTableModel model) {
        super(model.getProject(), model, true);
        setDefaultRenderer(String.class, new CellRenderer());
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setCellSelectionEnabled(true);
        setRowHeight(getRowHeight() + 2);
        accommodateColumnsSize();
    }


    private class CellRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (column == 0) {
                ArgumentValue argumentValue = (ArgumentValue) value;
                setBorder(EMPTY_BORDER);
                setBackground(UIUtil.getPanelBackgound());
                setForeground(UIUtil.getLabelForeground());

                if (argumentValue.getAttribute() != null) {
                    RowIcon icon = new RowIcon(2);
                    icon.setIcon(Icons.SMALL_TREE_BRANCH, 0);
                    icon.setIcon(argumentValue.getAttribute().getIcon(), 1);
                    setIcon(icon);
                } else {
                    setIcon(argumentValue.getArgument().getIcon());
                }
                setText(argumentValue.getName());
            } else {
                setBorder(EMPTY_BORDER);
                if (!isSelected) {
                    setBackground(UIUtil.getTableBackground());
                    setForeground(UIUtil.getLabelForeground());
                }
                setIcon(null);
            }
            return component;
        }
    }
}
