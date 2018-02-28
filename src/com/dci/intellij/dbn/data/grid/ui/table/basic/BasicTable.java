package com.dci.intellij.dbn.data.grid.ui.table.basic;

import com.dci.intellij.dbn.common.locale.options.RegionalSettings;
import com.dci.intellij.dbn.common.thread.ConditionalLaterInvocator;
import com.dci.intellij.dbn.common.ui.table.DBNTable;
import com.dci.intellij.dbn.common.ui.table.TableSelectionRestorer;
import com.dci.intellij.dbn.data.grid.color.DataGridTextAttributes;
import com.dci.intellij.dbn.data.grid.options.DataGridSettings;
import com.dci.intellij.dbn.data.model.DataModelCell;
import com.dci.intellij.dbn.data.model.DataModelRow;
import com.dci.intellij.dbn.data.model.basic.BasicDataModel;
import com.dci.intellij.dbn.data.preview.LargeValuePreviewPopup;
import com.dci.intellij.dbn.data.value.LargeObjectValue;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.colors.EditorColorsListener;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupAdapter;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.ui.components.JBViewport;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class BasicTable<T extends BasicDataModel> extends DBNTable<T> implements EditorColorsListener, Disposable {
    private BasicTableCellRenderer cellRenderer;
    private JBPopup valuePopup;
    private boolean isLoading;
    private RegionalSettings regionalSettings;
    private DataGridSettings dataGridSettings;
    private TableSelectionRestorer selectionRestorer = createSelectionRestorer();

    public BasicTable(Project project, T dataModel) {
        super(project, dataModel, true);
        regionalSettings = RegionalSettings.getInstance(project);
        dataGridSettings = DataGridSettings.getInstance(project);
        cellRenderer = createCellRenderer();
        DataGridTextAttributes displayAttributes = cellRenderer.getAttributes();
        setSelectionForeground(displayAttributes.getSelection().getFgColor());
        setSelectionBackground(displayAttributes.getSelection().getBgColor());
        EditorColorsManager.getInstance().addEditorColorsListener(this, this);
        Color bgColor = displayAttributes.getPlainData(false, false).getBgColor();
        setBackground(bgColor == null ? UIUtil.getTableBackground() : bgColor);
        addMouseListener(lobValueMouseListener);

        addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent e) {
                Object newProperty = e.getNewValue();
                if (newProperty instanceof Font) {
                    Font font = (Font) newProperty;
                    FontMetrics fontMetrics = getFontMetrics(font);
                    setRowHeight(fontMetrics.getHeight() + 2);
                    JTableHeader tableHeader = getTableHeader();
                    if (tableHeader != null) {
                        TableCellRenderer defaultRenderer = tableHeader.getDefaultRenderer();
                        if (defaultRenderer instanceof BasicTableHeaderRenderer) {
                            BasicTableHeaderRenderer renderer = (BasicTableHeaderRenderer) defaultRenderer;
                            renderer.setFont(font);
                        }
                    }
                    accommodateColumnsSize();
                }

            }
        });
    }



    @NotNull
    public BasicTableSelectionRestorer createSelectionRestorer() {
        return new BasicTableSelectionRestorer();
    }

    public boolean isRestoringSelection() {
        return selectionRestorer.isRestoring();
    }

    public void snapshotSelection() {
        selectionRestorer.snapshot();
    }

    public void restoreSelection() {
        selectionRestorer.restore();
    }

    protected BasicTableGutter createTableGutter() {
        return new BasicTableGutter(this);
    }

    public RegionalSettings getRegionalSettings() {
        return regionalSettings;
    }

    public DataGridSettings getDataGridSettings() {
        return dataGridSettings;
    }

    protected BasicTableCellRenderer createCellRenderer() {
        return new BasicTableCellRenderer();
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
        updateBackground(loading);
    }

    public void updateBackground(final boolean readonly) {
        final JBViewport viewport = UIUtil.getParentOfType(JBViewport.class, this);
        if (viewport != null) {
            new ConditionalLaterInvocator() {
                @Override
                public void execute() {
                    DataGridTextAttributes attributes = cellRenderer.getAttributes();
                    Color background = readonly ?
                            attributes.getLoadingData(false).getBgColor() :
                            attributes.getPlainData(false, false).getBgColor();
                    viewport.setBackground(background);

                    viewport.revalidate();
                    viewport.repaint();
                }
            }.start();
        }
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void selectRow(int index) {
        clearSelection();
        int lastColumnIndex = getModel().getColumnCount() - 1;
        setColumnSelectionInterval(0, lastColumnIndex);
        getSelectionModel().setSelectionInterval(index, index);
        scrollRectToVisible(getCellRect(index, 0, true));
    }

    public TableCellRenderer getCellRenderer(int i, int i1) {
        return cellRenderer;
    }

    public BasicTableCellRenderer getCellRenderer() {
        return cellRenderer;
    }

    public void tableChanged(TableModelEvent e) {
        super.tableChanged(e);
        if (e.getFirstRow() != e.getLastRow()) {
            accommodateColumnsSize();
        }

        if (tableGutter != null) {
            tableGutter.setFixedCellHeight(rowHeight);
            tableGutter.setFixedCellWidth(getModel().getSize() == 0 ? 10 : -1);
        }
    }

    @Override
    public void setModel(@NotNull TableModel dataModel) {
        super.setModel(dataModel);
    }

    public DataModelCell getCellAtLocation(Point point) {
        int columnIndex = columnAtPoint(point);
        int rowIndex = rowAtPoint(point);
        return columnIndex > -1 && rowIndex > -1 ? getCellAtPosition(rowIndex, columnIndex) : null;
    }

    public DataModelCell getCellAtMouseLocation() {
        Point location = MouseInfo.getPointerInfo().getLocation();
        location.setLocation(location.getX() - getLocationOnScreen().getX(), location.getY() - getLocationOnScreen().getY());
        return getCellAtLocation(location);
    }

    public int getModelColumnIndex(int columnIndex) {
        return getColumnModel().getColumn(columnIndex).getModelIndex();
    }

    public DataModelCell getCellAtPosition(int rowIndex, int columnIndex) {
        DataModelRow row = getModel().getRowAtIndex(rowIndex);
        int modelColumnIndex = getModelColumnIndex(columnIndex);
        return row.getCellAtIndex(modelColumnIndex);
    }
    /*********************************************************
     *                EditorColorsListener                  *
     *********************************************************/
    @Override
    public void globalSchemeChange(EditorColorsScheme scheme) {
        cellRenderer.getAttributes().load();

        revalidate();
        repaint();
    }

    /*********************************************************
     *                ListSelectionListener                  *
     *********************************************************/
    public void valueChanged(ListSelectionEvent e) {
        super.valueChanged(e);
        if (!e.getValueIsAdjusting()) {
            if (hasFocus()) getTableGutter().clearSelection();
            showCellValuePopup();
        }
    }

    public void columnSelectionChanged(ListSelectionEvent e) {
        JTableHeader tableHeader = getTableHeader();
        if (tableHeader != null && tableHeader.getDraggedColumn() == null) {
            super.columnSelectionChanged(e);
            if (!e.getValueIsAdjusting()) {
                showCellValuePopup();
            }
        }
    }

    private void showCellValuePopup() {
        if (valuePopup != null) {
            valuePopup.cancel();
            valuePopup = null;
        }
        if (isLargeValuePopupActive() && !isRestoringSelection()) {
            boolean isReadonly = getModel().isReadonly() || getModel().getState().isReadonly();
            if (isReadonly && getSelectedColumnCount() == 1 && getSelectedRowCount() == 1) {
                int rowIndex = getSelectedRow();
                int columnIndex = getSelectedColumn();
                if (!canDisplayCompleteValue(rowIndex, columnIndex)) {
                    Rectangle cellRect = getCellRect(rowIndex, columnIndex, true);
                    DataModelCell cell = (DataModelCell) getValueAt(rowIndex, columnIndex);
                    TableColumn column = getColumnModel().getColumn(columnIndex);

                    int preferredWidth = column.getWidth();
                    LargeValuePreviewPopup viewer = new LargeValuePreviewPopup(this, cell, preferredWidth);
                    initLargeValuePopup(viewer);
                    Point location = cellRect.getLocation();
                    location.setLocation(location.getX() + 4, location.getY() + 20);

                    valuePopup = viewer.show(this, location);
                    valuePopup.addListener(
                        new JBPopupAdapter() {
                            @Override
                            public void onClosed(LightweightWindowEvent event) {
                                valuePopup.cancel();
                                valuePopup = null;
                            }
                        }
                    );
                }
            }
        }
    }

    private MouseAdapter lobValueMouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1 && valuePopup == null) {
                showCellValuePopup();
            }
        }
    };


    protected void initLargeValuePopup(LargeValuePreviewPopup viewer) {
    }

    protected boolean isLargeValuePopupActive() {
        return true;
    }

    private boolean canDisplayCompleteValue(int rowIndex, int columnIndex) {
        DataModelCell cell = (DataModelCell) getValueAt(rowIndex, columnIndex);
        if (cell != null) {
            Object value = cell.getUserValue();
            if (value instanceof LargeObjectValue) {
                return false;
            }
            if (value != null) {
                TableCellRenderer renderer = getCellRenderer(rowIndex, columnIndex);
                Component component = renderer.getTableCellRendererComponent(this, cell, false, false, rowIndex, columnIndex);
                TableColumn column = getColumnModel().getColumn(columnIndex);
                return component.getPreferredSize().width <= column.getWidth();
            }
        }
        return true;
    }

    public void dispose() {
        super.dispose();
        regionalSettings = null;
        dataGridSettings = null;
        tableGutter = null;
        EditorColorsManager.getInstance().removeEditorColorsListener(this);
    }

    public Rectangle getCellRect(DataModelCell cell) {
        int rowIndex = convertRowIndexToView(cell.getRow().getIndex());
        int columnIndex = convertColumnIndexToView(cell.getIndex());
        return getCellRect(rowIndex, columnIndex, true);
    }

    public void scrollCellToVisible(DataModelCell cell) {
        Rectangle cellRectangle = getCellRect(cell);
        scrollRectToVisible(cellRectangle);
    }
}
