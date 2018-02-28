package com.dci.intellij.dbn.editor.data.filter.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilter;
import com.dci.intellij.dbn.editor.data.filter.ui.DatasetFilterList;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class DeleteFilterAction extends AbstractFilterListAction {

    public DeleteFilterAction(DatasetFilterList filterList) {
        super(filterList, "Delete filter", Icons.ACTION_REMOVE);
    }

    public void actionPerformed(AnActionEvent e) {
        Object[] selectedFilters = getFilterList().getSelectedValues();
        for (Object object : selectedFilters) {
            DatasetFilter filter = (DatasetFilter) object;
            getFilterGroup().deleteFilter(filter);
            if (getFilterList().getModel().getSize() > 0) {
                getFilterList().setSelectedIndex(0);
            }

        }
    }
}
