package com.dci.intellij.dbn.editor.data.ui.table.cell;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Insets;
import java.awt.event.KeyEvent;

import com.dci.intellij.dbn.common.Colors;
import com.dci.intellij.dbn.common.ui.KeyUtil;
import com.dci.intellij.dbn.data.editor.ui.TextFieldWithTextEditor;
import com.dci.intellij.dbn.data.model.ColumnInfo;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.editor.data.model.DatasetEditorModelCell;
import com.dci.intellij.dbn.editor.data.ui.table.DatasetEditorTable;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.actionSystem.Shortcut;
import com.intellij.openapi.project.Project;
import com.intellij.ui.RoundedLineBorder;

public class DatasetTableCellEditorWithTextEditor extends DatasetTableCellEditor {
    private static final Insets EMPTY_INSETS = new Insets(0, 0, 0, 0);
    private static final EmptyBorder BUTTON_OUTSIDE_BORDER = new EmptyBorder(1, 1, 1, 1);
    private static final EmptyBorder BUTTON_INSIDE_BORDER = new EmptyBorder(0, 2, 0, 2);
    private static final RoundedLineBorder BUTTON_LINE_BORDER = new RoundedLineBorder(Colors.BUTTON_BORDER_COLOR, 4);
    private static final CompoundBorder BUTTON_BORDER = new CompoundBorder(BUTTON_OUTSIDE_BORDER, new CompoundBorder(BUTTON_LINE_BORDER, BUTTON_INSIDE_BORDER));


    public DatasetTableCellEditorWithTextEditor(DatasetEditorTable table) {
        super(table, createTextField(table.getDataset().getProject()));
        TextFieldWithTextEditor editorComponent = getEditorComponent();
        JTextField textField = editorComponent.getTextField();
        textField.setBorder(new EmptyBorder(EMPTY_INSETS));
        JLabel button = editorComponent.getButton();
        button.setBackground(textField.getBackground());
        button.setBorder(BUTTON_BORDER);
    }

    private static TextFieldWithTextEditor createTextField(Project project) {
        return new TextFieldWithTextEditor(project) {
            @Override
            public void setEditable(boolean editable) {
                super.setEditable(editable);
                Color background = getTextField().getBackground();
                setBackground(background);
                getButton().setBackground(background);
            }
        };
    }

    public TextFieldWithTextEditor getEditorComponent() {
        return (TextFieldWithTextEditor) super.getEditorComponent();
    }

    @Override
    public void prepareEditor(final DatasetEditorModelCell cell) {
        getEditorComponent().setUserValueHolder(cell);
        setCell(cell);
        ColumnInfo columnInfo = cell.getColumnInfo();
        DBDataType dataType = columnInfo.getDataType();
        JTextField textField = getTextField();
        if (dataType.isNative()) {
            highlight(cell.hasError() ? HIGHLIGHT_TYPE_ERROR : HIGHLIGHT_TYPE_NONE);
            if (dataType.getNativeDataType().isLargeObject()) {
                setEditable(false);
            } else {
                String userValue = (String) cell.getUserValue();
                setEditable(userValue == null || (userValue.length() < 1000 && userValue.indexOf('\n') == -1));
            }
            selectText(textField);
        }
    }

    @Override
    public void setEditable(boolean editable) {
        TextFieldWithTextEditor editorComponent = getEditorComponent();
        editorComponent.setEditable(editable);
    }

    /********************************************************
     *                      KeyListener                     *
     ********************************************************/
    public void keyPressed(KeyEvent keyEvent) {
        Shortcut[] shortcuts = KeyUtil.getShortcuts(IdeActions.ACTION_SHOW_INTENTION_ACTIONS);
        if (!keyEvent.isConsumed() && KeyUtil.match(shortcuts, keyEvent)) {
            keyEvent.consume();
            getEditorComponent().openEditor();
        } else {
            super.keyPressed(keyEvent);
        }
    }

}
