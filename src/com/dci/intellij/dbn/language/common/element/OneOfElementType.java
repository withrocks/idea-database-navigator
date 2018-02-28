package com.dci.intellij.dbn.language.common.element;

import com.dci.intellij.dbn.language.common.element.impl.ElementTypeRef;

public interface OneOfElementType extends ElementType {

    void sort();

    ElementTypeRef[] getChildren();

    ElementTypeRef getFirstChild();
}
