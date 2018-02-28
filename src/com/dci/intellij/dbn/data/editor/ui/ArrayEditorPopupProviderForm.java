package com.dci.intellij.dbn.data.editor.ui;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.table.TableCellEditor;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.Borders;
import com.dci.intellij.dbn.common.ui.KeyUtil;
import com.dci.intellij.dbn.common.ui.list.EditableStringList;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.common.util.TextAttributesUtil;
import com.dci.intellij.dbn.data.grid.color.DataGridTextAttributesKeys;
import com.dci.intellij.dbn.data.value.ArrayValue;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.DocumentAdapter;

public class ArrayEditorPopupProviderForm extends TextFieldPopupProviderForm {
    private JPanel mainPanel;
    private JPanel rightActionPanel;
    private JPanel leftActionPanel;
    private JScrollPane listScrollPane;

    private ArrayEditorList list;
    private boolean changed;

    public ArrayEditorPopupProviderForm(TextFieldWithPopup textField, boolean autoPopup) {
        super(textField, autoPopup, true);

        ActionToolbar leftActionToolbar = ActionUtil.createActionToolbar(
                "DBNavigator.Place.DataEditor.TextAreaPopup", true,
                new AddAction(),
                new RemoveAction());
        leftActionPanel.add(leftActionToolbar.getComponent(), BorderLayout.WEST);

        ActionToolbar rightActionToolbar = ActionUtil.createActionToolbar(
                "DBNavigator.Place.DataEditor.TextAreaPopup", true,
                new DeleteAction(),
                new RevertAction(),
                new AcceptAction());
        rightActionPanel.add(rightActionToolbar.getComponent(), BorderLayout.EAST);
        list = new ArrayEditorList();
        listScrollPane.setViewportView(list);
        listScrollPane.getViewport().setBackground(list.getBackground());
        listScrollPane.setBorder(Borders.COMPONENT_LINE_BORDER);
        list.initTableGutter();
        list.addKeyListener(this);
        Color bgColor = TextAttributesUtil.getSimpleTextAttributes(DataGridTextAttributesKeys.PLAIN_DATA).getBgColor();
        if (bgColor != null) {
            list.setBackground(bgColor);
            listScrollPane.getViewport().setBackground(bgColor);
        }

        mainPanel.addKeyListener(this);
    }

    public JComponent getComponent() {
        return mainPanel;
    }

    private class ArrayEditorList extends EditableStringList {
        public ArrayEditorList() {
            super(false, true);
        }

        @Override
        public Component prepareEditor(TableCellEditor editor, int rowIndex, int columnIndex) {
            Component component = super.prepareEditor(editor, rowIndex, columnIndex);
            component.addKeyListener(ArrayEditorPopupProviderForm.this);
            return component;
        }
    }

    public JBPopup createPopup() {
        JTextField textField = getTextField();

        List<String> stringValues = new ArrayList<String>();
        UserValueHolder userValueHolder = getEditorComponent().getUserValueHolder();
        Project project = getProject();
        try {
            Object userValue = userValueHolder.getUserValue();
            ArrayValue array = (ArrayValue) userValue;
            List<String> values = array.read();
            if (values != null) {
                stringValues.addAll(values);
            }
        } catch (SQLException e) {
            MessageUtil.showErrorDialog(project, e.getMessage(), e);
            return null;
        }
        list.setStringValues(stringValues);
        if (list.getModel().getSize() > 0) {
            list.selectCell(0,0);
        }

        //editorTextArea.setText(text);
        changed = false;
        //if (textField.isEditable()) editorTextArea.setCaretPosition(textField.getCaretPosition());
        //editorTextArea.setSelectionStart(textField.getSelectionStart());
        //editorTextArea.setSelectionEnd(textField.getSelectionEnd());
        //editorTextArea.getDocument().addDocumentListener(new DocumentListener());
        mainPanel.setPreferredSize(new Dimension(Math.max(200, textField.getWidth() + 32), 200));

        ComponentPopupBuilder popupBuilder = JBPopupFactory.getInstance().createComponentPopupBuilder(mainPanel, list);
        popupBuilder.setRequestFocus(true);
        popupBuilder.setResizable(true);
        popupBuilder.setDimensionServiceKey(project, "ArrayEditor." + userValueHolder.getName(), false);
        return popupBuilder.createPopup();
    }

    public void handleKeyPressedEvent(KeyEvent e) {}
    public void handleKeyReleasedEvent(KeyEvent e) {}
    public void handleFocusLostEvent(FocusEvent e) {}

    public String getKeyShortcutName() {
        return IdeActions.ACTION_SHOW_INTENTION_ACTIONS;
    }

    public String getDescription() {
        return "Array Editor";
    }

    @Override
    public TextFieldPopupType getPopupType() {
        return TextFieldPopupType.TEXT_EDITOR;
    }

    @Nullable
    @Override
    public Icon getButtonIcon() {
        return Icons.DATA_EDITOR_BROWSE;
    }

    public void keyPressed(KeyEvent e) {
        if (!e.isConsumed()) {
            if (e.getKeyCode() == 27) {
                if (list.isEditing()) {
                    list.stopCellEditing();
                }
            } else {
                super.keyPressed(e);
            }
        }

    }

    private class DocumentListener extends DocumentAdapter {
        protected void textChanged(DocumentEvent documentEvent) {
            changed = true;
        }
    }

    private class AcceptAction extends DumbAwareAction {
        private AcceptAction() {
            super("Accept changes", null, Icons.TEXT_CELL_EDIT_ACCEPT);
            setShortcutSet(KeyUtil.createShortcutSet(KeyEvent.VK_ENTER, InputEvent.ALT_MASK));
            registerAction(this);
        }

        public void actionPerformed(@NotNull AnActionEvent e) {
            list.stopCellEditing();
            UserValueHolder userValueHolder = getEditorComponent().getUserValueHolder();
            userValueHolder.updateUserValue(list.getModel().getData(), false);

/*
            String text = editorTextArea.getText().trim();

            if (userValueHolder.getUserValue() instanceof String) {
                JTextField textField = getTextField();
                getEditorComponent().setEditable(text.indexOf('\n') == -1);

                textField.setText(text);
            }
*/
            hidePopup();
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            getTemplatePresentation().setEnabled(changed);
        }
    }

    private class RevertAction extends DumbAwareAction{
        private RevertAction() {
            super("Revert changes", null, Icons.TEXT_CELL_EDIT_REVERT);
            setShortcutSet(KeyUtil.createShortcutSet(KeyEvent.VK_ESCAPE, 0));
            registerAction(this);
        }

        public void actionPerformed(@NotNull AnActionEvent e) {
            hidePopup();
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            getTemplatePresentation().setEnabled(changed);
        }
    }

    private class DeleteAction extends AnAction {
        private DeleteAction() {
            super("Delete Content", null, Icons.TEXT_CELL_EDIT_DELETE);
            setShortcutSet(KeyUtil.createShortcutSet(KeyEvent.VK_DELETE, InputEvent.CTRL_MASK));
            registerAction(this);
        }

        public void actionPerformed(@NotNull AnActionEvent e) {
            TextFieldWithPopup editorComponent = getEditorComponent();
            editorComponent.getUserValueHolder().updateUserValue(new ArrayList<String>(), false);
            hidePopup();
        }
    }

    private class AddAction extends AnAction {
        private AddAction() {
            super("Add Value", null, Icons.ARRAY_CELL_EDIT_ADD);
            setShortcutSet(KeyUtil.createShortcutSet(KeyEvent.VK_PLUS, InputEvent.CTRL_MASK));
            setShortcutSet(KeyUtil.createShortcutSet(KeyEvent.VK_ADD, InputEvent.CTRL_MASK));
            registerAction(this);
        }

        public void actionPerformed(@NotNull AnActionEvent e) {
            list.insertRow();
        }
    }

    private class RemoveAction extends AnAction {
        private RemoveAction() {
            super("Remove Value", null, Icons.ARRAY_CELL_EDIT_REMOVE);
            setShortcutSet(KeyUtil.createShortcutSet(KeyEvent.VK_MINUS, InputEvent.CTRL_MASK));
            setShortcutSet(KeyUtil.createShortcutSet(KeyEvent.VK_SUBTRACT, InputEvent.CTRL_MASK));
            registerAction(this);
        }

        public void actionPerformed(@NotNull AnActionEvent e) {
            list.removeRow();
        }
    }

    public void dispose() {
        super.dispose();
    }

}
