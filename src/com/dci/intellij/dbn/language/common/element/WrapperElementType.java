package com.dci.intellij.dbn.language.common.element;

public interface WrapperElementType extends ElementType {

    TokenElementType getBeginTokenElement();

    TokenElementType getEndTokenElement();

    ElementType getWrappedElement();

    boolean isWrappedElementOptional();

    boolean isStrong();
}
