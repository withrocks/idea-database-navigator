package com.dci.intellij.dbn.browser.model;

import javax.swing.Icon;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.code.sql.color.SQLTextAttributesKeys;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.GenericDatabaseElement;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FileStatus;

public class SimpleBrowserTreeRoot implements BrowserTreeNode {
    private List<ConnectionBundle> rootChildren;
    private Project project;

    public SimpleBrowserTreeRoot(Project project, ConnectionBundle connectionBundle) {
        this.project = project;
        this.rootChildren = new ArrayList<ConnectionBundle>();
        this.rootChildren.add(connectionBundle);
    }

    public Project getProject() {
        return project;
    }

    /**************************************************
     *              BrowserTreeNode            *
     **************************************************/
    public boolean isTreeStructureLoaded() {
        return true;
    }

    public void initTreeElement() {}

    public boolean canExpand() {
        return true;
    }

    public int getTreeDepth() {
        return 0;
    }

    public BrowserTreeNode getTreeParent() {
        return null;
    }

    public List<? extends BrowserTreeNode> getTreeChildren() {
        return rootChildren;
    }

    @Override
    public void refreshTreeChildren(@Nullable DBObjectType objectType) {
        for (ConnectionBundle connectionBundle : rootChildren) {
            connectionBundle.refreshTreeChildren(objectType);
        }
    }

    public void rebuildTreeChildren() {
        for (ConnectionBundle connectionBundle : rootChildren) {
            connectionBundle.rebuildTreeChildren();
        }
    }

    public BrowserTreeNode getTreeChild(int index) {
        return rootChildren.get(index);
    }

    public int getTreeChildCount() {
        return rootChildren.size();
    }

    public boolean isLeafTreeElement() {
        return false;
    }

    public int getIndexOfTreeChild(BrowserTreeNode child) {
        return rootChildren.indexOf(child);
    }

    public Icon getIcon(int flags) {
        return Icons.WINDOW_DATABASE_BROWSER;
    }

    public String getPresentableText() {
        return "Connection Managers";
    }

    public String getPresentableTextDetails() {
        return null;
    }

    public String getPresentableTextConditionalDetails() {
        return null;
    }

    /**************************************************
     *              GenericDatabaseElement            *
     **************************************************/
    @Nullable
    public ConnectionHandler getConnectionHandler() {
        return null;
    }

    public GenericDatabaseElement getUndisposedElement() {
        return this;
    }

    public DynamicContent getDynamicContent(DynamicContentType dynamicContentType) {
        return null;
    }

    public boolean isDisposed() {
        return false;
    }

   /*********************************************************
    *                    ToolTipProvider                    *
    *********************************************************/
    public String getToolTip() {
        return null;
    }

    /*********************************************************
     *                  NavigationItem                       *
     *********************************************************/
    public String getName() {
        return getPresentableText();
    }
    
    public void navigate(boolean b) {}

    public boolean canNavigate() {
        return false;
    }

    public boolean canNavigateToSource() {
        return false;
    }

    public ItemPresentation getPresentation() {
        return this;
    }

    public FileStatus getFileStatus() {
        return FileStatus.NOT_CHANGED;
    }

    /*********************************************************
     *                 ItemPresentation                      *
     *********************************************************/
    public String getLocationString() {
        return null;
    }

    public Icon getIcon(boolean open) {
        return getIcon(0);
    }

    public TextAttributesKey getTextAttributesKey() {
        return SQLTextAttributesKeys.IDENTIFIER;
    }

    /*********************************************************
     *                       Disposable                      *
     *********************************************************/
    public void dispose() {
        rootChildren = null;
    }
}
