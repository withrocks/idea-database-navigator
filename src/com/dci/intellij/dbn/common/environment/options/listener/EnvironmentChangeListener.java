package com.dci.intellij.dbn.common.environment.options.listener;

import java.util.EventListener;

import com.intellij.util.messages.Topic;

public interface EnvironmentChangeListener extends EventListener {
    Topic<EnvironmentChangeListener> TOPIC = Topic.create("Environment changed", EnvironmentChangeListener.class);
    void configurationChanged();
}
