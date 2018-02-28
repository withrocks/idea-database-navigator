package com.dci.intellij.dbn.data.grid.ui.table.basic;

import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Component;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.dci.intellij.dbn.common.ui.table.DBNTableGutter;

public class BasicTableGutter<T extends BasicTable> extends DBNTableGutter<T> {
    public BasicTableGutter(final T table) {
        super(table);
        addListSelectionListener(gutterSelectionListener);
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        if (getModel().getSize() == 0) {
            setFixedCellWidth(10);
        }
        table.getSelectionModel().addListSelectionListener(tableSelectionListener);
        table.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent e) {
                Object newProperty = e.getNewValue();
                if (newProperty instanceof Font) {
                    Font font = (Font) newProperty;
                    setFont(font);
                    setFixedCellHeight(table.getRowHeight());
                    ListCellRenderer cellRenderer = getCellRenderer();
                    if (cellRenderer instanceof Component) {
                        Component component = (Component) cellRenderer;
                        component.setFont(font);
                    }
                }
            }
        });
    }

    protected ListCellRenderer createCellRenderer() {
        return new BasicTableGutterCellRenderer();
    }

    public void scrollRectToVisible(Rectangle rect) {
        super.scrollRectToVisible(rect);

        T table = getTable();
        Rectangle tableRect = table.getVisibleRect();

        tableRect.y = rect.y;
        tableRect.height = rect.height;
        table.scrollRectToVisible(tableRect);
    }

    boolean justGainedFocus = false;

    @Override
    protected void processFocusEvent(FocusEvent e) {
        super.processFocusEvent(e);
        if (e.getComponent() == this) {
            justGainedFocus = e.getID() == FocusEvent.FOCUS_GAINED;
        }
    }

    /*********************************************************
     *                ListSelectionListener                  *
     *********************************************************/
    private ListSelectionListener gutterSelectionListener = new ListSelectionListener() {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            T table = getTable();
            if (hasFocus()) {
                int lastColumnIndex = table.getColumnCount() - 1;
                if (justGainedFocus) {
                    justGainedFocus = false;
                    if (table.isEditing()) table.getCellEditor().cancelCellEditing();
                    table.clearSelection();
                    table.setColumnSelectionInterval(0, lastColumnIndex);
                }

                for (int i = e.getFirstIndex(); i <= e.getLastIndex(); i++) {
                    if (isSelectedIndex(i))
                        table.getSelectionModel().addSelectionInterval(i, i); else
                        table.getSelectionModel().removeSelectionInterval(i, i);
                }
            }
        }
    };

    private ListSelectionListener tableSelectionListener = new ListSelectionListener() {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            revalidate();
            repaint();
        }
    };

    @Override
    public void dispose() {
        if (!isDisposed()) {
            getTable().getSelectionModel().removeListSelectionListener(tableSelectionListener);
            removeListSelectionListener(gutterSelectionListener);
            tableSelectionListener = null;
            gutterSelectionListener = null;
            super.dispose();

        }
    }
}
