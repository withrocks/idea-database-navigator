package com.dci.intellij.dbn.browser.options.listener;

import com.dci.intellij.dbn.browser.options.BrowserDisplayMode;
import com.intellij.util.messages.Topic;

import java.util.EventListener;

public interface DisplayModeSettingsListener extends EventListener {
    Topic<DisplayModeSettingsListener> TOPIC = Topic.create("Browser Display Mode Settings", DisplayModeSettingsListener.class);
    void displayModeChanged(BrowserDisplayMode displayMode);
}
