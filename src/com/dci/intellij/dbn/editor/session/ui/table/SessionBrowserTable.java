package com.dci.intellij.dbn.editor.session.ui.table;

import javax.swing.JPopupMenu;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.table.TableCellRenderer;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.EventObject;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.thread.ConditionalLaterInvocator;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.data.grid.ui.table.basic.BasicTableCellRenderer;
import com.dci.intellij.dbn.data.grid.ui.table.basic.BasicTableGutter;
import com.dci.intellij.dbn.data.grid.ui.table.basic.BasicTableSelectionRestorer;
import com.dci.intellij.dbn.data.grid.ui.table.resultSet.ResultSetTable;
import com.dci.intellij.dbn.data.grid.ui.table.sortable.SortableTableHeaderRenderer;
import com.dci.intellij.dbn.data.preview.LargeValuePreviewPopup;
import com.dci.intellij.dbn.data.record.RecordViewInfo;
import com.dci.intellij.dbn.editor.session.SessionBrowser;
import com.dci.intellij.dbn.editor.session.action.SessionBrowserTableActionGroup;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserColumnInfo;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserModel;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserModelCell;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserModelRow;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPopupMenu;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import com.intellij.ui.PopupMenuListenerAdapter;

public class SessionBrowserTable extends ResultSetTable<SessionBrowserModel> {
    private SessionBrowser sessionBrowser;

    public SessionBrowserTable(SessionBrowser sessionBrowser) throws SQLException {
        super(new SessionBrowserModel(sessionBrowser.getConnectionHandler()), false, new RecordViewInfo(sessionBrowser.getConnectionHandler().getName(), null));
        getTableHeader().setDefaultRenderer(new SortableTableHeaderRenderer());
        getTableHeader().addMouseListener(new SessionBrowserTableHeaderMouseListener(this));
        addMouseListener(new SessionBrowserTableMouseListener(this));
        getSelectionModel().addListSelectionListener(listSelectionListener);
        this.sessionBrowser = sessionBrowser;


        DataProvider dataProvider = sessionBrowser.getDataProvider();
        ActionUtil.registerDataProvider(this, dataProvider, false);
        ActionUtil.registerDataProvider(getTableHeader(), dataProvider, false);
    }

    @Override
    protected BasicTableCellRenderer createCellRenderer() {
        return new SessionBrowserTableCellRenderer();
    }

    @NotNull
    @Override
    public BasicTableSelectionRestorer createSelectionRestorer() {
        return new SelectionRestorer();
    }

    public Project getProject() {
        return sessionBrowser.getProject();
    }


    public String getName() {
        return sessionBrowser == null ? "Disposed" : sessionBrowser.getConnectionHandler().getName();
    }

    @Override
    protected BasicTableGutter createTableGutter() {
        return new SessionBrowserTableGutter(this);
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        return getCellRenderer();
    }

    public void clearSelection() {
        new ConditionalLaterInvocator() {
            public void execute() {
                SessionBrowserTable.super.clearSelection();
            }
        }.start();
    }

    @Override
    public void removeEditor() {
        new ConditionalLaterInvocator() {
            @Override
            public void execute() {
                SessionBrowserTable.super.removeEditor();
            }
        }.start();
    }

    public void updateTableGutter() {
        new ConditionalLaterInvocator() {
            @Override
            public void execute() {
                getTableGutter().revalidate();
                getTableGutter().repaint();
            }
        }.start();
    }

    @Override
    public boolean editCellAt(final int row, final int column, final EventObject e) {
        return super.editCellAt(row, column, e);
    }

    @Override
    protected void initLargeValuePopup(LargeValuePreviewPopup viewer) {
        super.initLargeValuePopup(viewer);
    }

    @Override
    public int getColumnWidthSpan() {
        return 10;
    }

    public SessionBrowser getSessionBrowser() {
        return sessionBrowser;
    }

    private final ListSelectionListener listSelectionListener = new ListSelectionListener() {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                snapshotSelection();
                if (sessionBrowser != null) {
                    sessionBrowser.updateDetails();
                }
            }
        }
    };

    @Override
    protected boolean showRecordViewDataTypes() {
        return false;
    }

    private class SelectionRestorer extends BasicTableSelectionRestorer{
        private Object sessionId;
        private int columnIndex;

        public void snapshot() {
            if (!isRestoring()) {
                int selectedRowCount = getSelectedRowCount();
                int selectedColumnCount = getSelectedColumnCount();
                if (selectedRowCount == 1 && selectedColumnCount == 1) {
                    sessionId = getModel().getRowAtIndex(getSelectedRow()).getSessionId();
                    columnIndex = getSelectedColumn();
                } else if (selectedRowCount > 0 && selectedColumnCount > 0) {
                    sessionId = null;
                    columnIndex = -1;
                }
            }
        }

        public void restore() {
            try {
                setRestoring(true);
                if (sessionId != null) {
                    int rowIndex = 0;
                    for (SessionBrowserModelRow row : getModel().getRows()) {
                        if (sessionId.equals(row.getSessionId())) {
                            selectCell(rowIndex, columnIndex);
                            break;
                        }
                        rowIndex++;
                    }
                }
            } finally {
                setRestoring(false);
            }
        }
    };


    /********************************************************
     *                        Popup                         *
     ********************************************************/
    public void showPopupMenu(
            final MouseEvent event,
            final SessionBrowserModelCell cell,
            final SessionBrowserColumnInfo columnInfo) {
        ActionGroup actionGroup = new SessionBrowserTableActionGroup(sessionBrowser, cell, columnInfo);
        ActionPopupMenu actionPopupMenu = ActionManager.getInstance().createActionPopupMenu("", actionGroup);
        JPopupMenu popupMenu = actionPopupMenu.getComponent();
        popupMenu.addPopupMenuListener(new PopupMenuListenerAdapter() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                sessionBrowser.setPreventLoading(true);
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                sessionBrowser.setPreventLoading(false);
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                sessionBrowser.setPreventLoading(false);
            }
        });
        popupMenu.show((Component) event.getSource(), event.getX(), event.getY());
    }


    /********************************************************
     *                     Disposable                       *
     ********************************************************/

    @Override
    public void dispose() {
        super.dispose();
        sessionBrowser = null;
    }
}
