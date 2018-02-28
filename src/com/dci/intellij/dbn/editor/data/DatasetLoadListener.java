package com.dci.intellij.dbn.editor.data;

import java.util.EventListener;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.Topic;

public interface DatasetLoadListener extends EventListener {
    Topic<DatasetLoadListener> TOPIC = Topic.create("Dataset loaded", DatasetLoadListener.class);

    void datasetLoaded(VirtualFile virtualFile);
}
