package com.dci.intellij.dbn.data.editor.ui;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.Borders;
import com.dci.intellij.dbn.common.ui.KeyUtil;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.common.util.TextAttributesUtil;
import com.dci.intellij.dbn.data.grid.color.DataGridTextAttributesKeys;
import com.dci.intellij.dbn.data.value.LargeObjectValue;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.DocumentAdapter;

public class TextEditorPopupProviderForm extends TextFieldPopupProviderForm {
    private JPanel mainPanel;
    private JPanel rightActionPanel;
    private JPanel leftActionPanel;
    private JTextArea editorTextArea;
    private JScrollPane textEditorScrollPane;
    private boolean changed;

    public TextEditorPopupProviderForm(TextFieldWithPopup textField, boolean autoPopup) {
        super(textField, autoPopup, true);
        editorTextArea.setBorder(new EmptyBorder(4, 4, 4, 4));
        editorTextArea.addKeyListener(this);
        editorTextArea.setWrapStyleWord(true);
        Color bgColor = TextAttributesUtil.getSimpleTextAttributes(DataGridTextAttributesKeys.DEFAULT_PLAIN_DATA).getBgColor();
        if (bgColor != null) {
            editorTextArea.setBackground(bgColor);
        }

        textEditorScrollPane.setBorder(Borders.COMPONENT_LINE_BORDER);

        ActionToolbar leftActionToolbar = ActionUtil.createActionToolbar(
                "DBNavigator.Place.DataEditor.TextAreaPopup", true);
        leftActionPanel.add(leftActionToolbar.getComponent(), BorderLayout.WEST);

        ActionToolbar rightActionToolbar = ActionUtil.createActionToolbar(
                "DBNavigator.Place.DataEditor.TextAreaPopup", true,
                new DeleteAction(),
                new RevertAction(),
                new AcceptAction());
        rightActionPanel.add(rightActionToolbar.getComponent(), BorderLayout.EAST);
    }

    public JComponent getComponent() {
        return mainPanel;
    }

    public JBPopup createPopup() {
        JTextField textField = getTextField();
        String text = "";
        UserValueHolder userValueHolder = getEditorComponent().getUserValueHolder();
        if (textField.isEditable()) {
            text = textField.getText();
        } else {
            Object userValue = userValueHolder.getUserValue();
            if (userValue instanceof String) {
                text = (String) userValue;
            } else if (userValue instanceof LargeObjectValue) {
                LargeObjectValue largeObjectValue = (LargeObjectValue) userValue;
                try {
                    text = CommonUtil.nvl(largeObjectValue.read(), "");
                } catch (SQLException e) {
                    MessageUtil.showErrorDialog(getProject(), e.getMessage(), e);
                    return null;
                }
            }
        }

        editorTextArea.setText(text);
        changed = false;
        if (textField.isEditable()) editorTextArea.setCaretPosition(textField.getCaretPosition());
        editorTextArea.setSelectionStart(textField.getSelectionStart());
        editorTextArea.setSelectionEnd(textField.getSelectionEnd());
        editorTextArea.getDocument().addDocumentListener(new DocumentListener());
        mainPanel.setPreferredSize(new Dimension(Math.max(200, textField.getWidth() + 32), 160));

        ComponentPopupBuilder popupBuilder = JBPopupFactory.getInstance().createComponentPopupBuilder(mainPanel, editorTextArea);
        popupBuilder.setRequestFocus(true);
        popupBuilder.setResizable(true);
        popupBuilder.setDimensionServiceKey(getProject(), "TextEditor." + userValueHolder.getName(), false);
        return popupBuilder.createPopup();
    }

    public void handleKeyPressedEvent(KeyEvent e) {}
    public void handleKeyReleasedEvent(KeyEvent e) {}
    public void handleFocusLostEvent(FocusEvent e) {}

    public String getKeyShortcutName() {
        return IdeActions.ACTION_SHOW_INTENTION_ACTIONS;
    }

    public String getDescription() {
        return "Text Editor";
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
        super.keyPressed(e);
        if (!e.isConsumed()) {
            if (KeyUtil.match(getShortcuts(), e)) {
                editorTextArea.replaceSelection("\n");
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
            String text = editorTextArea.getText().trim();
            UserValueHolder userValueHolder = getEditorComponent().getUserValueHolder();
            userValueHolder.updateUserValue(text, false);

            if (userValueHolder.getUserValue() instanceof String) {
                JTextField textField = getTextField();
                getEditorComponent().setEditable(text.indexOf('\n') == -1);

                textField.setText(text);
            }
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
            //registerAction(this);
        }

        public void actionPerformed(AnActionEvent e) {
            hidePopup();
        }

        @Override
        public void update(AnActionEvent anActionEvent) {
            getTemplatePresentation().setEnabled(changed);
        }
    }

    private class DeleteAction extends AnAction {
        private DeleteAction() {
            super("Delete Content", null, Icons.TEXT_CELL_EDIT_DELETE);
            setShortcutSet(KeyUtil.createShortcutSet(KeyEvent.VK_DELETE, InputEvent.CTRL_MASK));
            //registerAction(this);
        }

        public void actionPerformed(AnActionEvent e) {
            JTextField textField = getTextField();
            getEditorComponent().getUserValueHolder().updateUserValue(null, false);
            getEditorComponent().setEditable(true);
            textField.setText("");
            hidePopup();
        }
    }

    public void dispose() {
        super.dispose();
    }

}
