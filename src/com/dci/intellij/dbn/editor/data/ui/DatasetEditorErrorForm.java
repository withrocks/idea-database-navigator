package com.dci.intellij.dbn.editor.data.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.editor.data.DatasetEditorError;
import com.dci.intellij.dbn.editor.data.model.DatasetEditorModelCell;
import com.dci.intellij.dbn.editor.data.ui.table.DatasetEditorTable;
import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.JBColor;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;

public class DatasetEditorErrorForm extends DBNFormImpl implements ChangeListener {
    public static final Color BACKGROUND_COLOR = new JBColor(
            new Color(0xFFCCCC),
            new Color(0x743A3A));
    private JPanel mainPanel;
    private JLabel errorIconLabel;
    private JTextArea errorMessageTextArea;

    private DatasetEditorModelCell cell;
    private JBPopup popup;

    public DatasetEditorErrorForm(DatasetEditorModelCell cell) {
        this.cell = cell;
        DatasetEditorError error = cell.getError();
        error.addChangeListener(this);
        errorIconLabel.setIcon(Icons.EXEC_MESSAGES_ERROR);
        errorIconLabel.setText("");
        errorMessageTextArea.setText(StringUtil.wrap(error.getMessage(), 60, ": ,."));
        Color backgroundColor = BACKGROUND_COLOR;
        errorMessageTextArea.setBackground(backgroundColor);
        errorMessageTextArea.setFont(mainPanel.getFont());
        mainPanel.setBackground(backgroundColor);

        ComponentPopupBuilder popupBuilder = JBPopupFactory.getInstance().createComponentPopupBuilder(mainPanel, mainPanel);
        popup = popupBuilder.createPopup();
    }

    public void show() {
        DatasetEditorTable table = cell.getRow().getModel().getEditorTable();
        Rectangle rectangle = table.getCellRect(cell.getRow().getIndex(), cell.getIndex(), false);

        if (table.isShowing()) {
            Point tableLocation = table.getLocationOnScreen();
            int x = (int) (tableLocation.getX() + rectangle.getLocation().getX() + 4);
            int y = (int) (tableLocation.getY() + rectangle.getLocation().getY() + 20);
            Point cellLocation = new Point(x, y);
            popup.showInScreenCoordinates(table, cellLocation);
        }
    }

    public JBPopup getPopup() {
        return popup;
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void stateChanged(ChangeEvent e) {
        if (popup.isVisible()) popup.cancel();
    }

    public void dispose() {
        super.dispose();
        popup.dispose();
        popup = null;
        cell = null;
    }
}
