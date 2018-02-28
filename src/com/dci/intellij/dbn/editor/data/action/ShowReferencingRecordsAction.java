package com.dci.intellij.dbn.editor.data.action;

import java.util.ArrayList;
import java.util.List;

import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilterInput;
import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.action.ObjectListShowAction;
import com.dci.intellij.dbn.object.common.DBObject;
import com.intellij.openapi.actionSystem.AnAction;

public class ShowReferencingRecordsAction extends ObjectListShowAction{
    private Object columnValue;

    public ShowReferencingRecordsAction(DBColumn column, Object columnValue) {
        super("Show referencing records...", column);
        this.columnValue = columnValue;
    }


    @Override
    public List<DBObject> getObjectList() {
        DBColumn column = (DBColumn) sourceObject;
        return new ArrayList<DBObject>(column.getReferencingColumns());
    }

    @Override
    public String getTitle() {
        return "Referencing datasets";
    }

    @Override
    public String getEmptyListMessage() {
        return "No referencing records found";
    }

    @Override
    public String getListName() {
        return "Referencing records";
    }

    @Override
    protected AnAction createObjectAction(DBObject object) {
        DBColumn column = (DBColumn) object;
        DatasetFilterInput filterInput = new DatasetFilterInput(column.getDataset());
        filterInput.setColumnValue(column, columnValue);
        String actionText = NamingUtil.enhanceNameForDisplay(column.getDataset().getName() + " - " + column.getName());
        return new ShowRecordsAction(actionText, filterInput) {};
    }
}
