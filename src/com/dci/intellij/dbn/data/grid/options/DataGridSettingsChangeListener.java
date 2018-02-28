package com.dci.intellij.dbn.data.grid.options;

import java.util.EventListener;

import com.intellij.util.messages.Topic;

public interface DataGridSettingsChangeListener extends EventListener {
    Topic<DataGridSettingsChangeListener> TOPIC = Topic.create("Data Grid settings change event", DataGridSettingsChangeListener.class);
    void trackingColumnsVisibilityChanged(boolean visible);
}
