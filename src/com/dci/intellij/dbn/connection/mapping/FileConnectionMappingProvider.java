package com.dci.intellij.dbn.connection.mapping;

import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.object.DBSchema;

public interface FileConnectionMappingProvider {
    @Nullable
    ConnectionHandler getActiveConnection();

    @Nullable
    DBSchema getCurrentSchema();
}
