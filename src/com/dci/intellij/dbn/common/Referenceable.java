package com.dci.intellij.dbn.common;

public interface Referenceable<R extends Reference> {
    R getRef();
    String getName();
}
