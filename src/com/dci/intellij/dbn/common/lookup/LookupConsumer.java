package com.dci.intellij.dbn.common.lookup;

import java.util.Collection;

public interface LookupConsumer {
    void consume(Object object) throws ConsumerStoppedException;
    void consume(Object[] array) throws ConsumerStoppedException;
    void consume(Collection objects) throws ConsumerStoppedException;
    void check() throws ConsumerStoppedException;
}
