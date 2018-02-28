package com.dci.intellij.dbn.connection;

import java.util.EventListener;

import com.intellij.util.messages.Topic;

public interface ConnectionManagerListener extends EventListener {
    Topic<ConnectionManagerListener> TOPIC = Topic.create("Connections changed", ConnectionManagerListener.class);
    void connectionsChanged();
}
