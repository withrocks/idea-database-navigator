package com.dci.intellij.dbn.editor.code;

import java.util.EventListener;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.Topic;

public interface SourceCodeLoadListener extends EventListener {
    Topic<SourceCodeLoadListener> TOPIC = Topic.create("Source Code loaded", SourceCodeLoadListener.class);

    void sourceCodeLoaded(VirtualFile virtualFile);
}
