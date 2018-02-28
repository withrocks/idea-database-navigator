package com.dci.intellij.dbn.database.common;

import com.dci.intellij.dbn.database.DatabaseDebuggerInterface;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;

public abstract class DatabaseDebuggerInterfaceImpl extends DatabaseInterfaceImpl implements DatabaseDebuggerInterface {
    public DatabaseDebuggerInterfaceImpl(String fileName, DatabaseInterfaceProvider provider) {
        super(fileName, provider);
    }


}
