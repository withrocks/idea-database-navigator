package com.dci.intellij.dbn.data.find.action;

import com.dci.intellij.dbn.common.ui.DBNCheckboxAction;
import com.dci.intellij.dbn.data.find.DataSearchComponent;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAware;

import javax.swing.JCheckBox;
import javax.swing.JComponent;

public abstract class DataSearchHeaderToggleAction extends DBNCheckboxAction implements DumbAware {

    @Override
    public boolean displayTextInToolbar() {
        return true;
    }

    public DataSearchComponent getEditorSearchComponent() {
        return searchComponent;
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
    }

    @Override
    public JComponent createCustomComponent(Presentation presentation) {
        final JComponent customComponent = super.createCustomComponent(presentation);
        if (customComponent instanceof JCheckBox) {
            JCheckBox checkBox = (JCheckBox) customComponent;
            checkBox.setFocusable(false);
            checkBox.setOpaque(false);
        }
        return customComponent;
    }

    private DataSearchComponent searchComponent;

    protected DataSearchHeaderToggleAction(DataSearchComponent searchComponent, String text) {
        super(text);
        this.searchComponent = searchComponent;
    }
}
