package com.dci.intellij.dbn.editor.data.ui.table.renderer;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.data.grid.ui.table.basic.BasicTableColors;
import com.dci.intellij.dbn.data.grid.ui.table.basic.BasicTableGutter;
import com.dci.intellij.dbn.editor.data.model.DatasetEditorModel;
import com.dci.intellij.dbn.editor.data.model.DatasetEditorModelRow;
import com.dci.intellij.dbn.editor.data.ui.table.DatasetEditorTable;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorFontType;
import com.intellij.ui.border.CustomLineBorder;
import com.intellij.util.ui.UIUtil;

public class DatasetEditorTableGutterRenderer extends JPanel implements ListCellRenderer {
    private JLabel textLabel;
    private JLabel imageLabel;
    private JPanel textPanel;
    private static final Cursor HAND_CURSOR = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    boolean isDarkScheme = UIUtil.isUnderDarcula();

    private Border border = new CompoundBorder(new CustomLineBorder(UIUtil.getPanelBackground(), 0, 0, 1, 1), new EmptyBorder(0, 3, 0, 3));

    public DatasetEditorTableGutterRenderer() {
        setBackground(UIUtil.getPanelBackground());
        setBorder(border);
        setLayout(new BorderLayout());
        textLabel = new JLabel();
        textLabel.setFont(EditorColorsManager.getInstance().getGlobalScheme().getFont(EditorFontType.PLAIN));
        imageLabel = new JLabel();

        textPanel = new JPanel(new BorderLayout());
        textPanel.setBorder(new EmptyBorder(0,0,0,3));
        textPanel.add(textLabel, BorderLayout.EAST);
        add(textPanel, BorderLayout.CENTER);
        add(imageLabel, BorderLayout.EAST);
        textLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
        textLabel.setCursor(HAND_CURSOR);
    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);
        if (textLabel != null) textLabel.setFont(font);
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        BasicTableGutter tableGutter = (BasicTableGutter) list;
        DatasetEditorModel model = (DatasetEditorModel) list.getModel();
        DatasetEditorModelRow row = model.getRowAtIndex(index);
        DatasetEditorTable table = (DatasetEditorTable) tableGutter.getTable();
        if (row != null) {
            Icon icon =
                    row.isNew() ? Icons.DATA_EDITOR_ROW_NEW :
                            row.isInsert() ? Icons.DATA_EDITOR_ROW_INSERT :
                                    row.isDeleted() ? Icons.DATA_EDITOR_ROW_DELETED :
                                            row.isModified() ? Icons.DATA_EDITOR_ROW_MODIFIED :
                                                    table.getModel().isModified() ? Icons.DATA_EDITOR_ROW_DEFAULT : null;

            textLabel.setText(Integer.toString(row.getIndex() + 1));
            if (imageLabel.getIcon() != icon) {
                imageLabel.setIcon(icon);
            }
        }
        //lText.setFont(isSelected ? BOLD_FONT : REGULAR_FONT);

        boolean isCaretRow = table.getCellSelectionEnabled() && table.getSelectedRow() == index && table.getSelectedRowCount() == 1;
        Color background = isSelected ?
                BasicTableColors.getSelectionBackgroundColor() :
                    isCaretRow ?
                        BasicTableColors.getCaretRowColor() :
                        UIUtil.getPanelBackground();
        setBackground(background);
        textPanel.setBackground(background);
        textLabel.setForeground(isSelected ?
                BasicTableColors.getSelectionForegroundColor() :
                BasicTableColors.getLineNumberColor());
        return this;
    }
}
