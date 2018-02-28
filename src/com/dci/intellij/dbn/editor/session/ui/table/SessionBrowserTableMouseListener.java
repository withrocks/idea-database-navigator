package com.dci.intellij.dbn.editor.session.ui.table;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import com.dci.intellij.dbn.editor.session.model.SessionBrowserColumnInfo;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserModelCell;
import com.intellij.openapi.Disposable;

public class SessionBrowserTableMouseListener extends MouseAdapter implements Disposable {
    private SessionBrowserTable table;

    public SessionBrowserTableMouseListener(SessionBrowserTable table) {
        this.table = table;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    public void mouseReleased(final MouseEvent event) {
        if (event.getButton() == MouseEvent.BUTTON3) {
            Point mousePoint = event.getPoint();
            SessionBrowserModelCell cell = (SessionBrowserModelCell) table.getCellAtLocation(mousePoint);
            if (cell != null) {
                int rowIndex = table.rowAtPoint(mousePoint);
                int columnIndex = table.columnAtPoint(mousePoint);
                int[] selectedRows = table.getSelectedRows();
                int[] selectedColumns = table.getSelectedColumns();

                boolean selectCell = true;
                for (int selectedRow : selectedRows) {
                    if (selectedRow == rowIndex) {
                        for (int selectedColumn : selectedColumns) {
                            if (selectedColumn == columnIndex ) {
                                selectCell = false;
                                break;
                            }
                        }
                        break;
                    }
                }


                if (selectCell) {
                    table.selectCell(rowIndex, columnIndex);
                }
                SessionBrowserColumnInfo columnInfo = (SessionBrowserColumnInfo) table.getModel().getColumnInfo(columnIndex);
                table.showPopupMenu(event, cell, columnInfo);
            }
        }
    }

    @Override
    public void dispose() {
        table = null;
    }
}