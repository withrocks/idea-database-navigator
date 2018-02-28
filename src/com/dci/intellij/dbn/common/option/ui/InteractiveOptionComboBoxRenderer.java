package com.dci.intellij.dbn.common.option.ui;

import javax.swing.JList;

import com.dci.intellij.dbn.common.option.InteractiveOption;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;

public class InteractiveOptionComboBoxRenderer extends ColoredListCellRenderer<InteractiveOption>{
    public static final InteractiveOptionComboBoxRenderer INSTANCE = new InteractiveOptionComboBoxRenderer();

    private InteractiveOptionComboBoxRenderer() {}

    protected void customizeCellRenderer(JList list, InteractiveOption value, int index, boolean selected, boolean hasFocus) {
        if (value != null) {
            setIcon(value.getIcon());
            append(value.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
        }
    }
}
