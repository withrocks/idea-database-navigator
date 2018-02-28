package com.dci.intellij.dbn.generator.action;

import java.util.List;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBDataset;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.DBTable;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.openapi.actionSystem.DefaultActionGroup;

public class GenerateStatementActionGroup extends DefaultActionGroup {

    public GenerateStatementActionGroup(DBObject object) {
        super("Extract SQL Statement", true);
        if (object instanceof DBColumn || object instanceof DBDataset) {
            List<DBObject> selectedObjects = DatabaseBrowserManager.getInstance(object.getProject()).getSelectedObjects();
            add(new GenerateSelectStatementAction(selectedObjects));
        }

        if (object instanceof DBTable) {
            DBTable table = (DBTable) object;
            add(new GenerateInsertStatementAction(table));
        }

        if (object instanceof DBSchemaObject &&
                object.getParentObject() instanceof DBSchema &&
                DatabaseFeature.OBJECT_DDL_EXTRACTION.isSupported(object)) {
            if (getChildrenCount() > 1) {
                addSeparator();
            }
            add(new GenerateDDLStatementAction(object));
        }
    }
}