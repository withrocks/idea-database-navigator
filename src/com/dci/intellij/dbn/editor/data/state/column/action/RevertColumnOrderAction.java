package com.dci.intellij.dbn.editor.data.state.column.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.list.CheckBoxList;
import com.dci.intellij.dbn.editor.data.state.column.ui.ColumnStateSelectable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class RevertColumnOrderAction extends AnAction {
    private CheckBoxList list;

    public RevertColumnOrderAction(CheckBoxList list)  {
        super("Revert Column Order", null, Icons.ACTION_REVERT_CHANGES);
        this.list = list;
    }

    public void actionPerformed(AnActionEvent e) {
        list.sortElements(ColumnStateSelectable.POSITION_COMPARATOR);
    }
}
