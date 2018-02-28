package com.dci.intellij.dbn.object.factory.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.Borders;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.type.ui.DataTypeEditor;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.factory.MethodFactoryInput;
import com.dci.intellij.dbn.object.factory.ObjectFactoryInput;
import com.dci.intellij.dbn.object.factory.ui.common.ObjectFactoryInputForm;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.project.Project;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.ui.UIUtil;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import java.awt.BorderLayout;
import java.awt.Color;

public abstract class MethodFactoryInputForm extends ObjectFactoryInputForm<MethodFactoryInput> {
    private JPanel mainPanel;
    private JLabel connectionLabel;
    private JLabel schemaLabel;
    protected JTextField nameTextField;
    private JPanel returnArgumentPanel;
    private JPanel argumentListComponent;
    private JLabel returnArgumentIconLabel;
    JPanel returnArgumentDataTypeEditor;
    private JPanel headerPanel;
    private JLabel nameLabel;

    private ArgumentFactoryInputListForm argumentListPanel;
    private DBObjectRef<DBSchema> schemaRef;

    public MethodFactoryInputForm(Project project, DBSchema schema, DBObjectType objectType, int index) {
        super(project, schema.getConnectionHandler(), objectType, index);
        this.schemaRef = DBObjectRef.from(schema);
        connectionLabel.setText(getConnectionHandler().getName());
        connectionLabel.setIcon(getConnectionHandler().getIcon());

        schemaLabel.setText(schema.getName());
        schemaLabel.setIcon(schema.getIcon());

        returnArgumentPanel.setVisible(hasReturnArgument());
        returnArgumentPanel.setBorder(Borders.BOTTOM_LINE_BORDER);
        argumentListPanel.createObjectPanel();
        argumentListPanel.createObjectPanel();
        argumentListPanel.createObjectPanel();

        returnArgumentIconLabel.setText(null);
        returnArgumentIconLabel.setIcon(Icons.DBO_ARGUMENT_OUT);

        nameLabel.setText(
                objectType == DBObjectType.FUNCTION ? "Function Name" :
                objectType == DBObjectType.PROCEDURE ? "Procedure Name" : "Name");

        final DBNHeaderForm headerForm = createHeaderForm(schema, objectType);
        nameTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent e) {
                headerForm.setTitle(getSchema().getName() + "." + nameTextField.getText().toUpperCase());
            }
        });
    }

    private DBNHeaderForm createHeaderForm(DBSchema schema, DBObjectType objectType) {
        String headerTitle = schema.getName() + ".[unnamed]";
        Icon headerIcon = objectType.getIcon();
        Color headerBackground = UIUtil.getPanelBackground();
        if (getEnvironmentSettings(schema.getProject()).getVisibilitySettings().getDialogHeaders().value()) {
            headerBackground = schema.getEnvironmentType().getColor();
        }
        DBNHeaderForm headerForm = new DBNHeaderForm(
                headerTitle,
                headerIcon,
                headerBackground);
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);
        return headerForm;
    }

    public MethodFactoryInput createFactoryInput(ObjectFactoryInput parent) {
        MethodFactoryInput methodFactoryInput = new MethodFactoryInput(getSchema(), nameTextField.getText(), getObjectType(), getIndex());
        methodFactoryInput.setArguments(argumentListPanel.createFactoryInputs(methodFactoryInput));
        return methodFactoryInput;
    }

    DBSchema getSchema() {
        return DBObjectRef.get(schemaRef);
    }

    public abstract boolean hasReturnArgument();

    private void createUIComponents() {
        ConnectionHandler connectionHandler = getConnectionHandler();
        boolean enforceInArguments = hasReturnArgument() && !DatabaseFeature.FUNCTION_OUT_ARGUMENTS.isSupported(connectionHandler);
        argumentListPanel = new ArgumentFactoryInputListForm(this, connectionHandler, enforceInArguments);
        argumentListComponent = argumentListPanel.getComponent();
        returnArgumentDataTypeEditor = new DataTypeEditor(getConnectionHandler());
    }

    public void focus() {
        nameTextField.requestFocus();
    }

    public JPanel getComponent() {
        return mainPanel;
    }
}
