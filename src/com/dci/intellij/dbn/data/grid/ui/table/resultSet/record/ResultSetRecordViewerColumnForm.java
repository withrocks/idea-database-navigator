package com.dci.intellij.dbn.data.grid.ui.table.resultSet.record;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.locale.Formatter;
import com.dci.intellij.dbn.common.locale.options.RegionalSettings;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.data.model.ColumnInfo;
import com.dci.intellij.dbn.data.model.resultSet.ResultSetDataModelCell;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.UIUtil;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.ParseException;

public class ResultSetRecordViewerColumnForm extends DBNFormImpl {
    private JLabel columnLabel;
    private JPanel valueFieldPanel;
    private JLabel dataTypeLabel;
    private JPanel mainPanel;

    private JTextField valueTextField;
    private ResultSetRecordViewerForm parentForm;
    private ResultSetDataModelCell cell;

    private RegionalSettings regionalSettings;

    public ResultSetRecordViewerColumnForm(ResultSetRecordViewerForm parentForm, ResultSetDataModelCell cell, boolean showDataType) {
        this.parentForm = parentForm;
        Project project = cell.getRow().getModel().getProject();
        ColumnInfo columnInfo = cell.getColumnInfo();

        DBDataType dataType = columnInfo.getDataType();
        regionalSettings = RegionalSettings.getInstance(project);

        columnLabel.setIcon(Icons.DBO_COLUMN);
        columnLabel.setText(columnInfo.getName());
        if (showDataType) {
            dataTypeLabel.setText(dataType.getQualifiedName());
            dataTypeLabel.setForeground(UIUtil.getInactiveTextColor());
        } else {
            dataTypeLabel.setVisible(showDataType);
        }

        valueTextField = new JTextField();
        valueTextField.setPreferredSize(new Dimension(200, -1));
        valueTextField.addKeyListener(keyAdapter);

        valueFieldPanel.add(valueTextField, BorderLayout.CENTER);
        valueTextField.setEditable(false);
        valueTextField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        valueTextField.setBackground(UIUtil.getTextFieldBackground());
        setCell(cell);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void setCell(ResultSetDataModelCell cell) {
        this.cell = cell;

        Formatter formatter = regionalSettings.getFormatter();
        if (cell.getUserValue() instanceof String) {
            String userValue = (String) cell.getUserValue();
            if (userValue.indexOf('\n') > -1) {
                userValue = userValue.replace('\n', ' ');
            } else {
            }
            valueTextField.setText(userValue);
        } else {
            String formattedUserValue = formatter.formatObject(cell.getUserValue());
            valueTextField.setText(formattedUserValue);
        }
    }

    public ResultSetDataModelCell getCell() {
        return cell;
    }

    protected int[] getMetrics(int[] metrics) {
        return new int[] {
            (int) Math.max(metrics[0], columnLabel.getPreferredSize().getWidth()),
            (int) Math.max(metrics[1], valueFieldPanel.getPreferredSize().getWidth())};
    }

    protected void adjustMetrics(int[] metrics) {
        columnLabel.setPreferredSize(new Dimension(metrics[0], columnLabel.getHeight()));
        valueFieldPanel.setPreferredSize(new Dimension(metrics[1], valueFieldPanel.getHeight()));
    }

    public Object getEditorValue() throws ParseException {
        DBDataType dataType = cell.getColumnInfo().getDataType();
        Class clazz = dataType.getTypeClass();
        String textValue = valueTextField.getText().trim();
        if (textValue.length() > 0) {
            Object value = getFormatter().parseObject(clazz, textValue);
            return dataType.getNativeDataType().getDataTypeDefinition().convert(value);
        } else {
            return null;
        }
    }

    private Formatter getFormatter() {
        Project project = cell.getRow().getModel().getProject();
        return Formatter.getInstance(project);
    }


    public JTextField getViewComponent() {
        return valueTextField;
    }

    /*********************************************************
     *                     Listeners                         *
     *********************************************************/
    KeyListener keyAdapter = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            if (!e.isConsumed()) {
                if (e.getKeyCode() == 38) {//UP
                    parentForm.focusPreviousColumnPanel(ResultSetRecordViewerColumnForm.this);
                    e.consume();
                } else if (e.getKeyCode() == 40) { // DOWN
                    parentForm.focusNextColumnPanel(ResultSetRecordViewerColumnForm.this);
                    e.consume();
                }
            }
        }
    };


    public void dispose() {
        super.dispose();
        regionalSettings = null;
        parentForm = null;
        cell = null;

    }


}
