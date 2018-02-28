package com.dci.intellij.dbn.ddl.options.listener;

import java.util.EventListener;

import com.intellij.util.messages.Topic;

public interface DDLFileSettingsChangeListener extends EventListener {
    Topic<DDLFileSettingsChangeListener> TOPIC = Topic.create("DDLFileSettingsEvents", DDLFileSettingsChangeListener.class);
    void settingsChanged();
}
