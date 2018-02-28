package com.dci.intellij.dbn.data.editor.ui;

import javax.swing.Icon;
import javax.swing.JLabel;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.Shortcut;

public interface TextFieldPopupProvider extends Disposable{
    TextFieldPopupType getPopupType();

    void setEnabled(boolean enabled);

    void setButton(@Nullable JLabel button);

    @Nullable
    JLabel getButton();

    boolean isButtonVisible();

    boolean isEnabled();

    boolean isAutoPopup();

    boolean isShowingPopup();

    void showPopup();

    void hidePopup();

    void handleFocusLostEvent(FocusEvent focusEvent);

    void handleKeyPressedEvent(KeyEvent keyEvent);

    void handleKeyReleasedEvent(KeyEvent keyEvent);

    String getDescription();

    String getKeyShortcutDescription();

    Shortcut[] getShortcuts();

    @Nullable
    Icon getButtonIcon();
}
