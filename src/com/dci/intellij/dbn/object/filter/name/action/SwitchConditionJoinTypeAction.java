package com.dci.intellij.dbn.object.filter.name.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.object.filter.name.CompoundFilterCondition;
import com.dci.intellij.dbn.object.filter.name.ObjectNameFilterManager;
import com.dci.intellij.dbn.object.filter.name.ui.ObjectNameFilterSettingsForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;

public class SwitchConditionJoinTypeAction extends ObjectNameFilterAction{

    public SwitchConditionJoinTypeAction(ObjectNameFilterSettingsForm settingsForm) {
        super("Switch Join Type", Icons.CONDITION_JOIN_TYPE, settingsForm);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        Object selection = getSelection();
        ObjectNameFilterManager filterManager = null;
        if (project != null) {
            filterManager = ObjectNameFilterManager.getInstance(project);
            if (selection instanceof CompoundFilterCondition) {
                CompoundFilterCondition condition = (CompoundFilterCondition) selection;
                filterManager.switchConditionJoinType(condition, settingsForm);
            }
        }
    }

    @Override
    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        Object selection = getSelection();
        if (selection instanceof CompoundFilterCondition) {
            CompoundFilterCondition condition = (CompoundFilterCondition) selection;
            presentation.setEnabled(condition.getConditions().size() > 1);
        } else {
            presentation.setEnabled(false);
        }
    }
}
