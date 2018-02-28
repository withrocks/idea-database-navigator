package com.dci.intellij.dbn.editor.data.filter.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilter;
import com.dci.intellij.dbn.editor.data.filter.ui.DatasetFilterList;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class CreateBasicFilterAction extends AbstractFilterListAction {

    public CreateBasicFilterAction(DatasetFilterList filterList) {
        super(filterList,  "Basic filter", Icons.DATASET_FILTER_BASIC);
    }

    public void actionPerformed(AnActionEvent e) {
        DatasetFilter filter = getFilterGroup().createBasicFilter(true);
        getFilterList().setSelectedValue(filter, true);
    }
}