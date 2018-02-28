package com.dci.intellij.dbn.browser.model;

import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.intellij.util.messages.Topic;

import java.util.EventListener;

public interface BrowserTreeChangeListener extends EventListener{
    Topic<BrowserTreeChangeListener> TOPIC = Topic.create("Browser tree change event", BrowserTreeChangeListener.class);

    void nodeChanged(BrowserTreeNode node, TreeEventType eventType);

}
