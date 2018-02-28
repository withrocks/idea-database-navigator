package com.dci.intellij.dbn.language.common.element.path;

import com.dci.intellij.dbn.language.common.element.ElementType;

public interface PathNode {
    PathNode getParent();

    ElementType getElementType();

    PathNode getRootPathNode();

    boolean isRecursive();

    boolean isRecursive(ElementType elementType);

    void detach();

    int getIndexInParent();
}
