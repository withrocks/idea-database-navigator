package com.dci.intellij.dbn.common.ui.list;

import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellEditor;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.dci.intellij.dbn.common.ui.table.DBNEditableTable;
import com.dci.intellij.dbn.common.ui.table.DBNEditableTableModel;
import com.dci.intellij.dbn.common.ui.table.DBNTableGutter;
import com.dci.intellij.dbn.common.ui.table.IndexTableGutter;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.intellij.openapi.project.Project;
import com.intellij.ui.ColoredTableCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.border.CustomLineBorder;
import com.intellij.util.ui.UIUtil;

public class EditableStringList extends DBNEditableTable<EditableStringList.EditableListModel> {
    private boolean sorted;
    private boolean indexed;

    public EditableStringList(boolean sorted, boolean indexed) {
        this(null, new ArrayList<String>(), sorted, indexed);
    }
    public EditableStringList(Project project, List<String> elements, boolean sorted, boolean indexed) {
        super(project, new EditableListModel(elements, sorted), false);
        setTableHeader(null);
        this.sorted = sorted;
        this.indexed = indexed;

        if (indexed) {
            getColumnModel().getColumn(0).setPreferredWidth(20);
            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    accommodateColumnsSize();
                }
            });
        }

        setDefaultRenderer(String.class, new ColoredTableCellRenderer() {
            @Override
            protected void customizeCellRenderer(JTable table, Object value, boolean selected, boolean hasFocus, int row, int column) {
                acquireState(table, false, false, row, column);
                Color background = table.getBackground();
                Color foreground = table.getForeground();
                SimpleTextAttributes attributes = SimpleTextAttributes.SIMPLE_CELL_ATTRIBUTES;
                if (selected && !table.isEditing()) {
                    background = UIUtil.getListSelectionBackground();
                    foreground = UIUtil.getListSelectionForeground();
                    attributes = SimpleTextAttributes.SELECTED_SIMPLE_CELL_ATTRIBUTES;

                }
                setBorder(new LineBorder(background, 2));
                setBackground(background);
                setForeground(foreground);
                append((String) value, attributes);
            }
        });

        addKeyListener(keyListener);
    }

    @Override
    public DBNTableGutter createTableGutter() {
        return indexed ? new IndexTableGutter(this) : null;
    }

    @Override
    public Component prepareEditor(final TableCellEditor editor, int rowIndex, int columnIndex) {
        JTextField component = (JTextField) super.prepareEditor(editor, rowIndex, columnIndex);
        component.setBorder(new CustomLineBorder(component.getBackground(), 0, 3, 0, 0));
        component.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (e.getOppositeComponent() != EditableStringList.this) {
                    editor.stopCellEditing();
                }
            }
        });

        component.addKeyListener(keyListener);
        return component;
    }

    private KeyAdapter keyListener = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            if (!e.isConsumed()) {
                int selectedRow = getSelectedRow();
                int keyCode = e.getKeyCode();
                if (keyCode == KeyEvent.VK_DOWN) {
                    if (selectedRow == getModel().getSize() - 1) {
                        e.consume();
                        insertRow();
                    }
                } else if (keyCode == KeyEvent.VK_ENTER && e.getModifiers() == 0) {
                    e.consume();
                    insertRow();
                } else if (keyCode == KeyEvent.VK_BACK_SPACE || keyCode == KeyEvent.VK_DELETE) {
                    Object source = e.getSource();
                    String value = source == EditableStringList.this ?
                            (String) getModel().getValueAt(selectedRow, 0) :
                            ((JTextField) source).getText();

                    if (StringUtil.isEmpty(value)) {
                        e.consume();
                        removeRow();
                    }
                }
            }
        }
    };

    @Override
    public Component getEditorComponent() {
        return super.getEditorComponent();
    }

    public List<String> getStringValues() {
        return getModel().getData();
    }

    public void setStringValues(Collection<String> stringValues) {
        setModel(new EditableListModel(stringValues, sorted));
    }


    public static class EditableListModel extends DBNEditableTableModel {
        private List<String> data;

        public EditableListModel(Collection<String> data, boolean sorted) {
            this.data = new ArrayList<String>(data);
            if (sorted) Collections.sort(this.data);
        }

        public List<String> getData() {
            return data;
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public String getColumnName(int columnIndex) {
            return "DATA";
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return data.get(rowIndex);
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            boolean change = true;
            if (rowIndex < data.size()) {
                String currentValue = data.get(rowIndex);
                if (currentValue.equals(value)) {
                    change = false;
                }
            }

            if (change) {
                data.set(rowIndex, (String) value);
                notifyListeners(rowIndex, rowIndex, columnIndex);
            }
        }

        @Override
        public void insertRow(int rowIndex) {
            data.add(rowIndex, "");
            notifyListeners(rowIndex, data.size() + 1, -1);
        }

        @Override
        public void removeRow(int rowIndex) {
            if (rowIndex >-1 && rowIndex<data.size()) {
                data.remove(rowIndex);
                notifyListeners(rowIndex, data.size() + 1, -1);
            }
        }

        @Override
        public int getSize() {
            return data.size();
        }

        @Override
        public Object getElementAt(int index) {
            return getValueAt(index, 0);
        }
    }
}
