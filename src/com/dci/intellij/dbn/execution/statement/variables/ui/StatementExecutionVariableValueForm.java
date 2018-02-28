package com.dci.intellij.dbn.execution.statement.variables.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.ComboBoxSelectionKeyListener;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.data.editor.ui.ListPopupValuesProvider;
import com.dci.intellij.dbn.data.editor.ui.TextFieldPopupType;
import com.dci.intellij.dbn.data.editor.ui.TextFieldWithPopup;
import com.dci.intellij.dbn.data.type.GenericDataType;
import com.dci.intellij.dbn.execution.statement.StatementExecutionManager;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.dci.intellij.dbn.execution.statement.variables.StatementExecutionVariable;
import com.dci.intellij.dbn.execution.statement.variables.StatementExecutionVariablesCache;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.ui.UIUtil;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;


public class StatementExecutionVariableValueForm extends DBNFormImpl<StatementExecutionVariablesForm> {
    private JPanel mainPanel;
    private JLabel variableNameLabel;
    private JComboBox dataTypeComboBox;
    private JPanel valueFieldPanel;
    private JLabel errorLabel;

    private StatementExecutionVariable variable;
    private TextFieldWithPopup editorComponent;

    public StatementExecutionVariableValueForm(StatementExecutionVariablesForm parentComponent, final StatementExecutionVariable variable) {
        super(parentComponent);
        this.variable = variable;
        errorLabel.setVisible(false);
        errorLabel.setIcon(Icons.STMT_EXECUTION_ERROR);

        variableNameLabel.setText(variable.getName().substring(1).trim());
        variableNameLabel.setIcon(Icons.DBO_VARIABLE);

        dataTypeComboBox.addItem(GenericDataType.LITERAL);
        dataTypeComboBox.addItem(GenericDataType.NUMERIC);
        dataTypeComboBox.addItem(GenericDataType.DATE_TIME);
        dataTypeComboBox.setRenderer(new DataTypeCellRenderer());
        dataTypeComboBox.setSelectedItem(variable.getDataType());

        StatementExecutionProcessor executionProcessor = parentComponent.getExecutionProcessor();
        StatementExecutionManager executionManager = StatementExecutionManager.getInstance(executionProcessor.getProject());
        final StatementExecutionVariablesCache variablesCache = executionManager.getVariablesCache();
        final VirtualFile virtualFile = executionProcessor.getVirtualFile();

        editorComponent = new TextFieldWithPopup(executionProcessor.getProject());
        editorComponent.createCalendarPopup(false);
        editorComponent.createValuesListPopup(new ListPopupValuesProvider() {
            @Override
            public String getDescription() {
                return "History Values List";
            }

            @Override
            public List<String> getValues() {
                List<String> values = new ArrayList<String>();
                final Set<StatementExecutionVariable> variables = variablesCache.getVariables(virtualFile);
                for (StatementExecutionVariable executionVariable : variables) {
                    if (executionVariable.getName().equals(variable.getName())) {
                        Iterable<String> valueHistory = executionVariable.getValueHistory();
                        for (String value : valueHistory) {
                            values.add(value);
                        }
                    }
                }

                return values;
            }

            @Override
            public List<String> getSecondaryValues() {
                return Collections.emptyList();
            }

            @Override
            public boolean isLongLoading() {
                return false;
            }
        }, true);
        editorComponent.setPopupEnabled(TextFieldPopupType.CALENDAR, variable.getDataType() == GenericDataType.DATE_TIME);
        valueFieldPanel.add(editorComponent, BorderLayout.CENTER);
        final JTextField textField = editorComponent.getTextField();
        String value = variable.getValue();
        if (StringUtil.isEmpty(value)) {
            StatementExecutionVariable cachedVariable = variablesCache.getVariable(virtualFile, variable.getName());
            if (cachedVariable != null) {
                textField.setForeground(UIUtil.getLabelDisabledForeground());
                textField.setText(cachedVariable.getValue());
                textField.getDocument().addDocumentListener(new DocumentAdapter() {
                    @Override
                    protected void textChanged(DocumentEvent documentEvent) {
                        textField.setForeground(UIUtil.getTextFieldForeground());
                    }
                });
            }
        } else {
            textField.setText(value);
        }


        textField.addKeyListener(ComboBoxSelectionKeyListener.create(dataTypeComboBox, false));

        variable.setPreviewValueProvider(new StatementExecutionVariable.TemporaryValueProvider() {
            public String getValue() {
                return textField.getText().trim();
            }

            public GenericDataType getDataType() {
                return (GenericDataType) dataTypeComboBox.getSelectedItem();
            }
        });

        dataTypeComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editorComponent.setPopupEnabled(TextFieldPopupType.CALENDAR, dataTypeComboBox.getSelectedItem() == GenericDataType.DATE_TIME);
            }
        });

        textField.setToolTipText("<html>While editing variable value, press <b>Up/Down</b> keys to change data type");

        Disposer.register(this, editorComponent);
    }

    public void showErrorLabel(String errorText) {
        errorLabel.setVisible(true);
        errorLabel.setText(errorText);
    }
    
    public void hideErrorLabel(){
        errorLabel.setVisible(false);
        errorLabel.setText(null);
    }

    public StatementExecutionVariable getVariable() {
        return variable;
    }

    public void saveValue() {
        variable.setValue(editorComponent.getTextField().getText().trim());
        variable.setDataType((GenericDataType) dataTypeComboBox.getSelectedItem());
        StatementExecutionProcessor executionProcessor = getParentComponent().getExecutionProcessor();
        Project project = executionProcessor.getProject();
        StatementExecutionManager executionManager = StatementExecutionManager.getInstance(project);
        executionManager.cacheVariable(executionProcessor.getVirtualFile(), variable);
    }

    public void addDocumentListener(DocumentListener documentListener) {
        editorComponent.getTextField().getDocument().addDocumentListener(documentListener);        
    }

    public void addActionListener(ActionListener actionListener) {
        dataTypeComboBox.addActionListener(actionListener);
    }

    private class DataTypeCellRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            GenericDataType dataType = (GenericDataType) value;
            label.setText(" " + dataType.getName() + " ");
            return label;
        }
    }

    protected int[] getMetrics(int[] metrics) {
        return new int[] {
            (int) Math.max(metrics[0], variableNameLabel.getPreferredSize().getWidth()),
            (int) Math.max(metrics[1], valueFieldPanel.getPreferredSize().getWidth())};
    }

    protected void adjustMetrics(int[] metrics) {
        variableNameLabel.setPreferredSize(new Dimension(metrics[0], variableNameLabel.getHeight()));
        valueFieldPanel.setPreferredSize(new Dimension(metrics[1], valueFieldPanel.getHeight()));
    }


    public JComponent getComponent() {
        return mainPanel;
    }

    public void dispose() {
        super.dispose();
        variable = null;
        editorComponent = null;
    }

    public JComponent getEditorComponent() {
        return editorComponent.getTextField();
    }
}
