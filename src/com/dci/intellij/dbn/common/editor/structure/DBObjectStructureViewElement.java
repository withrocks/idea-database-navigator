package com.dci.intellij.dbn.common.editor.structure;

import javax.swing.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.object.common.DBObject;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.editor.colors.TextAttributesKey;

public abstract class DBObjectStructureViewElement<T extends DBObject> implements StructureViewTreeElement, Comparable{
    private T object;
    protected DBObjectStructureViewElement[] children;
    private StructureViewTreeElement treeParent;

    public DBObjectStructureViewElement(StructureViewTreeElement treeParent, T object) {
        this.treeParent = treeParent;
        this.object = object;
    }

    public StructureViewTreeElement getTreeParent() {
        return treeParent;
    }

    public Object getValue() {
        return this;
    }

    public T getObject() {
        return object;
    }

    @NotNull
    public ItemPresentation getPresentation() {
        return new ItemPresentation() {
            public String getPresentableText() {
                return object.getPresentableText();
            }

            @Nullable
            public String getLocationString() {
                return null;
            }

            @Nullable
            public Icon getIcon(boolean open) {
                return object.getIcon(0);
            }

            @Nullable
            public TextAttributesKey getTextAttributesKey() {
                return null;
            }
        };
    }

    public boolean canNavigate() {
        return false;
    }

    public boolean canNavigateToSource() {
        return false;
    }

    @NotNull
    public TreeElement[] getChildren() {
        return EMPTY_ARRAY;
    }

    public void navigate(boolean requestFocus) {

    }

    public int compareTo(@NotNull Object o) {
        if (o instanceof DBObject) {
            DBObject remote = (DBObject) o;
            return object.compareTo(remote);
        }
        return 0;
    }

    public void reset() {
        if (children != null) {
            for (DBObjectStructureViewElement child : children) {
                child.reset();
            }
            children = null;
        }
    }
}
