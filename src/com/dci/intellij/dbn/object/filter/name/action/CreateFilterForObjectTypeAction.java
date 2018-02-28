package com.dci.intellij.dbn.object.filter.name.action;

import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.filter.name.ObjectNameFilterManager;
import com.dci.intellij.dbn.object.filter.name.ui.ObjectNameFilterSettingsForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

public class CreateFilterForObjectTypeAction extends ObjectNameFilterAction{
    private DBObjectType objectType;

    public CreateFilterForObjectTypeAction(DBObjectType objectType, ObjectNameFilterSettingsForm settingsForm) {
        super(objectType.getName().toUpperCase(), objectType.getIcon(), settingsForm);
        this.objectType = objectType;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        if (project != null) {
            ObjectNameFilterManager.getInstance(project).createFilter(objectType, settingsForm);
        }
    }

    @Override
    public void update(AnActionEvent e) {

    }
}
