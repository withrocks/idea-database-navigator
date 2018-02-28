package com.dci.intellij.dbn.object.common;

import com.dci.intellij.dbn.common.content.DynamicContentType;

public enum DBObjectRelationType implements DynamicContentType {
    CONSTRAINT_COLUMN(DBObjectType.CONSTRAINT, DBObjectType.COLUMN),
    INDEX_COLUMN(DBObjectType.INDEX, DBObjectType.COLUMN),
    USER_ROLE(DBObjectType.USER, DBObjectType.GRANTED_ROLE),
    USER_PRIVILEGE(DBObjectType.USER, DBObjectType.GRANTED_PRIVILEGE),
    ROLE_PRIVILEGE(DBObjectType.ROLE, DBObjectType.GRANTED_PRIVILEGE),
    ROLE_ROLE(DBObjectType.ROLE, DBObjectType.GRANTED_ROLE); 

    private DBObjectType sourceType;
    private DBObjectType targetType;

    DBObjectRelationType(DBObjectType sourceType, DBObjectType targetType) {
        this.sourceType = sourceType;
        this.targetType = targetType;
    }

    public DBObjectType getSourceType() {
        return sourceType;
    }

    public DBObjectType getTargetType() {
        return targetType;
    }
}
