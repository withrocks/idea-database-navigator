package com.dci.intellij.dbn.data.grid.ui.table.basic;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;

import com.dci.intellij.dbn.common.ui.table.DBNTable;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.EditorFontType;
import com.intellij.ui.border.CustomLineBorder;
import com.intellij.util.ui.UIUtil;

public class BasicTableGutterCellRenderer extends JPanel implements ListCellRenderer {

    static EditorColorsScheme getGlobalScheme() {
        return EditorColorsManager.getInstance().getGlobalScheme();
    }

    private static final Border BORDER = new CompoundBorder(new CustomLineBorder(UIUtil.getPanelBackground(), 0, 0, 1, 1), new EmptyBorder(0, 3, 0, 3));
    private JLabel textLabel;

    public BasicTableGutterCellRenderer() {
        setBackground(UIUtil.getPanelBackground());
        setBorder(BORDER);
        setLayout(new BorderLayout());
        textLabel = new JLabel();
        textLabel.setForeground(BasicTableColors.getLineNumberColor());
        textLabel.setFont(EditorColorsManager.getInstance().getGlobalScheme().getFont(EditorFontType.PLAIN));
        add(textLabel, BorderLayout.EAST);
    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);
        if (textLabel != null) textLabel.setFont(font);
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        BasicTableGutter tableGutter = (BasicTableGutter) list;
        textLabel.setText(Integer.toString(index + 1));
        DBNTable table = tableGutter.getTable();
        boolean isCaretRow = table.getCellSelectionEnabled() && table.getSelectedRow() == index && table.getSelectedRowCount() == 1;

        setBackground(isSelected ?
                BasicTableColors.getSelectionBackgroundColor() :
                isCaretRow ?
                        BasicTableColors.getCaretRowColor() :
                        UIUtil.getPanelBackground());
        textLabel.setForeground(isSelected ? BasicTableColors.getSelectionBackgroundColor() : BasicTableColors.getLineNumberColor());
        return this;
    }
}
