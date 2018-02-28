package com.dci.intellij.dbn.data.record.navigation.action;

import com.dci.intellij.dbn.editor.data.filter.DatasetFilterInput;
import com.intellij.openapi.actionSystem.DefaultActionGroup;

public class RecordNavigationActionGroup extends DefaultActionGroup{

    public RecordNavigationActionGroup(DatasetFilterInput filterInput) {
        add(new OpenRecordEditorAction(filterInput));
        add(new OpenRecordViewerAction(filterInput));
    }
}
