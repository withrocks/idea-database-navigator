package com.dci.intellij.dbn.connection;

import org.jetbrains.annotations.Nullable;

public interface ConnectionProvider {
    @Nullable
    ConnectionHandler getConnectionHandler();
}
