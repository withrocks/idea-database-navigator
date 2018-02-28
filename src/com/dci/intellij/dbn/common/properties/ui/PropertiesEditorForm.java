package com.dci.intellij.dbn.common.properties.ui;

import com.dci.intellij.dbn.common.ui.DBNForm;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.ui.ToolbarDecorator;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Container;
import java.util.Map;

public class PropertiesEditorForm extends DBNFormImpl<DBNForm> {
    private JPanel mainPanel;
    private PropertiesEditorTable propertiesTable;

    public PropertiesEditorForm(DBNForm parentForm, Map<String, String> properties, boolean showMoveButtons) {
        super(parentForm);
        propertiesTable = new PropertiesEditorTable(properties);
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(propertiesTable);
        decorator.setAddAction(new AnActionButtonRunnable() {
            @Override
            public void run(AnActionButton anActionButton) {
                propertiesTable.insertRow();
            }
        });

        decorator.setRemoveAction(new AnActionButtonRunnable() {
            @Override
            public void run(AnActionButton anActionButton) {
                propertiesTable.removeRow();
            }
        });

        if (showMoveButtons) {
            decorator.setMoveUpAction(new AnActionButtonRunnable() {
                @Override
                public void run(AnActionButton anActionButton) {
                    propertiesTable.moveRowUp();
                }
            });

            decorator.setMoveDownAction(new AnActionButtonRunnable() {
                @Override
                public void run(AnActionButton anActionButton) {
                    propertiesTable.moveRowDown();
                }
            });
        }

        JPanel propertiesPanel = decorator.createPanel();
        Container parent = propertiesTable.getParent();
        parent.setBackground(propertiesTable.getBackground());
        mainPanel.add(propertiesPanel, BorderLayout.CENTER);
/*
        propertiesTableScrollPane.setViewportView(propertiesTable);
        propertiesTableScrollPane.setPreferredSize(new Dimension(200, 80));
*/

    }

    public void setProperties(Map<String, String> properties) {
        propertiesTable.setProperties(properties);
    } 

    public JComponent getComponent() {
        return mainPanel;
    }

    public void dispose() {
        super.dispose();
    }

    public Map<String, String> getProperties() {
        return propertiesTable.getModel().exportProperties();
    }
}
