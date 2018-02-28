package com.dci.intellij.dbn.common.state;


public interface PersistentStateElement<T>{
    void readState(T parent);
    void writeState(T parent);
}
