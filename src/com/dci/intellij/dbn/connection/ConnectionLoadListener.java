package com.dci.intellij.dbn.connection;

import java.util.EventListener;

import com.intellij.util.messages.Topic;

public interface ConnectionLoadListener extends EventListener {
    Topic<ConnectionLoadListener> TOPIC = Topic.create("meta-data load event", ConnectionLoadListener.class);
    void contentsLoaded(ConnectionHandler connectionHandler);
}
