package com.dci.intellij.dbn.editor.data.record.ui;

import com.dci.intellij.dbn.common.locale.Formatter;
import com.dci.intellij.dbn.common.locale.options.RegionalSettings;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.data.editor.ui.BasicDataEditorComponent;
import com.dci.intellij.dbn.data.editor.ui.DataEditorComponent;
import com.dci.intellij.dbn.data.editor.ui.ListPopupValuesProvider;
import com.dci.intellij.dbn.data.editor.ui.ListPopupValuesProviderImpl;
import com.dci.intellij.dbn.data.editor.ui.TextFieldWithPopup;
import com.dci.intellij.dbn.data.editor.ui.TextFieldWithTextEditor;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.data.type.DBNativeDataType;
import com.dci.intellij.dbn.data.type.DataTypeDefinition;
import com.dci.intellij.dbn.data.type.GenericDataType;
import com.dci.intellij.dbn.data.value.LargeObjectValue;
import com.dci.intellij.dbn.editor.data.model.DatasetEditorColumnInfo;
import com.dci.intellij.dbn.editor.data.model.DatasetEditorModelCell;
import com.dci.intellij.dbn.editor.data.model.DatasetEditorModelRow;
import com.dci.intellij.dbn.editor.data.options.DataEditorSettings;
import com.dci.intellij.dbn.editor.data.options.DataEditorValueListPopupSettings;
import com.dci.intellij.dbn.object.DBColumn;
import com.intellij.openapi.project.Project;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.ui.UIUtil;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.ParseException;
import java.util.List;

public class DatasetRecordEditorColumnForm extends DBNFormImpl {
    private JLabel columnLabel;
    private JPanel valueFieldPanel;
    private JLabel dataTypeLabel;
    private JPanel mainPanel;

    private DatasetRecordEditorForm parentForm;
    private DatasetEditorModelCell cell;
    private DataEditorComponent editorComponent;

    private RegionalSettings regionalSettings;

    public DatasetRecordEditorColumnForm(DatasetRecordEditorForm parentForm, DatasetEditorModelCell cell) {
        this.parentForm = parentForm;
        final DatasetEditorColumnInfo columnInfo = cell.getColumnInfo();
        DBColumn column = columnInfo.getColumn();
        DBDataType dataType = column.getDataType();
        Project project = column.getProject();
        regionalSettings = RegionalSettings.getInstance(project);

        columnLabel.setIcon(column.getIcon());
        columnLabel.setText(column.getName());
        dataTypeLabel.setText(dataType.getQualifiedName());
        dataTypeLabel.setForeground(UIUtil.getInactiveTextColor());

        DBNativeDataType nativeDataType = dataType.getNativeDataType();
        if (nativeDataType != null) {
            DataTypeDefinition dataTypeDefinition = nativeDataType.getDataTypeDefinition();
            GenericDataType genericDataType = dataTypeDefinition.getGenericDataType();

            DataEditorSettings dataEditorSettings = DataEditorSettings.getInstance(project);

            long dataLength = dataType.getLength();

            if (genericDataType.is(GenericDataType.DATE_TIME, GenericDataType.LITERAL, GenericDataType.ARRAY)) {
                TextFieldWithPopup textFieldWithPopup = new TextFieldWithPopup(project);

                textFieldWithPopup.setPreferredSize(new Dimension(200, -1));
                JTextField valueTextField = textFieldWithPopup.getTextField();
                valueTextField.getDocument().addDocumentListener(documentListener);
                valueTextField.addKeyListener(keyAdapter);
                valueTextField.addFocusListener(focusListener);

                if (cell.getRow().getModel().isEditable()) {
                    switch (genericDataType) {
                        case DATE_TIME: textFieldWithPopup.createCalendarPopup(false); break;
                        case ARRAY: textFieldWithPopup.createArrayEditorPopup(false); break;
                        case LITERAL: {
                            DataEditorValueListPopupSettings valueListPopupSettings = dataEditorSettings.getValueListPopupSettings();

                            if (!column.isPrimaryKey() && !column.isUniqueKey() && dataLength <= valueListPopupSettings.getDataLengthThreshold()) {
                                ListPopupValuesProvider valuesProvider = new ListPopupValuesProviderImpl("Possible Values List", true) {
                                    @Override
                                    public List<String> getValues() {
                                        return columnInfo.getPossibleValues();
                                    }
                                };
                                textFieldWithPopup.createValuesListPopup(valuesProvider, valueListPopupSettings.isShowPopupButton());
                            }

                            if (dataLength > 20 && !column.isPrimaryKey() && !column.isForeignKey()) {
                                textFieldWithPopup.createTextEditorPopup(false);
                            }
                            break;
                        }
                    }

                }
                editorComponent = textFieldWithPopup;
            } else if (genericDataType.is(GenericDataType.BLOB, GenericDataType.CLOB)) {
                editorComponent = new TextFieldWithTextEditor(project);
            } else {
                editorComponent = new BasicDataEditorComponent();
            }
        } else {
            editorComponent = new BasicDataEditorComponent();
            editorComponent.setEnabled(false);
            editorComponent.setEditable(false);
        }

        valueFieldPanel.add((Component) editorComponent, BorderLayout.CENTER);
        editorComponent.getTextField().setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        setCell(cell);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void setCell(DatasetEditorModelCell cell) {
        if (this.cell != null) updateUserValue(false);
        this.cell = cell;

        DatasetEditorModelRow row = cell.getRow();
        boolean editable = !row.isDeleted() && row.getModel().isEditable();
        editorComponent.setEnabled(editable);
        editorComponent.setUserValueHolder(cell);

        Formatter formatter = regionalSettings.getFormatter();
        if (cell.getUserValue() instanceof String) {
            String userValue = (String) cell.getUserValue();
            if (userValue.indexOf('\n') > -1) {
                userValue = userValue.replace('\n', ' ');
                editorComponent.setEditable(false);
            } else {
                editorComponent.setEditable(editable);
            }
            editorComponent.setText(userValue);
        } else {
            editable = editable && !(cell.getUserValue() instanceof LargeObjectValue);
            editorComponent.setEditable(editable);
            String formattedUserValue = formatter.formatObject(cell.getUserValue());
            editorComponent.setText(formattedUserValue);
        }
        JTextField valueTextField = editorComponent.getTextField();
        valueTextField.setBackground(UIUtil.getTextFieldBackground());
    }

    public DatasetEditorModelCell getCell() {
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

    public JComponent getEditorComponent() {
        return editorComponent.getTextField();
    }


    public Object getEditorValue() throws ParseException {
        DBDataType dataType = cell.getColumnInfo().getDataType();
        Class clazz = dataType.getTypeClass();
        String textValue = editorComponent.getText().trim();
        if (textValue.length() > 0) {
            Object value = getFormatter().parseObject(clazz, textValue);
            DBNativeDataType nativeDataType = dataType.getNativeDataType();
            return nativeDataType == null ? null : nativeDataType.getDataTypeDefinition().convert(value);
        } else {
            return null;
        }
    }

    private void updateUserValue(boolean highlightError) {
        if (editorComponent != null) {
            JTextField valueTextField = editorComponent.getTextField();
            if (valueTextField.isEditable())  {
                try {
                    Object value = getEditorValue();
                    editorComponent.getUserValueHolder().updateUserValue(value, false);
                    valueTextField.setForeground(UIUtil.getTextFieldForeground());
                } catch (ParseException e1) {
                    if (highlightError) {
                        valueTextField.setForeground(Color.RED);
                    }

                    //DBDataType dataType = cell.getColumnInfo().getDataType();
                    //MessageUtil.showErrorDialog("Can not convert " + valueTextField.getText() + " to " + dataType.getName());
                }
            }
        }
    }

    private Formatter getFormatter() {
        Project project = cell.getRow().getModel().getDataset().getProject();
        return Formatter.getInstance(project);
    }

    /*********************************************************
     *                     Listeners                         *
     *********************************************************/
    DocumentListener documentListener = new DocumentAdapter() {
        @Override
        protected void textChanged(DocumentEvent documentEvent) {
            JTextField valueTextField = editorComponent.getTextField();
            valueTextField.setForeground(UIUtil.getTextFieldForeground());
        }
    };

    KeyListener keyAdapter = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            if (!e.isConsumed()) {
                if (e.getKeyCode() == 38) {//UP
                    parentForm.focusPreviousColumnPanel(DatasetRecordEditorColumnForm.this);
                    e.consume();
                } else if (e.getKeyCode() == 40) { // DOWN
                    parentForm.focusNextColumnPanel(DatasetRecordEditorColumnForm.this);
                    e.consume();
                }
            }
        }
    };


    boolean isError = false;
    FocusListener focusListener = new FocusAdapter() {
        @Override
        public void focusGained(FocusEvent e) {
            if (e.getOppositeComponent() != null) {
                JTextField valueTextField = editorComponent.getTextField();
                DataEditorSettings settings = cell.getRow().getModel().getSettings();
                if (settings.getGeneralSettings().getSelectContentOnCellEdit().value()) {
                    valueTextField.selectAll();
                }

                Rectangle rectangle = new Rectangle(mainPanel.getLocation(), mainPanel.getSize());
                parentForm.getColumnsPanel().scrollRectToVisible(rectangle);
            }
        }

        @Override
        public void focusLost(FocusEvent e) {
            updateUserValue(true);
        }
    };

    public void dispose() {
        super.dispose();
        regionalSettings = null;
        parentForm = null;
        cell = null;
        editorComponent = null;


    }
}
