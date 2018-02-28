package com.dci.intellij.dbn.common.ui;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

public class ComboBoxUtil {
    public static void addItems(JComboBox comboBox, Iterable items) {
        for (Object item : items) {
            comboBox.addItem(item);
        }
    }

    public static void addItems(DefaultComboBoxModel comboBox, Iterable items) {
        for (Object item : items) {
            comboBox.addElement(item);
        }
    }


}
