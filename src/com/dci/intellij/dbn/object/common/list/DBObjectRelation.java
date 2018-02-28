package com.dci.intellij.dbn.object.common.list;

import com.dci.intellij.dbn.common.content.DynamicContentElement;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectRelationType;

public interface DBObjectRelation<S extends DBObject, T extends DBObject> extends DynamicContentElement {
    DBObjectRelationType getObjectRelationType();
    S getSourceObject();
    T getTargetObject();
}
