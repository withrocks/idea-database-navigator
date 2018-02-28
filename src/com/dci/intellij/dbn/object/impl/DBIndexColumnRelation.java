package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBIndex;
import com.dci.intellij.dbn.object.common.DBObjectRelationType;
import com.dci.intellij.dbn.object.common.list.DBObjectRelationImpl;

public class DBIndexColumnRelation extends DBObjectRelationImpl<DBIndex, DBColumn> {
    public DBIndexColumnRelation(DBIndex index, DBColumn column) {
        super(DBObjectRelationType.INDEX_COLUMN, index, column);
    }

    public DBIndex getIndex() {
        return getSourceObject();
    }

    public DBColumn getColumn() {
        return getTargetObject();
    }
}