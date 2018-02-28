package com.dci.intellij.dbn.connection.config.ui;

import javax.swing.*;
import java.awt.*;
import java.util.EventListener;

import com.dci.intellij.dbn.connection.DatabaseType;
import com.intellij.util.messages.Topic;

public interface ConnectionPresentationChangeListener extends EventListener {
    Topic<ConnectionPresentationChangeListener> TOPIC = Topic.create("Connection presentation changed", ConnectionPresentationChangeListener.class);
    void presentationChanged(String name, Icon icon, Color color, String connectionId, DatabaseType databaseType);
}
