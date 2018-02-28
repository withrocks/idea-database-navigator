package com.dci.intellij.dbn.common.ui.table;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.event.EventListenerList;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.font.LineMetrics;
import java.util.Timer;
import java.util.TimerTask;

import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.UIUtil;
import sun.swing.SwingUtilities2;

public class DBNTable<T extends DBNTableModel> extends JTable implements Disposable{
    private static final int MAX_COLUMN_WIDTH = 300;
    private static final int MIN_COLUMN_WIDTH = 10;
    public static final Color GRID_COLOR = new JBColor(new Color(0xE6E6E6), Color.DARK_GRAY);
    protected DBNTableGutter tableGutter;
    private Project project;
    private double scrollDistance;
    private JBScrollPane scrollPane;
    private Timer scrollTimer;


    public DBNTable(Project project, DBNTableModel tableModel, boolean showHeader) {
        super(tableModel);
        this.project = project;
        setGridColor(GRID_COLOR);
        Font font = getFont();//UIUtil.getListFont();
        setFont(font);
        setBackground(UIUtil.getTextFieldBackground());

        LineMetrics lineMetrics = font.getLineMetrics("ABC", SwingUtilities2.getFontRenderContext(this));
        int fontHeight = Math.round(lineMetrics.getHeight());
        setRowHeight(fontHeight + 2);

        final JTableHeader tableHeader = getTableHeader();
        if (!showHeader) {
            tableHeader.setVisible(false);
            tableHeader.setPreferredSize(new Dimension(-1, 0));
        } else {
            tableHeader.addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent e) {
                    scrollPane = UIUtil.getParentOfType(JBScrollPane.class, DBNTable.this);
                    if (scrollPane != null) {
                        calculateScrollDistance();
                        if (scrollDistance != 0 && scrollTimer == null) {
                            scrollTimer = new Timer();
                            scrollTimer.schedule(new ScrollTask(), 100, 100);
                        }
                    }
                }
            });

            tableHeader.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (scrollTimer != null) {
                        scrollTimer.cancel();
                        scrollTimer.purge();
                        scrollTimer = null;
                    }
                }
            });
        }

        Disposer.register(this, tableModel);
    }

    @Override
    public T getModel() {
        return (T) super.getModel();
    }

    private double calculateScrollDistance() {
        JViewport viewport = scrollPane.getViewport();
        double mouseLocation = MouseInfo.getPointerInfo().getLocation().getX();
        double viewportLocation = viewport.getLocationOnScreen().getX();

        Point viewPosition = viewport.getViewPosition();
        double contentLocation = viewport.getView().getLocationOnScreen().getX();

        if (contentLocation < viewportLocation && mouseLocation < viewportLocation + 20) {
            scrollDistance = - Math.min(viewPosition.x, (viewportLocation - mouseLocation));
        } else {
            int viewportWidth = viewport.getWidth();
            int contentWidth = viewport.getView().getWidth();

            if (contentLocation + contentWidth > viewportLocation + viewportWidth && mouseLocation > viewportLocation + viewportWidth - 20) {
                scrollDistance = (mouseLocation - viewportLocation - viewportWidth);
            } else {
                scrollDistance = 0;
            }
        }

        return scrollDistance;
    }



    public Project getProject() {
        return project;
    }

    public Object getValueAtMouseLocation() {
        Point location = MouseInfo.getPointerInfo().getLocation();
        location.setLocation(location.getX() - getLocationOnScreen().getX(), location.getY() - getLocationOnScreen().getY());
        return getValueAtLocation(location);
    }

    public Object getValueAtLocation(Point point) {
        int columnIndex = columnAtPoint(point);
        int rowIndex = rowAtPoint(point);
        return columnIndex > -1 && rowIndex > -1 ? getModel().getValueAt(rowIndex, columnIndex) : null;
    }

    /*********************************************************
     *                    Cell metrics                       *
     *********************************************************/
    public void accommodateColumnsSize() {
        for (int columnIndex = 0; columnIndex < getColumnCount(); columnIndex++){
            accommodateColumnSize(columnIndex, getColumnWidthSpan());
        }
    }

    public int getColumnWidthSpan() {
        return 22;
    }

    @Override
    public int convertColumnIndexToView(int modelColumnIndex) {
        return super.convertColumnIndexToView(modelColumnIndex);
    }

    @Override
    public int convertColumnIndexToModel(int viewColumnIndex) {
        // table is not scrolling correctly when columns are moved
/*
        if (getTableHeader().getDraggedColumn() != null && CommonUtil.isCalledThrough(BasicTableHeaderUI.MouseInputHandler.class)) {
            return getTableHeader().getDraggedColumn().getModelIndex();
        }
*/
        return super.convertColumnIndexToModel(viewColumnIndex);
    }

    public void accommodateColumnSize(int colIndex, int span) {
        TableColumn column = getColumnModel().getColumn(colIndex);
        int columnIndex = column.getModelIndex();
        int preferredWidth = 0;

        // header
        JTableHeader tableHeader = getTableHeader();
        if (tableHeader != null) {
            Object headerValue = column.getHeaderValue();
            TableCellRenderer headerCellRenderer = column.getHeaderRenderer();
            if (headerCellRenderer == null) headerCellRenderer = tableHeader.getDefaultRenderer();
            Component headerComponent = headerCellRenderer.getTableCellRendererComponent(this, headerValue, false, false, 0, columnIndex);
            if (headerComponent.getPreferredSize().width > preferredWidth)
                preferredWidth = headerComponent.getPreferredSize().width;
        }

        // rows
        for (int rowIndex =0; rowIndex < getModel().getRowCount(); rowIndex++) {
            if (preferredWidth > MAX_COLUMN_WIDTH) {
                break;
            }
            Object value = getModel().getValueAt(rowIndex, columnIndex);
            TableCellRenderer renderer = getCellRenderer(rowIndex, columnIndex);

            if (renderer != null) {
                Component component = renderer.getTableCellRendererComponent(this, value, false, false, rowIndex, columnIndex);
                if (component.getPreferredSize().width > preferredWidth) {
                    preferredWidth = component.getPreferredSize().width;
                }
            }
        }

        if (preferredWidth > MAX_COLUMN_WIDTH) {
            preferredWidth = MAX_COLUMN_WIDTH;
        }

        if (preferredWidth < MIN_COLUMN_WIDTH) {
            preferredWidth = MIN_COLUMN_WIDTH;
        }

        preferredWidth = preferredWidth + span;

        if (column.getPreferredWidth() != preferredWidth)  {
            column.setPreferredWidth(preferredWidth);
        }

    }

    public void selectCell(int rowIndex, int columnIndex) {
        Rectangle cellRect = getCellRect(rowIndex, columnIndex, true);
        if (!getVisibleRect().contains(cellRect)) {
            scrollRectToVisible(cellRect);
        }
        if (getSelectedRowCount() != 1 || getSelectedRow() != rowIndex) {
            setRowSelectionInterval(rowIndex, rowIndex);
        }

        if (getSelectedColumnCount() != 1 || getSelectedColumn() != columnIndex) {
            setColumnSelectionInterval(columnIndex, columnIndex);
        }
    }

    private class ScrollTask extends TimerTask {
        public void run() {
            if (scrollPane != null && scrollDistance != 0) {
                new SimpleLaterInvocator() {
                    @Override
                    public void execute() {
                        JViewport viewport = scrollPane.getViewport();
                        Point viewPosition = viewport.getViewPosition();
                        viewport.setViewPosition(new Point((int) (viewPosition.x + scrollDistance), viewPosition.y));
                        calculateScrollDistance();
                    }
                }.start();
            }
        }
    }

    protected DBNTableGutter createTableGutter() {
        return null; // do not create gutter by default
    }

    public final DBNTableGutter getTableGutter() {
        if (tableGutter == null) {
            tableGutter = createTableGutter();
            if (tableGutter != null) {
                Disposer.register(this, tableGutter);
            }
        }
        return tableGutter;
    }

    public final void initTableGutter() {
        DBNTableGutter tableGutter = getTableGutter();
        if (tableGutter != null){
            JScrollPane scrollPane = UIUtil.getParentOfType(JScrollPane.class, this);
            if (scrollPane != null) {
                scrollPane.setRowHeaderView(tableGutter);
            }
        }
    }

    public void stopCellEditing() {
        if (isEditing()) {
            getCellEditor().stopCellEditing();
        }
    }

    /********************************************************
     *                    Disposable                        *
     ********************************************************/
    private boolean disposed;

    @Override
    public void dispose() {
        if (!disposed) {
            disposed = true;
            project = null;
            GUIUtil.removeListeners(this);
            listenerList = new EventListenerList();
            columnModel = new DefaultTableColumnModel();
            selectionModel = new DefaultListSelectionModel();
            tableHeader = null;
        }
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }

}
