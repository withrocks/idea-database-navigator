package com.dci.intellij.dbn.editor.data.state.sorting.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.editor.data.state.sorting.ui.DatasetSortingColumnForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;

public class DeleteSortingCriteriaAction extends DumbAwareAction {
    private DatasetSortingColumnForm form;

    public DeleteSortingCriteriaAction(DatasetSortingColumnForm form) {
        this.form = form;
    }

    public void update(AnActionEvent e) {
        e.getPresentation().setIcon(Icons.ACTION_DELETE);
        e.getPresentation().setText("Remove Sorting Criteria");
    }

    public void actionPerformed(AnActionEvent e) {
        form.remove();
    }

}
