package com.dci.intellij.dbn.editor.data.filter.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilter;
import com.dci.intellij.dbn.editor.data.filter.ui.DatasetFilterList;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class CreateCustomFilterAction extends AbstractFilterListAction {

    public CreateCustomFilterAction(DatasetFilterList filterList) {
        super(filterList,  "Custom filter", Icons.DATASET_FILTER_CUSTOM);
    }

    public void actionPerformed(AnActionEvent e) {
        DatasetFilter filter = getFilterGroup().createCustomFilter(true);
        getFilterList().setSelectedValue(filter, true);
    }
}