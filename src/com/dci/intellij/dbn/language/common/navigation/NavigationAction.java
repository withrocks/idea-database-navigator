package com.dci.intellij.dbn.language.common.navigation;

import com.dci.intellij.dbn.editor.code.SourceCodeManager;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import javax.swing.Icon;

public abstract class NavigationAction extends AnAction {
    private BasePsiElement navigationElement;
    private DBObject parentObject;

    protected NavigationAction(String text, Icon icon, DBObject parentObject, BasePsiElement navigationElement) {
        super(text, null, icon);
        this.parentObject = parentObject;
        this.navigationElement = navigationElement;
    }

    public DBObject getParentObject() {
        return parentObject;
    }

    public void actionPerformed(AnActionEvent e) {
        navigate();
    }

    public void navigate() {
        if (parentObject != null) {
            SourceCodeManager codeEditorManager = SourceCodeManager.getInstance(parentObject.getProject());
            codeEditorManager.navigateToObject((DBSchemaObject) parentObject, navigationElement);
        } else {
            navigationElement.navigate(true);
        }
    }
}
