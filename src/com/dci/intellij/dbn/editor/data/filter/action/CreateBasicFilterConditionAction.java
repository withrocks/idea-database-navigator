package com.dci.intellij.dbn.editor.data.filter.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.editor.data.filter.ui.DatasetBasicFilterForm;
import com.dci.intellij.dbn.object.DBColumn;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;


public class CreateBasicFilterConditionAction extends DumbAwareAction {
    private DatasetBasicFilterForm filterForm;

    public CreateBasicFilterConditionAction(DatasetBasicFilterForm filterForm) {
        super("Add condition", null, Icons.DATASET_FILTER_CONDITION_NEW);
        this.filterForm = filterForm;
    }

    public void actionPerformed(AnActionEvent e) {
        filterForm.addConditionPanel((DBColumn) null);
    }
}
