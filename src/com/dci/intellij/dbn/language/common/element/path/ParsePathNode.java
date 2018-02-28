package com.dci.intellij.dbn.language.common.element.path;

import com.dci.intellij.dbn.language.common.element.ElementType;
import com.intellij.lang.PsiBuilder;

public class ParsePathNode extends BasicPathNode {
    private int startOffset;
    private int currentOffset;
    private int cursorPosition;
    private PsiBuilder.Marker elementMarker;
    private int depth;

    public ParsePathNode(ElementType elementType, ParsePathNode parent, int startOffset, int cursorPosition) {
        super(elementType, parent);
        this.startOffset = startOffset;
        this.currentOffset = startOffset;
        this.cursorPosition = cursorPosition;
        this.depth = parent == null ? 0 : parent.depth + 1;
    }

    public ParsePathNode getParent() {
        return (ParsePathNode) super.getParent();
    }

    public int getStartOffset() {
        return startOffset;
    }

    public int getCurrentOffset() {
        return currentOffset;
    }

    public void setCurrentOffset(int currentOffset) {
        this.currentOffset = currentOffset;
    }

    public int getCursorPosition() {
        return cursorPosition;
    }

    public void setCursorPosition(int cursorPosition) {
        this.cursorPosition = cursorPosition;
    }

    public boolean isRecursive() {
        ParsePathNode parseNode = this.getParent();
        while (parseNode != null) {
            if (parseNode.getElementType() == getElementType() &&
                parseNode.startOffset == startOffset) {
                return true;
            }
            parseNode = parseNode.getParent();
        }
        return false;
    }

    public boolean isRecursive(int currentOffset) {
        ParsePathNode parseNode = this.getParent();
        while (parseNode != null) {
            if (parseNode.getElementType() == getElementType() &&
                        parseNode.currentOffset == currentOffset) {
                    return true;
                }
            parseNode = parseNode.getParent();
        }
        return false;
    }

    public int incrementIndex(int builderOffset) {
        cursorPosition++;
        this.currentOffset = builderOffset;
        return cursorPosition;
    }

    public PsiBuilder.Marker getElementMarker() {
        return elementMarker;
    }

    public void setElementMarker(PsiBuilder.Marker elementMarker) {
        this.elementMarker = elementMarker;
    }

    public int getDepth() {
        return depth;
    }

    @Override
    public void detach() {
        super.detach();
        elementMarker = null;
    }

    @Override
    public boolean isSiblingOf(ParsePathNode parentNode) {
        return depth < parentNode.depth && super.isSiblingOf(parentNode);
    }
}

