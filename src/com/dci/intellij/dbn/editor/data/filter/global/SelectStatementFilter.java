package com.dci.intellij.dbn.editor.data.filter.global;

import com.dci.intellij.dbn.object.DBDataset;

public interface SelectStatementFilter {
    String createSelectStatement(DBDataset dataset);
}
