package com.dci.intellij.dbn.object.action;

import com.dci.intellij.dbn.editor.data.filter.global.DataDependencyPath;
import com.dci.intellij.dbn.editor.data.filter.global.DataDependencyPathBuilder;
import com.dci.intellij.dbn.object.DBTable;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class TestAction extends AnAction {
    private DBObject object;
    public TestAction(DBObject object) {
        super("Test", "Test", null);
        this.object = object;
        setDefaultIcon(true);
    }

    public void actionPerformed(AnActionEvent e) {
        new Thread() {
            public void run() {
                if (object instanceof DBTable) {
                    DBTable table = (DBTable) object;
                    DBTable target = (DBTable) table.getSchema().getChildObject(DBObjectType.TABLE, "ALLOCATIONS", 0, false);
                    DataDependencyPath[] shortestPath = new DataDependencyPath[1];
                    DataDependencyPathBuilder.buildDependencyPath(null, table.getColumns().get(0), target.getColumns().get(0), shortestPath);
                    System.out.println();
                }
            }
        }.start();
    }
}