package com.dci.intellij.dbn.connection.config;

import com.intellij.util.messages.Topic;

import java.util.EventListener;

public interface ConnectionSettingsListener extends EventListener {
    Topic<ConnectionSettingsListener> TOPIC = Topic.create("Connection changed", ConnectionSettingsListener.class);
    void settingsChanged(String connectionId);
}
