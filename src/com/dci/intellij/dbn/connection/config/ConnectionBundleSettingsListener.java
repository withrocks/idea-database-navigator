package com.dci.intellij.dbn.connection.config;

import com.intellij.util.messages.Topic;

import java.util.EventListener;

public interface ConnectionBundleSettingsListener extends EventListener {
    Topic<ConnectionBundleSettingsListener> TOPIC = Topic.create("Connections changed", ConnectionBundleSettingsListener.class);
    void settingsChanged();
}
