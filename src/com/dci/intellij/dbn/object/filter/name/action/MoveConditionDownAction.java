package com.dci.intellij.dbn.object.filter.name.action;

import java.util.List;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.object.filter.name.CompoundFilterCondition;
import com.dci.intellij.dbn.object.filter.name.FilterCondition;
import com.dci.intellij.dbn.object.filter.name.ObjectNameFilter;
import com.dci.intellij.dbn.object.filter.name.ObjectNameFilterManager;
import com.dci.intellij.dbn.object.filter.name.ui.ObjectNameFilterSettingsForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;

public class MoveConditionDownAction extends ObjectNameFilterAction{

    public MoveConditionDownAction(ObjectNameFilterSettingsForm settingsForm) {
        super("Move Down", Icons.ACTION_MOVE_DOWN, settingsForm);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Object selection = getSelection();
        if (selection instanceof FilterCondition) {
            Project project = ActionUtil.getProject(e);
            if (project != null) {
                FilterCondition condition = (FilterCondition) selection;
                ObjectNameFilterManager filterManager = ObjectNameFilterManager.getInstance(project);
                filterManager.moveFilterConditionDown(condition, settingsForm);
            }
        }
    }

    @Override
    public void update(AnActionEvent e) {
        Object selection = getSelection();
        Presentation presentation = e.getPresentation();
        if (selection instanceof FilterCondition) {
            FilterCondition condition = (FilterCondition) selection;
            if (condition instanceof ObjectNameFilter) {
                ObjectNameFilter filter = (ObjectNameFilter) condition;
                List<ObjectNameFilter> filters = filter.getSettings().getFilters();
                int index = filters.indexOf(filter);
                presentation.setEnabled(index < filters.size() - 1);
            } else {
                CompoundFilterCondition parentCondition = condition.getParent();
                List<FilterCondition> conditions = parentCondition.getConditions();
                int index = conditions.indexOf(condition);
                presentation.setEnabled(index < conditions.size() - 1);
            }
        } else {
            presentation.setEnabled(false);
        }
    }
}
