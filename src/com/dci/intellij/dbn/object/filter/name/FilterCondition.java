package com.dci.intellij.dbn.object.filter.name;

import com.dci.intellij.dbn.common.options.PersistentConfiguration;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;

public interface FilterCondition extends PersistentConfiguration {
    void setParent(CompoundFilterCondition parent);
    CompoundFilterCondition getParent();
    DBObjectType getObjectType();
    String getConditionString();
    ObjectNameFilterSettings getSettings();
    boolean accepts(DBObject object);
}
