package com.dci.intellij.dbn.common.ui;

import javax.swing.Icon;

import com.intellij.openapi.actionSystem.ex.CheckboxAction;

public abstract class DBNCheckboxAction extends CheckboxAction{
    protected DBNCheckboxAction() {
    }

    protected DBNCheckboxAction(String text) {
        super(text);
    }

    protected DBNCheckboxAction(String text, String description, Icon icon) {
        super(text, description, icon);
    }
}
