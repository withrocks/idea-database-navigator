package com.dci.intellij.dbn.object.dependency.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.DBNComboBox;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.common.ui.ValueSelectorListener;
import com.dci.intellij.dbn.common.ui.tree.TreeUtil;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.dependency.ObjectDependencyType;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;

public class ObjectDependencyTreeForm extends DBNFormImpl<ObjectDependencyTreeDialog>{
    private JPanel mainPanel;
    private JPanel actionsPanel;
    private JPanel contentPanel;
    private JPanel headerPanel;
    private DBNComboBox<ObjectDependencyType> dependencyTypeComboBox;
    private JBScrollPane treeScrollPane;


    private ObjectDependencyTree dependencyTree;

    private DBObjectRef<DBSchemaObject> objectRef;

    public ObjectDependencyTreeForm(ObjectDependencyTreeDialog parentComponent, final DBSchemaObject schemaObject) {
        super(parentComponent);
        dependencyTree = new ObjectDependencyTree(getProject(), schemaObject);
        treeScrollPane.setViewportView(dependencyTree);

        dependencyTypeComboBox.setValues(ObjectDependencyType.values());
        dependencyTypeComboBox.setSelectedValue(ObjectDependencyType.OUTGOING);
        dependencyTypeComboBox.addListener(new ValueSelectorListener<ObjectDependencyType>() {
            @Override
            public void valueSelected(ObjectDependencyType value) {
                dependencyTree.setDependencyType(value);
            }
        });
        this.objectRef = DBObjectRef.from(schemaObject);
        DBNHeaderForm headerForm = new DBNHeaderForm(schemaObject);
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);

        ActionToolbar actionToolbar = ActionUtil.createActionToolbar("", true, /*new ExpandTreeAction(),*/ new CollapseTreeAction());
        actionsPanel.add(actionToolbar.getComponent(), BorderLayout.CENTER);

    }

    private DBSchemaObject getObject() {
        return DBObjectRef.get(objectRef);
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    public class ExpandTreeAction extends DumbAwareAction {

        public ExpandTreeAction() {
            super("Expand All", null, Icons.ACTION_EXPAND_ALL);
        }

        public void actionPerformed(AnActionEvent e) {
            TreeUtil.expandAll(dependencyTree);
        }

        public void update(AnActionEvent e) {
            Presentation presentation = e.getPresentation();
            presentation.setText("Expand All");
        }
    }
    public class CollapseTreeAction extends DumbAwareAction {

        public CollapseTreeAction() {
            super("Collapse All", null, Icons.ACTION_COLLAPSE_ALL);
        }

        public void actionPerformed(AnActionEvent e) {
            TreeUtil.collapseAll(dependencyTree);
        }

        public void update(AnActionEvent e) {
            Presentation presentation = e.getPresentation();
            presentation.setText("Collapse All");
        }
    }
}
