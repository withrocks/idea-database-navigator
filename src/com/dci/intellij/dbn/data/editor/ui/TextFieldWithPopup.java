package com.dci.intellij.dbn.data.editor.ui;

import com.dci.intellij.dbn.common.ui.KeyUtil;
import com.intellij.openapi.actionSystem.Shortcut;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.Document;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class TextFieldWithPopup extends JPanel implements DataEditorComponent {
    private JTextField textField;
    private JPanel buttonsPanel;
    //private JLabel button;

    private List<TextFieldPopupProvider> popupProviders = new ArrayList<TextFieldPopupProvider>();
    private UserValueHolder userValueHolder;
    private Project project;

    public TextFieldWithPopup(Project project) {
        super(new BorderLayout(2, 0));
        this.project = project;
        setMaximumSize(new Dimension(-1, 24));
        setPreferredSize(new Dimension(-1, 24));
        setMinimumSize(new Dimension(180, 24));

        textField = new JTextField();
        textField.setMargin(new Insets(0, 1, 0, 1));
        add(textField, BorderLayout.CENTER);

        buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        add(buttonsPanel, BorderLayout.EAST);

        textField.setPreferredSize(new Dimension(150, 24));
        textField.setMaximumSize(new Dimension(-1, 24));
        textField.addKeyListener(keyListener);
        textField.addFocusListener(focusListener);

        customizeTextField(textField);
    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);
        if (textField != null) textField.setFont(font);
    }

    public Project getProject() {
        return project;
    }

    public void setEditable(boolean editable){
        textField.setEditable(editable);
    }
                                                                                  
    public void setUserValueHolder(UserValueHolder userValueHolder) {
        this.userValueHolder = userValueHolder;
    }

    public void customizeTextField(JTextField textField) {}
    public void customizeButton(JLabel button) {
        int width = (int) button.getPreferredSize().getWidth();
        int height = (int) textField.getPreferredSize().getHeight();
        button.setPreferredSize(new Dimension(width, height));
    }

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

    @Override
    public void setEnabled(boolean enabled) {
        //textField.setEnabled(enabled);
        textField.setEditable(enabled);
        for (TextFieldPopupProvider popupProvider : popupProviders) {
            JLabel button = popupProvider.getButton();
            if (button != null) {
                button.setVisible(enabled);
            }
        }
    }

    /******************************************************
     *                    PopupProviders                  *
     ******************************************************/
    public void createValuesListPopup(ListPopupValuesProvider valuesProvider, boolean buttonVisible) {
        ValueListPopupProvider popupProvider = new ValueListPopupProvider(this, valuesProvider, false, buttonVisible);
        addPopupProvider(popupProvider);
    }

    @Deprecated
    public void createValuesListPopup(List<String> valuesList, boolean buttonVisible, boolean dynamicFiltering) {
        ValuesListPopupProviderForm popupProviderForm = new ValuesListPopupProviderForm(this, new BasicListPopupValuesProvider("Possible Values List", valuesList), buttonVisible, dynamicFiltering);
        addPopupProvider(popupProviderForm);
    }

    @Deprecated
    public void createValuesListPopup(ListPopupValuesProvider valuesProvider, boolean buttonVisible, boolean dynamicFiltering) {
        ValuesListPopupProviderForm popupProviderForm = new ValuesListPopupProviderForm(this, valuesProvider, buttonVisible, dynamicFiltering);
        addPopupProvider(popupProviderForm);
    }

    public void createTextEditorPopup(boolean autoPopup) {
        TextEditorPopupProviderForm popupProvider = new TextEditorPopupProviderForm(this, autoPopup);
        addPopupProvider(popupProvider);
    }

    public void createCalendarPopup(boolean autoPopup) {
        CalendarPopupProviderForm popupProviderForm = new CalendarPopupProviderForm(this, autoPopup);
        addPopupProvider(popupProviderForm);
    }

    public void createArrayEditorPopup(boolean autoPopup) {
        ArrayEditorPopupProviderForm popupProviderForm = new ArrayEditorPopupProviderForm(this, autoPopup);
        addPopupProvider(popupProviderForm);
    }

    private void addPopupProvider(TextFieldPopupProvider popupProvider) {
        popupProviders.add(popupProvider);

        if (popupProvider.isButtonVisible()) {
            Icon buttonIcon = popupProvider.getButtonIcon();
            JLabel button = new JLabel(buttonIcon);
            button.setBorder(BUTTON_BORDER);
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            String toolTipText = "Open " + popupProvider.getDescription();
            String keyShortcutDescription = popupProvider.getKeyShortcutDescription();
            if (keyShortcutDescription != null) {
                toolTipText += " (" + keyShortcutDescription + ')';
            }
            button.setToolTipText(toolTipText);

            button.addMouseListener(new ButtonMouseListener(popupProvider));
            buttonsPanel.add(button, buttonsPanel.getComponentCount());
            customizeButton(button);
            popupProvider.setButton(button);
        }
        Disposer.register(this, popupProvider);
    }

    public void setPopupEnabled(TextFieldPopupType popupType, boolean enabled) {
        for (TextFieldPopupProvider popupProvider : popupProviders) {
            if (popupProvider.getPopupType() == popupType) {
                popupProvider.setEnabled(enabled);
                JLabel button = popupProvider.getButton();
                if (button != null) {
                    button.setVisible(enabled);
                }
                break;
            }
        }
    }

    public void hideActivePopup() {
        TextFieldPopupProvider popupProvider = getActivePopupProvider();
        if ( popupProvider != null) {
             popupProvider.hidePopup();
        }
    }

    public TextFieldPopupProvider getAutoPopupProvider() {
        for (TextFieldPopupProvider popupProvider : popupProviders) {
            if (popupProvider.isAutoPopup()) {
                return popupProvider;
            }
        }
        return null;
    }

    public TextFieldPopupProvider getDefaultPopupProvider() {
        return popupProviders.get(0);
    }

    public TextFieldPopupProvider getActivePopupProvider() {
        for (TextFieldPopupProvider popupProvider : popupProviders) {
            if (popupProvider.isShowingPopup()) {
                return popupProvider;
            }
        }
        return null;
    }

    public TextFieldPopupProvider getPopupProvider(KeyEvent keyEvent) {
        for (TextFieldPopupProvider popupProvider : popupProviders) {
            Shortcut[] shortcuts = popupProvider.getShortcuts();
            if (KeyUtil.match(shortcuts, keyEvent)) {
                return popupProvider;
            }
        }
        return null;
    }

    /********************************************************
     *                    FocusListener                     *
     ********************************************************/
    private FocusListener focusListener = new FocusAdapter() {
        @Override
        public void focusLost(FocusEvent focusEvent) {
            TextFieldPopupProvider popupProvider = getActivePopupProvider();
            if (popupProvider != null) {
                popupProvider.handleFocusLostEvent(focusEvent);
            }
        }
    };

    /********************************************************
     *                      KeyListener                     *
     ********************************************************/
    private KeyListener keyListener = new KeyAdapter() {
        public void keyPressed(KeyEvent keyEvent) {
            TextFieldPopupProvider popupProvider = getActivePopupProvider();
            if (popupProvider != null) {
                popupProvider.handleKeyPressedEvent(keyEvent);

            } else {
                popupProvider = getPopupProvider(keyEvent);
                if (popupProvider != null && popupProvider.isEnabled()) {
                    hideActivePopup();
                    popupProvider.showPopup();
                }
            }
        }

        public void keyReleased(KeyEvent keyEvent) {
            TextFieldPopupProvider popupProvider = getActivePopupProvider();
            if (popupProvider != null) {
                popupProvider.handleKeyReleasedEvent(keyEvent);

            }
        }
    };
    /********************************************************
     *                    ActionListener                    *
     ********************************************************/
    private ActionListener actionListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            TextFieldPopupProvider defaultPopupProvider = getDefaultPopupProvider();
            TextFieldPopupProvider popupProvider = getActivePopupProvider();
            if (popupProvider == null || popupProvider != defaultPopupProvider) {
                hideActivePopup();
                defaultPopupProvider.showPopup();
            }
        }
    };

    private class ButtonMouseListener extends MouseAdapter {
        TextFieldPopupProvider popupProvider;

        public ButtonMouseListener(TextFieldPopupProvider popupProvider) {
            this.popupProvider = popupProvider;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            getTextField().requestFocus();
            TextFieldPopupProvider activePopupProvider = getActivePopupProvider();
            if (activePopupProvider == null || activePopupProvider != popupProvider) {
                hideActivePopup();
                popupProvider.showPopup();
            }
        }
    };

    public UserValueHolder getUserValueHolder() {
        return userValueHolder;
    }

    /********************************************************
     *                    Disposable                        *
     ********************************************************/
    private boolean disposed;

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    @Override
    public void dispose() {
        if (!disposed) {
            disposed = true;
            userValueHolder = null;
        }
    }

}
