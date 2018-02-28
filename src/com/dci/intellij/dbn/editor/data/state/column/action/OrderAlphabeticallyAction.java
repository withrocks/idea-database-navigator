package com.dci.intellij.dbn.editor.data.state.column.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.list.CheckBoxList;
import com.dci.intellij.dbn.editor.data.state.column.ui.ColumnStateSelectable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class OrderAlphabeticallyAction extends AnAction {
    private CheckBoxList list;

    public OrderAlphabeticallyAction(CheckBoxList list)  {
        super("Order Columns Alphabetically", null, Icons.ACTION_SORT_ALPHA);
        this.list = list;
    }

    public void actionPerformed(AnActionEvent e) {
        list.sortElements(ColumnStateSelectable.NAME_COMPARATOR);
    }
}
