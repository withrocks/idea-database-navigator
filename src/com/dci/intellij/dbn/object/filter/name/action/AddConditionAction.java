package com.dci.intellij.dbn.object.filter.name.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.object.filter.name.CompoundFilterCondition;
import com.dci.intellij.dbn.object.filter.name.ObjectNameFilterManager;
import com.dci.intellij.dbn.object.filter.name.SimpleFilterCondition;
import com.dci.intellij.dbn.object.filter.name.ui.ObjectNameFilterSettingsForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;

public class AddConditionAction extends ObjectNameFilterAction{

    public AddConditionAction(ObjectNameFilterSettingsForm settingsForm) {
        super("Add Filter Condition", Icons.ACTION_ADD, settingsForm);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        if (project != null) {
            Object selection = getSelection();
            ObjectNameFilterManager filterManager = ObjectNameFilterManager.getInstance(project);
            if (selection instanceof CompoundFilterCondition) {
                CompoundFilterCondition condition = (CompoundFilterCondition) selection;
                filterManager.createFilterCondition(condition, settingsForm);
            } else if (selection instanceof SimpleFilterCondition) {
                SimpleFilterCondition condition = (SimpleFilterCondition) selection;
                filterManager.joinFilterCondition(condition, settingsForm);
            }
        }
    }

    @Override
    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        Object selection = getSelection();
        if (selection instanceof CompoundFilterCondition) {
            presentation.setText("Add Condition");
            presentation.setEnabled(true);
            presentation.setIcon(Icons.ACTION_ADD);
        } else if (selection instanceof SimpleFilterCondition) {
            presentation.setText("Join Condition");
            presentation.setEnabled(true);
            presentation.setIcon(Icons.ACTION_ADD_SPECIAL);
        } else {
            presentation.setIcon(Icons.ACTION_ADD);
            presentation.setEnabled(false);
        }
    }
}
