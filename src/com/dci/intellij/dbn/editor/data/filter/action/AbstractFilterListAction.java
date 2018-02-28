package com.dci.intellij.dbn.editor.data.filter.action;

import com.dci.intellij.dbn.editor.data.filter.DatasetFilterGroup;
import com.dci.intellij.dbn.editor.data.filter.ui.DatasetFilterList;
import com.intellij.openapi.project.DumbAwareAction;

import javax.swing.*;

public abstract class AbstractFilterListAction extends DumbAwareAction {
    private DatasetFilterList filterList;

    protected AbstractFilterListAction(DatasetFilterList filterList, String name, Icon icon) {
        super(name, null, icon);
        this.filterList = filterList;
    }

    public DatasetFilterList getFilterList() {
        return filterList;
    }

    public DatasetFilterGroup getFilterGroup() {
        return (DatasetFilterGroup) filterList.getModel();
    }
}
