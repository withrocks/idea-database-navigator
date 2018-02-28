package com.dci.intellij.dbn.object.common.list;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectRelationType;

public abstract class DBObjectRelationImpl<S extends DBObject, T extends DBObject> implements DBObjectRelation<S, T> {

    private DBObjectRelationType objectRelationType;
    private boolean isDisposed = false;
    private S sourceObject;
    private T targetObject;
    private DynamicContent ownerContent;

    public DBObjectRelationImpl(DBObjectRelationType objectRelationType, S sourceObject, T targetObject) {
        this.objectRelationType = objectRelationType;
        assert sourceObject.getObjectType() == objectRelationType.getSourceType();
        assert targetObject.getObjectType() == objectRelationType.getTargetType();
        this.sourceObject = sourceObject;
        this.targetObject = targetObject;
    }



    public DBObjectRelationType getObjectRelationType() {
        return objectRelationType;
    }

    public S getSourceObject() {
        return sourceObject;
    }

    public void setSourceObject(S sourceObject) {
        this.sourceObject = sourceObject;
    }

    public T getTargetObject() {
        return targetObject;
    }

    public void setTargetObject(T targetObject) {
        this.targetObject = targetObject;
    }

    public String toString() {
        return sourceObject.getQualifiedNameWithType() + " => " + targetObject.getQualifiedNameWithType();
    }

    /*********************************************************
    *               DynamicContentElement                   *
    *********************************************************/
    public DynamicContent getOwnerContent() {
        return ownerContent;
    }

    public void setOwnerContent(DynamicContent ownerContent) {
        this.ownerContent = ownerContent;
    }

    public boolean isDisposed() {
        return isDisposed;
    }

    public String getName() {
        return null;
    }

    @Override
    public int getOverload() {
        return 0;
    }

    public String getDescription() {
        return null;
    }

    public void dispose() {
        isDisposed = true;
        sourceObject = null;
        targetObject = null;
        ownerContent = null;
    }

    public void reload() {
    }

    public int compareTo(@NotNull Object o) {
        DBObjectRelationImpl remote = (DBObjectRelationImpl) o;
        return sourceObject.compareTo(remote.sourceObject);
    }

}
