package com.dci.intellij.dbn.object.factory;

import com.dci.intellij.dbn.object.common.DBSchemaObject;

public class ObjectFactoryEvent {
    public static final int EVENT_TYPE_CREATE = 0;
    public static final int EVENT_TYPE_DROP = 1;

    private DBSchemaObject object;
    private int eventType;

    public ObjectFactoryEvent(DBSchemaObject object, int eventType) {
        this.object = object;
        this.eventType = eventType;
    }

    public DBSchemaObject getObject() {
        return object;
    }

    public int getEventType() {
        return eventType;
    }
}
