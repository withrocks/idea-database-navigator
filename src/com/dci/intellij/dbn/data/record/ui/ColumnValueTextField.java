package com.dci.intellij.dbn.data.record.ui;

import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.ui.MouseUtil;
import com.dci.intellij.dbn.common.util.TextAttributesUtil;
import com.dci.intellij.dbn.data.grid.color.DataGridTextAttributesKeys;
import com.dci.intellij.dbn.data.record.DatasetRecord;
import com.dci.intellij.dbn.editor.data.DatasetEditorManager;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilterInput;
import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBConstraint;
import com.dci.intellij.dbn.object.DBDataset;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.ui.SimpleTextAttributes;

public class ColumnValueTextField extends JTextField {
    private DatasetRecord record;
    private DBObjectRef<DBColumn> columnRef;

    public ColumnValueTextField(DatasetRecord record, DBColumn column) {
        this.record = record;
        this.columnRef = DBObjectRef.from(column);
        if (column.isPrimaryKey()) {
            SimpleTextAttributes textAttributes = TextAttributesUtil.getSimpleTextAttributes(DataGridTextAttributesKeys.PRIMARY_KEY);
            setForeground(textAttributes.getFgColor());
            Color background = textAttributes.getBgColor();
            if (background != null) {
                setBackground(background);
            }
        } else if (column.isForeignKey()) {
            addMouseListener(mouseListener);
            SimpleTextAttributes textAttributes = TextAttributesUtil.getSimpleTextAttributes(DataGridTextAttributesKeys.FOREIGN_KEY);
            setForeground(textAttributes.getFgColor());
            Color background = textAttributes.getBgColor();
            if (background != null) {
                setBackground(background);
            }
        }

    }

    protected void processMouseMotionEvent(MouseEvent e) {
        DBColumn column = getColumn();
        if (column != null && column.isForeignKey()) {
            if (e.isControlDown() && e.getID() != MouseEvent.MOUSE_DRAGGED && record.getColumnValue(column) != null) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setToolTipText("<html>Show referenced <b>" + column.getForeignKeyColumn().getDataset().getQualifiedName() + "</b> record<html>");
            } else {
                super.processMouseMotionEvent(e);
                setCursor(Cursor.getDefaultCursor());
                setToolTipText(null);
            }
        } else {
            super.processMouseMotionEvent(e);
        }
    }
    
    @Nullable
    public DatasetFilterInput resolveForeignKeyRecord() {
        DBColumn column = getColumn();
        if (column != null) {
            for (DBConstraint constraint : column.getConstraints()) {
                if (constraint.isForeignKey()) {
                    DBConstraint foreignKeyConstraint = constraint.getForeignKeyConstraint();
                    if (foreignKeyConstraint != null) {
                        DBDataset foreignKeyDataset = foreignKeyConstraint.getDataset();
                        DatasetFilterInput filterInput = new DatasetFilterInput(foreignKeyDataset);

                        for (DBColumn constraintColumn : constraint.getColumns()) {
                            DBObject constraintCol = constraintColumn.getUndisposedElement();
                            if (constraintCol != null) {
                                DBColumn foreignKeyColumn = ((DBColumn) constraintCol).getForeignKeyColumn();
                                Object value = record.getColumnValue(column);
                                filterInput.setColumnValue(foreignKeyColumn, value);
                            }
                        }
                        return filterInput;
                    }
                }
            }
        }

        return null;
    }

    private DBColumn getColumn() {
        return DBObjectRef.get(columnRef);
    }

    MouseListener mouseListener = new MouseAdapter() {
        public void mouseClicked(MouseEvent event) {
            DBColumn column = getColumn();
            if (column != null && MouseUtil.isNavigationEvent(event)) {
                if (column.isForeignKey() && record.getColumnValue(column) != null) {
                    DatasetFilterInput filterInput = resolveForeignKeyRecord();
                    DatasetEditorManager datasetEditorManager = DatasetEditorManager.getInstance(column.getProject());
                    datasetEditorManager.navigateToRecord(filterInput, event);
                    event.consume();
                }
            }
        }        
        
    };
}
