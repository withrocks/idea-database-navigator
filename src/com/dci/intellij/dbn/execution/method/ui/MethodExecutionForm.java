package com.dci.intellij.dbn.execution.method.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.DisposableProjectComponent;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.ui.AutoCommitLabel;
import com.dci.intellij.dbn.common.ui.Borders;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.common.ui.ValueSelector;
import com.dci.intellij.dbn.common.ui.ValueSelectorListener;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.object.DBArgument;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBSchema;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.DocumentAdapter;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MethodExecutionForm extends DBNFormImpl<DisposableProjectComponent> {
    private JPanel mainPanel;
    private JPanel argumentsPanel;
    private JPanel headerPanel;
    private JPanel executionSchemaActionPanel;
    private JLabel executionSchemaLabel;
    private JLabel noArgumentsLabel;
    private JCheckBox usePoolConnectionCheckBox;
    private JCheckBox commitCheckBox;
    private JLabel connectionLabel;
    private JScrollPane argumentsScrollPane;
    private AutoCommitLabel autoCommitLabel;
    private JCheckBox enableLoggingCheckBox;


    private List<MethodExecutionArgumentForm> argumentForms = new ArrayList<MethodExecutionArgumentForm>();
    private MethodExecutionInput executionInput;
    private Set<ChangeListener> changeListeners = new HashSet<ChangeListener>();
    private boolean debug;

    public MethodExecutionForm(DisposableProjectComponent parentComponent, MethodExecutionInput executionInput, boolean showHeader, boolean debug) {
        super(parentComponent);
        this.executionInput = executionInput;
        this.debug = debug;
        DBMethod method = executionInput.getMethod();

        ConnectionHandler connectionHandler = FailsafeUtil.get(executionInput.getConnectionHandler());
        if (DatabaseFeature.AUTHID_METHOD_EXECUTION.isSupported(connectionHandler)) {
            //ActionToolbar actionToolbar = ActionUtil.createActionToolbar("", true, new SetExecutionSchemaComboBoxAction(executionInput));
            executionSchemaActionPanel.add(new SchemaSelector(), BorderLayout.CENTER);
        } else {
            executionSchemaActionPanel.setVisible(false);
            executionSchemaLabel.setVisible(false);
        }
        connectionLabel.setText(connectionHandler.getPresentableText());
        connectionLabel.setIcon(connectionHandler.getIcon());
        autoCommitLabel.setConnectionHandler(connectionHandler);

        //objectPanel.add(new ObjectDetailsPanel(method).getComponent(), BorderLayout.NORTH);

        if (showHeader) {
            DBNHeaderForm headerForm = new DBNHeaderForm(method);
            headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);
        }
        headerPanel.setVisible(showHeader);

        argumentsPanel.setLayout(new BoxLayout(argumentsPanel, BoxLayout.Y_AXIS));
        int[] metrics = new int[]{0, 0};

        //topSeparator.setVisible(false);
        List<DBArgument> arguments = new ArrayList<DBArgument>(method.getArguments());
        noArgumentsLabel.setVisible(arguments.size() == 0);
        for (DBArgument argument: arguments) {
            if (argument.isInput()) {
                metrics = addArgumentPanel(argument, metrics);
                argumentsScrollPane.setBorder(Borders.BOTTOM_LINE_BORDER);
                //topSeparator.setVisible(true);
            }
        }

        for (MethodExecutionArgumentForm component : argumentForms) {
            component.adjustMetrics(metrics);
        }

        if (argumentForms.size() > 0) {
            argumentsScrollPane.getVerticalScrollBar().setUnitIncrement(argumentForms.get(0).getScrollUnitIncrement());
        }


        Dimension preferredSize = mainPanel.getPreferredSize();
        int width = (int) preferredSize.getWidth() + 24;
        int height = (int) Math.min(preferredSize.getHeight(), 380);
        mainPanel.setPreferredSize(new Dimension(width, height));
        commitCheckBox.setSelected(executionInput.isCommitAfterExecution());
        commitCheckBox.setEnabled(!connectionHandler.isAutoCommit());
        usePoolConnectionCheckBox.setSelected(executionInput.isUsePoolConnection());

        for (MethodExecutionArgumentForm argumentComponent : argumentForms){
            argumentComponent.addDocumentListener(documentListener);
        }
        commitCheckBox.addActionListener(actionListener);
        usePoolConnectionCheckBox.addActionListener(actionListener);
        usePoolConnectionCheckBox.setEnabled(!debug);

        enableLoggingCheckBox.setEnabled(!debug);
        enableLoggingCheckBox.setSelected(!debug && executionInput.isEnableLogging());
        enableLoggingCheckBox.setVisible(DatabaseFeature.DATABASE_LOGGING.isSupported(connectionHandler));
        DatabaseCompatibilityInterface compatibilityInterface = DatabaseCompatibilityInterface.getInstance(connectionHandler);
        String databaseLogName = compatibilityInterface == null ? null : compatibilityInterface.getDatabaseLogName();
        if (StringUtil.isNotEmpty(databaseLogName)) {
            enableLoggingCheckBox.setText("Enable logging (" + databaseLogName + ")");
        }

        Disposer.register(this, autoCommitLabel);
    }

    private class SchemaSelector extends ValueSelector<DBSchema> {
        public SchemaSelector() {
            super(Icons.DBO_SCHEMA, "Select Schema...", executionInput.getExecutionSchema(), true);
            addListener(new ValueSelectorListener<DBSchema>() {
                @Override
                public void valueSelected(DBSchema schema) {
                    executionInput.setExecutionSchema(schema);
                    notifyChangeListeners();
                }
            });
        }

        @Override
        public List<DBSchema> loadValues() {
            ConnectionHandler connectionHandler = FailsafeUtil.get(executionInput.getConnectionHandler());
            return connectionHandler.getObjectBundle().getSchemas();
        }
    }

    public void setExecutionInput(MethodExecutionInput executionInput) {
        if (!executionInput.equals(this.executionInput)) {
            System.out.println("");
        }
        this.executionInput = executionInput;
    }

    public MethodExecutionInput getExecutionInput() {
        return executionInput;
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    private int[] addArgumentPanel(DBArgument argument, int[] gridMetrics) {
        MethodExecutionArgumentForm argumentComponent = new MethodExecutionArgumentForm(this, argument);
        argumentsPanel.add(argumentComponent.getComponent());
        argumentForms.add(argumentComponent);
        return argumentComponent.getMetrics(gridMetrics);
   }

    public void updateExecutionInput() {
        for (MethodExecutionArgumentForm argumentComponent : argumentForms) {
            argumentComponent.updateExecutionInput();
        }

        executionInput.setUsePoolConnection(usePoolConnectionCheckBox.isSelected());
        executionInput.setCommitAfterExecution(commitCheckBox.isSelected());
        //DBSchema schema = (DBSchema) schemaList.getSelectedValue();
        //executionInput.setExecutionSchema(schema);
    }

    public void addChangeListener(ChangeListener changeListener) {
        changeListeners.add(changeListener);
    }

    private DocumentListener documentListener = new DocumentAdapter() {
        protected void textChanged(DocumentEvent e) {
            notifyChangeListeners();
        }
    };

    private ActionListener actionListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            notifyChangeListeners();
        }
    };

    private void notifyChangeListeners() {
        if (changeListeners != null) {
            for (ChangeListener changeListener : changeListeners) {
                changeListener.stateChanged(new ChangeEvent(this));
            }
        }
    }

    public void touch() {
        commitCheckBox.setSelected(!commitCheckBox.isSelected());    
        commitCheckBox.setSelected(!commitCheckBox.isSelected());
    }


    public void dispose() {
        super.dispose();
        DisposerUtil.dispose(argumentForms);
        changeListeners.clear();
        argumentForms = null;
        executionInput = null;
        changeListeners = null;
    }
}
