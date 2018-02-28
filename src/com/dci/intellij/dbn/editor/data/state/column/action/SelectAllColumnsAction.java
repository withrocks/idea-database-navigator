package com.dci.intellij.dbn.editor.data.state.column.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.list.CheckBoxList;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class SelectAllColumnsAction extends AnAction {
    private CheckBoxList list;

    public SelectAllColumnsAction(CheckBoxList list)  {
        super("Select All Columns", null, Icons.ACTION_SELECT_ALL);
        this.list = list;
    }

    public void actionPerformed(AnActionEvent e) {
        list.selectAll();
    }
}
