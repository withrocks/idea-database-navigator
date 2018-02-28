package com.dci.intellij.dbn.object.factory.ui.common;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import javax.swing.JPanel;
import java.awt.BorderLayout;

public class ObjectListItemForm extends DBNFormImpl {
    private JPanel mainPanel;
    private JPanel removeActionPanel;
    private JPanel objectDetailsComponent;

    private ObjectListForm parent;
    private ObjectFactoryInputForm inputForm;

    public ObjectListItemForm(ObjectListForm parent, ObjectFactoryInputForm inputForm) {
        this.parent = parent;
        this.inputForm = inputForm;
        ActionToolbar actionToolbar = ActionUtil.createActionToolbar(
                "DBNavigator.ObjectFactory.AddElement", true,
                new RemoveObjectAction());
        removeActionPanel.add(actionToolbar.getComponent(), BorderLayout.NORTH);

    }

    public JPanel getComponent(){
        return mainPanel;
    }

    private void createUIComponents() {
        objectDetailsComponent = inputForm.getComponent();
    }

    public void dispose() {
        super.dispose();
        parent = null;
        inputForm = null;
    }


    public class RemoveObjectAction extends AnAction {
        public RemoveObjectAction() {
            super("Remove " + parent.getObjectType(), null, Icons.ACTION_DELETE);
        }

        public void actionPerformed(AnActionEvent e) {
            parent.removeObjectPanel(ObjectListItemForm.this);
        }
    }

    public ObjectFactoryInputForm getObjectDetailsPanel() {
        return inputForm;
    }
}
