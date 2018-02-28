package com.dci.intellij.dbn.browser.model;

import javax.swing.Icon;
import java.util.List;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.code.sql.color.SQLTextAttributesKeys;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.load.LoadIcon;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.GenericDatabaseElement;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FileStatus;

public class LoadInProgressTreeNode implements BrowserTreeNode {
    public static final LoadInProgressTreeNode LOOSE_INSTANCE = new LoadInProgressTreeNode(null);

    private BrowserTreeNode parent;
    private boolean disposed;

    public LoadInProgressTreeNode(BrowserTreeNode parent) {
        this.parent = parent;
    }

    public boolean isTreeStructureLoaded() {
        return true;
    }

    public void initTreeElement() {}

    public boolean canExpand() {
        return false;
    }

    public int getTreeDepth() {
        return parent == null ? 0 : parent.getTreeDepth() + 1;
    }

    public BrowserTreeNode getTreeChild(int index) {
        return null;
    }

    public BrowserTreeNode getTreeParent() {
        return parent;
    }

    public List<? extends BrowserTreeNode> getTreeChildren() {
        return null;
    }

    @Override
    public void refreshTreeChildren(@Nullable DBObjectType objectType) {}

    public void rebuildTreeChildren() {}

    public int getTreeChildCount() {
        return 0;
    }

    public boolean isLeafTreeElement() {
        return true;
    }

    public int getIndexOfTreeChild(BrowserTreeNode child) {
        return -1;
    }

    public Icon getIcon(int flags) {
        return LoadIcon.INSTANCE;
    }
    public String getPresentableText() {
        return "Loading...";
    }

    public String getPresentableTextDetails() {
        return null;
    }

    public String getPresentableTextConditionalDetails() {
        return null;
    }

    @Nullable
    public ConnectionHandler getConnectionHandler() {
        return parent.getConnectionHandler();
    }

    public Project getProject() {
        return parent.getProject();
    }

    public GenericDatabaseElement getUndisposedElement() {
        return this;
    }

    public DynamicContent getDynamicContent(DynamicContentType dynamicContentType) {
        return null;
    }

    public boolean isDisposed() {
        return disposed || parent.isDisposed();
    }

    /*********************************************************
    *                    ItemPresentation                    *
    *********************************************************/
    public String getLocationString() {
        return null;
    }

    public Icon getIcon(boolean open) {
        return null;
    }

    public TextAttributesKey getTextAttributesKey() {
        return SQLTextAttributesKeys.IDENTIFIER;
    }

    /*********************************************************
    *                    NavigationItem                      *
    *********************************************************/
    public void navigate(boolean requestFocus) {}
    public boolean canNavigate() { return false;}
    public boolean canNavigateToSource() {return false;}

    public String getName() {
        return null;
    }

    public ItemPresentation getPresentation() {
        return this;
    }

    public FileStatus getFileStatus() {
        return FileStatus.NOT_CHANGED;
    }

    /*********************************************************
    *                    ToolTipProvider                    *
    *********************************************************/
    public String getToolTip() {
        return null;
    }

    public void dispose() {
        disposed = true;
        parent = null;
    }
}
