package com.dci.intellij.dbn.editor.data.ui;

import com.intellij.codeInsight.hint.HintUtil;
import com.intellij.icons.AllIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

public class DatasetLoadErrorNotificationPanel extends JPanel{
    protected final JLabel label = new JLabel();

    public DatasetLoadErrorNotificationPanel() {
        super(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(1, 15, 1, 15));

        setPreferredSize(new Dimension(-1, 24));

        add(label, BorderLayout.CENTER);
        label.setIcon(AllIcons.General.Error);
    }

    public void setText(String text) {
        label.setText(text);
    }

    public DatasetLoadErrorNotificationPanel text(@NotNull String text) {
        label.setText(text);
        return this;
    }

    public DatasetLoadErrorNotificationPanel icon(@NotNull Icon icon) {
        label.setIcon(icon);
        return this;
    }

    @Override
    public Color getBackground() {
        return HintUtil.ERROR_COLOR;
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(0, 0);
    }
}
