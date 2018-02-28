package com.dci.intellij.dbn.editor.session;

import java.util.EventListener;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.Topic;

public interface SessionBrowserLoadListener extends EventListener {
    Topic<SessionBrowserLoadListener> TOPIC = Topic.create("Sessions loaded", SessionBrowserLoadListener.class);

    void sessionsLoaded(VirtualFile virtualFile);
}
