package com.dci.intellij.dbn.editor.session.details;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.Component;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.table.DBNTable;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.intellij.openapi.project.Project;

public class SessionDetailsTable extends DBNTable {

    public SessionDetailsTable(Project project) {
        super(project, new SessionDetailsTableModel(), false);
        setDefaultRenderer(Object.class, cellRenderer);
    }

    private final TableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String text = CommonUtil.nvl(value, "").toString();
            setText(text);
            if (column == 1 && StringUtil.isNotEmpty(text)) {
                switch (row) {
                    case 1: setIcon(Icons.SB_FILTER_USER); break;
                    case 2: setIcon(Icons.DBO_SCHEMA); break;
                    case 3: setIcon(Icons.SB_FILTER_SERVER); break;
                    default: setIcon(null);
                }
            } else{
                setIcon(null);
            }

            return component;
        }
    };
}
