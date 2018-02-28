package com.dci.intellij.dbn.data.record.ui;

import com.dci.intellij.dbn.common.locale.Formatter;
import com.dci.intellij.dbn.common.locale.options.RegionalSettings;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.data.record.DatasetRecord;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.object.DBColumn;
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

public class RecordViewerColumnForm extends DBNFormImpl<RecordViewerForm> {
    private JLabel columnLabel;
    private JPanel valueFieldPanel;
    private JLabel dataTypeLabel;
    private JPanel mainPanel;

    private JTextField valueTextField;

    private RegionalSettings regionalSettings;
    private DatasetRecord record;
    private DBColumn column;

    public RecordViewerColumnForm(RecordViewerForm parentForm, DatasetRecord record, DBColumn column) {
        super(parentForm);
        this.record = record;
        this.column = column;
        Project project = record.getDataset().getProject();

        DBDataType dataType = column.getDataType();
        regionalSettings = RegionalSettings.getInstance(project);

        columnLabel.setIcon(column.getIcon());
        columnLabel.setText(column.getName());
        dataTypeLabel.setText(dataType.getQualifiedName());
        dataTypeLabel.setForeground(UIUtil.getInactiveTextColor());

        valueTextField = new ColumnValueTextField(record, column);
        valueTextField.setPreferredSize(new Dimension(200, -1));
        valueTextField.addKeyListener(keyAdapter);

        valueFieldPanel.add(valueTextField, BorderLayout.CENTER);
        valueTextField.setEditable(false);
        valueTextField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        valueTextField.setBackground(UIUtil.getTextFieldBackground());
        updateColumnValue(column);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    private void updateColumnValue(DBColumn column) {
        Object value = record.getColumnValue(column);
        Formatter formatter = regionalSettings.getFormatter();
        if (value instanceof String) {
            String userValue = (String) value;
            if (userValue.indexOf('\n') > -1) {
                userValue = userValue.replace('\n', ' ');
            } else {
            }
            valueTextField.setText(userValue);
        } else {
            String formattedUserValue = formatter.formatObject(value);
            valueTextField.setText(formattedUserValue);
        }
    }

    public DBColumn getColumn() {
        return column;
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

/*    public Object getEditorValue() throws ParseException {
        DBDataType dataType = cell.getColumnInfo().getDataType();
        Class clazz = dataType.getTypeClass();
        String textValue = valueTextField.getText().trim();
        if (textValue.length() > 0) {
            Object value = getFormatter().parseObject(clazz, textValue);
            return dataType.getNativeDataType().getDataTypeDefinition().convert(value);
        } else {
            return null;
        }
    }*/

    private Formatter getFormatter() {
        Project project = record.getDataset().getProject();
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
                RecordViewerForm parentForm = getParentComponent();
                if (e.getKeyCode() == 38) {//UP
                    parentForm.focusPreviousColumnPanel(RecordViewerColumnForm.this);
                    e.consume();
                } else if (e.getKeyCode() == 40) { // DOWN
                    parentForm.focusNextColumnPanel(RecordViewerColumnForm.this);
                    e.consume();
                }
            }
        }
    };


    public void dispose() {
        super.dispose();
        regionalSettings = null;
        column = null;
    }


}
