package com.dci.intellij.dbn.connection;

import java.util.EventListener;

import com.intellij.util.messages.Topic;

public interface ConnectionStatusListener extends EventListener {
    Topic<ConnectionStatusListener> TOPIC = Topic.create("Connection status changed", ConnectionStatusListener.class);
    void statusChanged(String connectionId);
}
