package com.dci.intellij.dbn.common.ui.list;

import com.dci.intellij.dbn.common.dispose.DisposableProjectComponent;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.ui.ToolbarDecorator;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Container;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EditableStringListForm extends DBNFormImpl<DisposableProjectComponent>{
    private JPanel component;
    private JLabel titleLabel;
    private JPanel listPanel;

    private EditableStringList editableStringList;

    public EditableStringListForm(DisposableProjectComponent parentComponent, String title, boolean sorted) {
        this(parentComponent, title, new ArrayList<String>(), sorted);
    }

    public EditableStringListForm(DisposableProjectComponent parentComponent, String title, List<String> elements, boolean sorted) {
        super(parentComponent);
        editableStringList = new EditableStringList(null, elements, sorted, false);
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(editableStringList);
        decorator.setAddAction(new AnActionButtonRunnable() {
            @Override
            public void run(AnActionButton anActionButton) {
                editableStringList.insertRow();
            }
        });
        decorator.setRemoveAction(new AnActionButtonRunnable() {
            @Override
            public void run(AnActionButton anActionButton) {
                editableStringList.removeRow();
            }
        });
        decorator.setMoveUpAction(new AnActionButtonRunnable() {
            @Override
            public void run(AnActionButton anActionButton) {
                editableStringList.moveRowUp();
            }
        });
        decorator.setMoveDownAction(new AnActionButtonRunnable() {
            @Override
            public void run(AnActionButton anActionButton) {
                editableStringList.moveRowDown();
            }
        });
        titleLabel.setText(title);
        //decorator.setPreferredSize(new Dimension(200, 300));
        JPanel editableListPanel = decorator.createPanel();
        Container parent = editableStringList.getParent();
        parent.setBackground(editableStringList.getBackground());
        this.listPanel.add(editableListPanel, BorderLayout.CENTER);
        Disposer.register(this, editableStringList);
    }

    @Override
    public JComponent getComponent() {
        return component;
    }

    public List<String> getStringValues() {
        return editableStringList.getStringValues();
    }

    public void setStringValues(Collection<String> stringValues) {
        editableStringList.setStringValues(stringValues);
    }
}
