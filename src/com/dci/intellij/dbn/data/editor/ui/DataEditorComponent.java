package com.dci.intellij.dbn.data.editor.ui;

import com.dci.intellij.dbn.common.Colors;
import com.dci.intellij.dbn.common.dispose.Disposable;
import com.intellij.ui.RoundedLineBorder;

import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

public interface DataEditorComponent extends Disposable{
    EmptyBorder BUTTON_OUTSIDE_BORDER = new EmptyBorder(1, 1, 1, 1);
    EmptyBorder BUTTON_INSIDE_BORDER = new EmptyBorder(0, 8, 0, 8);
    RoundedLineBorder BUTTON_LINE_BORDER = new RoundedLineBorder(Colors.BUTTON_BORDER_COLOR, 4);
    CompoundBorder BUTTON_BORDER = new CompoundBorder(BUTTON_OUTSIDE_BORDER, new CompoundBorder(BUTTON_LINE_BORDER, BUTTON_INSIDE_BORDER));

    JTextField getTextField();

    void setEditable(boolean editable);

    void setEnabled(boolean enabled);

    UserValueHolder getUserValueHolder();

    void setUserValueHolder(UserValueHolder userValueHolder);

    String getText();

    void setText(String text);
}
