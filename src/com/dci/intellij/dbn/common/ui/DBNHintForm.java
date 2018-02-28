package com.dci.intellij.dbn.common.ui;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;

import com.dci.intellij.dbn.common.Colors;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.intellij.util.ui.UIUtil;

public class DBNHintForm extends DBNFormImpl{
    private JPanel mainPanel;
    private JLabel hintLabel;
    private JTextPane hintTextPane;

    public DBNHintForm(String hintText) {
        this(hintText, null);
    }
    public DBNHintForm(String hintText, Icon hintIcon) {
        hintLabel.setText("");
        hintLabel.setIcon(CommonUtil.nvl(hintIcon, Icons.COMMON_INFO));
        hintTextPane.setBackground(UIUtil.getPanelBackground());
        hintTextPane.setText(hintText);
        hintTextPane.setFont(UIUtil.getLabelFont());
        hintTextPane.setForeground(Colors.HINT_COLOR);

    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }
}
