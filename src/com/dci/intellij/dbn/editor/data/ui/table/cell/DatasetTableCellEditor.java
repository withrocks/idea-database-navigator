 package com.dci.intellij.dbn.editor.data.ui.table.cell;

 import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
 import com.dci.intellij.dbn.common.ui.MouseUtil;
 import com.dci.intellij.dbn.data.editor.ui.BasicDataEditorComponent;
 import com.dci.intellij.dbn.data.editor.ui.DataEditorComponent;
 import com.dci.intellij.dbn.data.model.ColumnInfo;
 import com.dci.intellij.dbn.data.type.DBDataType;
 import com.dci.intellij.dbn.data.type.GenericDataType;
 import com.dci.intellij.dbn.editor.data.DatasetEditorManager;
 import com.dci.intellij.dbn.editor.data.filter.DatasetFilterInput;
 import com.dci.intellij.dbn.editor.data.model.DatasetEditorModelCell;
 import com.dci.intellij.dbn.editor.data.ui.table.DatasetEditorTable;
 import com.dci.intellij.dbn.object.DBColumn;
 import com.intellij.ui.JBColor;
 import com.intellij.ui.SimpleTextAttributes;

 import javax.swing.JTextField;
 import javax.swing.border.Border;
 import javax.swing.border.CompoundBorder;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.LineBorder;
 import javax.swing.text.Document;
 import java.awt.Cursor;
 import java.awt.MouseInfo;
 import java.awt.Point;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionAdapter;
 import java.awt.event.MouseMotionListener;

 public class DatasetTableCellEditor extends AbstractDatasetTableCellEditor implements KeyListener{
    public static final Border EMPTY_BORDER = new EmptyBorder(0, 3, 0, 3);
    private static final Border ERROR_BORDER = new CompoundBorder(new LineBorder(JBColor.RED, 1), new EmptyBorder(0, 2, 0, 2));
    private static final Border POPUP_BORDER = new CompoundBorder(new LineBorder(JBColor.BLUE, 1), new EmptyBorder(0, 2, 0, 2));

    public static final int HIGHLIGHT_TYPE_NONE = 0;
    public static final int HIGHLIGHT_TYPE_POPUP = 1;
    public static final int HIGHLIGHT_TYPE_ERROR = 2;

    public DatasetTableCellEditor(DatasetEditorTable table) {
        this(table, new BasicDataEditorComponent());
        SimpleTextAttributes selectionTextAttributes = table.getCellRenderer().getAttributes().getSelection();

        JTextField textField = getTextField();
        textField.setSelectionColor(selectionTextAttributes.getBgColor());
        textField.setSelectedTextColor(selectionTextAttributes.getFgColor());
        textField.setFont(table.getFont());
    }

    public DatasetTableCellEditor(DatasetEditorTable table, DataEditorComponent editorComponent) {
        super(table, editorComponent);
        JTextField textField = getTextField();
        textField.addKeyListener(this);
        textField.addMouseListener(mouseListener);
        textField.addMouseMotionListener(mouseMotionListener);

        SimpleTextAttributes selectionTextAttributes = table.getCellRenderer().getAttributes().getSelection();
        textField.setSelectionColor(selectionTextAttributes.getBgColor());
        textField.setSelectedTextColor(selectionTextAttributes.getFgColor());
        textField.setFont(table.getFont());
    }

    public void prepareEditor(DatasetEditorModelCell cell) {
        setCell(cell);
        ColumnInfo columnInfo = cell.getColumnInfo();
        DBDataType dataType = columnInfo.getDataType();
        if (dataType.isNative()) {
            GenericDataType genericDataType = dataType.getGenericDataType();
            highlight(cell.hasError() ? HIGHLIGHT_TYPE_ERROR : HIGHLIGHT_TYPE_NONE);
            Object userValue = cell.getUserValue();
            if (genericDataType == GenericDataType.LITERAL) {
                String value = (String) userValue;
                setEditable(value == null || value.indexOf('\n') == -1);
            } else if (genericDataType.is(GenericDataType.DATE_TIME, GenericDataType.NUMERIC)) {
                setEditable(true);                    
            } else {
                setEditable(false);
            }
            JTextField textField = getTextField();
            selectText(textField);
        }
    }

    public void setEditable(boolean editable) {
        getTextField().setEditable(editable);
    }

    public void highlight(int type) {
        switch (type) {
            case HIGHLIGHT_TYPE_NONE: getEditorComponent().setBorder(EMPTY_BORDER); break;
            case HIGHLIGHT_TYPE_POPUP: getEditorComponent().setBorder(POPUP_BORDER); break;
            case HIGHLIGHT_TYPE_ERROR: getEditorComponent().setBorder(ERROR_BORDER); break;
        }
    }

    public boolean isEditable() {
        return getTextField().isEditable();
    }

    protected void selectText(final JTextField textField) {

        if (textField.isEditable()) {
            final String originalText = textField.getText();
            new SimpleLaterInvocator() {
                public void execute() {
                    if (!isDisposed()) {
                        // select all only if the text didn't change
                        if (settings.getGeneralSettings().getSelectContentOnCellEdit().value()) {
                            if (originalText.equals(textField.getText())) {
                                textField.grabFocus();
                                textField.selectAll();
                            }
                        } else {
                            textField.requestFocus();

                            Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
                            Point textFieldLocation = textField.getLocationOnScreen();
                            int x = (int) Math.max(mouseLocation.getX() - textFieldLocation.getX(), 0);
                            int y = (int) Math.min(Math.max(mouseLocation.getY() - textFieldLocation.getY(), 0), 10);

                            Point location = new Point(x, y);
                            int position = textField.viewToModel(location);
                            textField.setCaretPosition(position);
                        }
                    }
                }
            }.start();
        }
    }

    protected boolean isSelected() {
        JTextField textField = getTextField();
        Document document = textField.getDocument();
        return document.getLength() > 0 && textField.getSelectionStart() == 0 && textField.getSelectionEnd() == document.getLength();
    }

    @Override
    public Object getCellEditorValue() {
        return super.getCellEditorValue();
/*
        String stringValue = (String) super.getCellEditorValue();
        return stringValue != null &&
                stringValue.trim().length() > 0 ? stringValue : null;
*/
    }

    /********************************************************
     *                      KeyListener                     *
     ********************************************************/
    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {}



    public void keyPressed(KeyEvent e) {
        if (!e.isConsumed()) {
            JTextField textField = getTextField();

            int caretPosition = textField.getCaretPosition();
            if (e.getKeyCode() == 37 ) { // LEFT
                if (isSelected()) {
                    textField.setCaretPosition(0);
                } else  if (caretPosition == 0) {
                    e.consume();
                    getCell().editPrevious();
                }
            }
            else if (e.getKeyCode() == 39 ) { // RIGHT
                if (!isSelected() && caretPosition == textField.getDocument().getLength()) {
                    e.consume();
                    getCell().editNext();
                }
            }
            else if (e.getKeyCode() == 27 ) { // ESC
                e.consume();
                getTable().cancelEditing();
            }
        }
    }

    /********************************************************
     *                    MouseListener                     *
     ********************************************************/
    private MouseMotionListener mouseMotionListener = new MouseMotionAdapter() {
        @Override
        public void mouseMoved(MouseEvent e) {
            JTextField textField = getTextField();
            DatasetEditorModelCell cell = getCell();
            if (e.isControlDown() && cell.isNavigable()) {
                DBColumn column = cell.getColumnInfo().getColumn();
                if (column != null) {
                    DBColumn foreignKeyColumn = column.getForeignKeyColumn();
                    if (foreignKeyColumn != null) {
                        textField.setToolTipText("<html>Show referenced <b>" + foreignKeyColumn.getDataset().getQualifiedName() + "</b> record<html>");
                        textField.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    }
                }
            } else {
                textField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
                textField.setToolTipText(null);
            }
        }
    };

    private MouseListener mouseListener = new MouseAdapter() {
        public void mouseReleased(MouseEvent event) {
            if (event.getButton() == MouseEvent.BUTTON3 ) {
                DatasetEditorModelCell cell = getCell();
                if (cell != null) {
                    getTable().showPopupMenu(event, cell, cell.getColumnInfo());
                }
            }
        }

        public void mouseClicked(MouseEvent event) {
            if (MouseUtil.isNavigationEvent(event)) {
                DatasetEditorModelCell cell = getCell();
                if (cell.isNavigable()) {
                    DatasetEditorTable table = getTable();
                    DatasetFilterInput filterInput = table.getModel().resolveForeignKeyRecord(cell);
                    DatasetEditorManager datasetEditorManager = DatasetEditorManager.getInstance(table.getProject());
                    datasetEditorManager.navigateToRecord(filterInput, event);
                    event.consume();
                }
            }
        }
    };

     /********************************************************
      *                    Disposable                        *
      ********************************************************/
     @Override
     public void dispose() {
         if (!isDisposed()) {
             super.dispose();
         }
     }
 }
