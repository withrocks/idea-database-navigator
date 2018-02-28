package com.dci.intellij.dbn.ddl;

import java.util.EventListener;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.Topic;

public interface DDLMappingListener extends EventListener {
    Topic<DDLMappingListener> TOPIC = Topic.create("DDL Mappings changed", DDLMappingListener.class);

    void ddlFileDetached(VirtualFile virtualFile);

    void ddlFileAttached(VirtualFile virtualFile);
}
