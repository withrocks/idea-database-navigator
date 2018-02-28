package com.dci.intellij.dbn.editor.session.ui.table;

import javax.swing.JTable;
import javax.swing.border.Border;
import java.awt.Color;

import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.data.grid.color.DataGridTextAttributes;
import com.dci.intellij.dbn.data.grid.ui.table.basic.BasicTableCellRenderer;
import com.dci.intellij.dbn.editor.session.color.SessionBrowserTextAttributes;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserModelCell;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserModelRow;
import com.intellij.ui.SimpleTextAttributes;

public class SessionBrowserTableCellRenderer extends BasicTableCellRenderer {

    @Override
    protected DataGridTextAttributes createTextAttributes() {
        return new SessionBrowserTextAttributes();
    }

    @Override
    public SessionBrowserTextAttributes getAttributes() {
        return (SessionBrowserTextAttributes) super.getAttributes();
    }

    protected void customizeCellRenderer(JTable table, Object value, boolean isSelected, boolean hasFocus, int rowIndex, int columnIndex) {
        acquireState(table, isSelected, false, rowIndex, columnIndex);
        SessionBrowserModelCell cell = (SessionBrowserModelCell) value;
        SessionBrowserTable sessionBrowserTable = (SessionBrowserTable) table;

        if (cell != null && !cell.isDisposed() && !sessionBrowserTable.isDisposed() && !sessionBrowserTable.getProject().isDisposed()) {
            SessionBrowserModelRow row = cell.getRow();
            boolean isLoading = sessionBrowserTable.isLoading();

            boolean isCaretRow = table.getCellSelectionEnabled() && table.getSelectedRow() == rowIndex && table.getSelectedRowCount() == 1;
            boolean isConnected = FailsafeUtil.get(sessionBrowserTable.getSessionBrowser().getConnectionHandler()).isConnected();

            SessionBrowserTextAttributes attributes = getAttributes();
            SimpleTextAttributes textAttributes = attributes.getActiveSession(isCaretRow);

            if (isSelected) {
                textAttributes = attributes.getSelection();
            } else {
                if (isLoading || !isConnected) {
                    textAttributes = attributes.getLoadingData(isCaretRow);
                } else {
                    switch (row.getSessionStatus()) {
                        case ACTIVE: textAttributes = attributes.getActiveSession(isCaretRow); break;
                        case INACTIVE: textAttributes = attributes.getInactiveSession(isCaretRow); break;
                        case CACHED: textAttributes = attributes.getCachedSession(isCaretRow); break;
                        case SNIPED: textAttributes = attributes.getSnipedSession(isCaretRow); break;
                        case KILLED: textAttributes = attributes.getKilledSession(isCaretRow); break;
                    }
                }
            }

            Color background = CommonUtil.nvl(textAttributes.getBgColor(), table.getBackground());
            Color foreground = CommonUtil.nvl(textAttributes.getFgColor(), table.getForeground());


            Border border = getLineBorder(background);

            setBorder(border);
            setBackground(background);
            setForeground(foreground);
            writeUserValue(cell, textAttributes, attributes);
        }
    }

    @Override
    public void setForeground(Color fg) {
        super.setForeground(fg);
    }
}
                                                                