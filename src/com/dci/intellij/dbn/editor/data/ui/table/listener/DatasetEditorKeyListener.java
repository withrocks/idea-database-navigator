package com.dci.intellij.dbn.editor.data.ui.table.listener;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;

import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.editor.data.model.DatasetEditorModel;
import com.dci.intellij.dbn.editor.data.model.DatasetEditorModelCell;
import com.dci.intellij.dbn.editor.data.ui.table.DatasetEditorTable;

public class DatasetEditorKeyListener extends KeyAdapter {
    private DatasetEditorTable table;

    public DatasetEditorKeyListener(DatasetEditorTable table) {
        this.table = table;
    }

    public void keyPressed(KeyEvent e) {
        DatasetEditorModel model = table.getModel();
        if (!e.isConsumed()) {
            int keyChar = e.getKeyChar();
            if (model.isInserting()) {
                switch (keyChar) {
                    case 27:  // escape
                        model.cancelInsert(true);
                        break;
                    case 10:  // enter
                        int index = model.getInsertRowIndex();
                        try {
                            model.postInsertRecord(false, true, false);
                            if (!model.isInserting()) {
                                model.insertRecord(index + 1);
                            }
                        } catch (SQLException e1) {
                            MessageUtil.showErrorDialog(table.getProject(), "Could not create row in " + table.getDataset().getQualifiedNameWithType() + ".", e1);
                        }
                        e.consume();
                }
            } else if (!table.isEditing()){
                if (keyChar == 127) {
                    for (int rowIndex : table.getSelectedRows()) {
                        for (int columnIndex : table.getSelectedColumns()) {
                            DatasetEditorModelCell cell = model.getCellAt(rowIndex, columnIndex);
                            DBDataType dataType = cell.getColumnInfo().getDataType();
                            if (dataType != null && dataType.isNative() && !dataType.getNativeDataType().isLargeObject()) {
                                cell.updateUserValue(null, true);
                            }
                        }
                    }
                    table.revalidate();
                    table.repaint();
                }
            }
        }

    }

    public void keyReleased(KeyEvent e) {
    }
}
