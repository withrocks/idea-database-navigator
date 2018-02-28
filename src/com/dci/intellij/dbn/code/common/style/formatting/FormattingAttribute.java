package com.dci.intellij.dbn.code.common.style.formatting;

public interface FormattingAttribute<T> {
    T getValue();

    static abstract class Loader<T> {
        abstract T load();
    }
}
