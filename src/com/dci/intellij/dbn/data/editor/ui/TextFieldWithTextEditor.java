package com.dci.intellij.dbn.data.editor.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.KeyUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.data.editor.text.TextEditorAdapter;
import com.dci.intellij.dbn.data.editor.text.ui.TextEditorDialog;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.actionSystem.Shortcut;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.UIUtil;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.Document;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class TextFieldWithTextEditor extends JPanel implements DataEditorComponent, TextEditorAdapter {
    private JTextField textField;
    private JLabel button;

    private UserValueHolder userValueHolder;
    private Project project;
    private String displayValue;

    public TextFieldWithTextEditor(Project project) {
        this(project, null);
    }
    public TextFieldWithTextEditor(Project project, String displayValue) {
        super(new BorderLayout(2, 0));
        this.project = project;
        this.displayValue = displayValue;
        setBounds(0, 0, 0, 0);

        textField = new JTextField();
        textField.setMargin(new Insets(1, 3, 1, 1));
        add(textField, BorderLayout.CENTER);

        button = new JLabel(Icons.DATA_EDITOR_BROWSE);
        button.setBorder(BUTTON_BORDER);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addMouseListener(mouseListener);
        Shortcut[] shortcuts = KeyUtil.getShortcuts(IdeActions.ACTION_SHOW_INTENTION_ACTIONS);
        String shortcutText = KeymapUtil.getShortcutsText(shortcuts);

        button.setToolTipText("Open editor (" + shortcutText + ')');
        add(button, BorderLayout.EAST);
        if (StringUtil.isNotEmpty(displayValue)) {
            textField.setText(displayValue);
            textField.setEnabled(false);
            textField.setDisabledTextColor(UIUtil.getLabelDisabledForeground());
        }
        textField.setPreferredSize(new Dimension(150, -1));
        textField.addKeyListener(keyListener);
        textField.setEditable(false);

        button.addKeyListener(keyListener);
        addKeyListener(keyListener);

        customizeButton(button);
        customizeTextField(textField);
    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);
        if (textField != null) textField.setFont(font);
    }

    public void setEditable(boolean editable){
        textField.setEditable(editable);
    }

    public void setUserValueHolder(UserValueHolder userValueHolder) {
        this.userValueHolder = userValueHolder;
    }


    public void customizeTextField(JTextField textField) {}
    public void customizeButton(JLabel button) {}

    public boolean isSelected() {
        Document document = textField.getDocument();
        return document.getLength() > 0 &&
               textField.getSelectionStart() == 0 &&
               textField.getSelectionEnd() == document.getLength();
    }

    public void clearSelection() {
        if (isSelected()) {
            textField.setSelectionStart(0);
            textField.setSelectionEnd(0);
            textField.setCaretPosition(0);
        }
    }

    public JTextField getTextField() {
        return textField;
    }

    @Override
    public String getText() {
        return textField.getText();
    }

    @Override
    public void setText(String text) {
        textField.setText(text);
    }

    public JLabel getButton() {
        return button;
    }

    @Override
    public void setEnabled(boolean enabled) {
        textField.setEditable(enabled);
    }

    public void openEditor() {
        TextEditorDialog.show(project, this);
    }

    /********************************************************
     *                      KeyListener                     *
     ********************************************************/
    private KeyListener keyListener = new KeyAdapter() {
        public void keyPressed(KeyEvent keyEvent) {
            Shortcut[] shortcuts = KeyUtil.getShortcuts(IdeActions.ACTION_SHOW_INTENTION_ACTIONS);
            if (!keyEvent.isConsumed() && KeyUtil.match(shortcuts, keyEvent)) {
                keyEvent.consume();
                openEditor();
            }
        }
    };
    /********************************************************
     *                    ActionListener                    *
     ********************************************************/
    private ActionListener actionListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            openEditor();
        }
    };

    private MouseListener mouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            openEditor();
        }
    };

    public UserValueHolder getUserValueHolder() {
        return userValueHolder;
    }

    /********************************************************
     *                 TextEditorListener                   *
     ********************************************************/
    public void afterUpdate() {
        Object userValue = userValueHolder.getUserValue();
        if (userValue instanceof String && StringUtil.isEmpty(displayValue)) {
            String text = (String) userValue;
            setEditable(text.length() < 1000 && text.indexOf('\n') == -1);
            setText(text);
        }
    }

    /********************************************************
     *                    Disposable                        *
     ********************************************************/
    private boolean isDisposed;

    @Override
    public boolean isDisposed() {
        return isDisposed;
    }

    @Override
    public void dispose() {
        if (!isDisposed) {
            isDisposed = true;
            userValueHolder = null;
            project = null;
        }
    }
}
