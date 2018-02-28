package com.dci.intellij.dbn.common.ui;

import javax.swing.JComboBox;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class ComboBoxSelectionKeyListener extends KeyAdapter {
    private JComboBox comboBox;
    private ValueSelector valueSelector;
    private boolean useControlKey;

    public static KeyListener create(JComboBox comboBox, boolean useControlKey) {
        return new ComboBoxSelectionKeyListener(comboBox, useControlKey);
    }

    public static KeyListener create(ValueSelector valueSelector, boolean useControlKey) {
        return new ComboBoxSelectionKeyListener(valueSelector, useControlKey);
    }

    private ComboBoxSelectionKeyListener(JComboBox comboBox, boolean useControlKey) {
        this.comboBox = comboBox;
        this.useControlKey = useControlKey;
    }

    private ComboBoxSelectionKeyListener(ValueSelector valueSelector, boolean useControlKey) {
        this.valueSelector = valueSelector;
        this.useControlKey = useControlKey;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!e.isConsumed()) {
            if (comboBox != null) {
                int operatorSelectionIndex = comboBox.getSelectedIndex();
                if ((useControlKey && e.getModifiers() == InputEvent.CTRL_MASK) ||
                        (!useControlKey && e.getModifiers() != InputEvent.CTRL_MASK)) {
                    if (e.getKeyCode() == 38) {//UP
                        if (operatorSelectionIndex > 0) {
                            comboBox.setSelectedIndex(operatorSelectionIndex-1);
                        }
                        e.consume();
                    } else if (e.getKeyCode() == 40) { // DOWN
                        if (operatorSelectionIndex < comboBox.getItemCount() - 1) {
                            comboBox.setSelectedIndex(operatorSelectionIndex+1);
                        }
                        e.consume();
                    }
                }
            }

            if(valueSelector != null) {
                if ((useControlKey && e.getModifiers() == InputEvent.CTRL_MASK) ||
                        (!useControlKey && e.getModifiers() != InputEvent.CTRL_MASK)) {
                    if (e.getKeyCode() == 38) {//UP
                        valueSelector.selectPrevious();
                        e.consume();
                    } else if (e.getKeyCode() == 40) { // DOWN
                        valueSelector.selectNext();
                        e.consume();
                    }
                }
            }
        }
    }
}
