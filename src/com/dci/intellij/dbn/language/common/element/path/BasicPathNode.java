package com.dci.intellij.dbn.language.common.element.path;

import java.util.ArrayList;
import java.util.List;

import com.dci.intellij.dbn.common.list.ReversedList;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.NamedElementType;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;

public class BasicPathNode implements PathNode {
    private PathNode parent;
    private ElementType elementType;

    public BasicPathNode(ElementType elementType, PathNode parent) {
        this.elementType = elementType;
        this.parent = parent;
    }

    public PathNode getParent() {
        return parent;
    }

    public void setParent(PathNode parent) {
        this.parent = parent;
    }

    public ElementType getElementType() {
        return elementType;
    }

    public PathNode getRootPathNode() {
        PathNode pathNode = parent;
        while (pathNode != null) {
            PathNode parentPathNode = pathNode.getParent();
            if (parentPathNode == null) {
                return pathNode;
            }
            pathNode = parentPathNode;
        }
        return this;
    }

    public PathNode getPathNode(ElementTypeAttribute attribute) {
        PathNode pathNode = this;
        while (pathNode != null) {
            if (pathNode.getElementType().is(attribute)) {
                return pathNode;
            }
            pathNode = pathNode.getParent();
        }
        return null;

    }

    public void setElementType(ElementType elementType) {
        this.elementType = elementType;
    }

    public boolean isRecursive() {
        PathNode node = this.getParent();
        while (node != null) {
            if (node.getElementType() == elementType) {
                return true;
            }
            node = node.getParent();
        }
        return false;
    }

    public boolean isRecursive(ElementType elementType) {
        if (this.elementType == elementType) {
            return true;
        }
        PathNode node = this.getParent();
        while (node != null) {
            if (node.getElementType() == elementType) {
                return true;
            }
            node = node.getParent();
        }
        return false;
    }

    @Override
    public int getIndexInParent() {
        return elementType.getIndexInParent(this);
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        PathNode parent = this;
        while (parent != null) {
            buffer.insert(0, '/');
            buffer.insert(0, parent.getElementType().getId());
            parent = parent.getParent();
        }
        return buffer.toString();
    }

    public void detach() {
        parent = null;
    }

    public boolean isSiblingOf(ParsePathNode parentNode) {
        PathNode parent = getParent();
        while (parent != null) {
            if (parent == parentNode) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }

    public static PathNode buildPathUp(ElementType elementType) {
        List<ElementType> path = new ArrayList<ElementType>();
        while (elementType != null) {
            path.add(elementType);
            if (elementType instanceof NamedElementType) break;
            elementType = elementType.getParent();
        }

        PathNode pathNode = null;
        ReversedList<ElementType> reversedPath = ReversedList.get(path);
        for (ElementType pathElement : reversedPath) {
            pathNode = new BasicPathNode(pathElement, pathNode);
        }
        return pathNode;
    }
}
