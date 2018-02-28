package com.dci.intellij.dbn.object.filter.name.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.object.filter.name.FilterCondition;
import com.dci.intellij.dbn.object.filter.name.ObjectNameFilter;
import com.dci.intellij.dbn.object.filter.name.ObjectNameFilterManager;
import com.dci.intellij.dbn.object.filter.name.ui.ObjectNameFilterSettingsForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;

public class RemoveConditionAction extends ObjectNameFilterAction{

    public RemoveConditionAction(ObjectNameFilterSettingsForm settingsForm) {
        super("Remove", Icons.ACTION_REMOVE, settingsForm);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Object selection = getSelection();
        if (selection instanceof FilterCondition) {
            FilterCondition filterCondition = (FilterCondition) selection;

            Project project = ActionUtil.getProject(e);
            ObjectNameFilterManager filterManager = null;
            if (project != null) {
                filterManager = ObjectNameFilterManager.getInstance(project);
                filterManager.removeFilterCondition(filterCondition, settingsForm);
            }
        }
    }

    @Override
    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        Object selection = getSelection();
        if (selection instanceof ObjectNameFilter) {
            presentation.setText("Remove Filter");
            presentation.setEnabled(true);
        } else if (selection instanceof FilterCondition) {
            presentation.setText("Remove Condition");
            presentation.setEnabled(true);
        } else {
            presentation.setEnabled(false);
        }
    }

}
