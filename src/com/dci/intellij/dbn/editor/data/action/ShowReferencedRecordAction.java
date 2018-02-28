package com.dci.intellij.dbn.editor.data.action;

import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilterInput;

public class ShowReferencedRecordAction extends ShowRecordsAction {

    public ShowReferencedRecordAction(DatasetFilterInput filterInput) {
        super(getActionText(filterInput), filterInput);
    }

    private static String getActionText(DatasetFilterInput filterInput) {
        return NamingUtil.enhanceNameForDisplay("Show referenced " + filterInput.getDataset().getName() + " record");
    }

}
