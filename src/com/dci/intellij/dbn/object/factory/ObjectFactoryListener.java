package com.dci.intellij.dbn.object.factory;

import java.util.EventListener;

import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.util.messages.Topic;

public interface ObjectFactoryListener extends EventListener{
    Topic<ObjectFactoryListener> TOPIC = Topic.create("Object Factory Event", ObjectFactoryListener.class);
    void objectCreated(DBSchemaObject object);
    void objectDropped(DBSchemaObject object);
}
