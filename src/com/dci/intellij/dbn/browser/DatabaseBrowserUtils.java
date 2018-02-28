package com.dci.intellij.dbn.browser;

import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.object.common.DBObjectBundle;

public class DatabaseBrowserUtils {
    @Nullable
    public static TreePath createTreePath(BrowserTreeNode treeNode) {
        boolean isTabbedMode = DatabaseBrowserManager.getInstance(treeNode.getProject()).isTabbedMode();

        int treeDepth = treeNode.getTreeDepth();
        int nodeIndex = isTabbedMode ? treeDepth - 1 : treeDepth + 1;
        if (nodeIndex < 0) {
            return null;
        }

        BrowserTreeNode[] path = new BrowserTreeNode[nodeIndex];
        while (treeNode != null) {
            treeDepth = treeNode.getTreeDepth();
            path[isTabbedMode ? treeDepth -2 : treeDepth] = treeNode;
            if (treeNode instanceof DatabaseBrowserManager) break;
            if (isTabbedMode && treeNode instanceof DBObjectBundle) break;
            treeNode = treeNode.getTreeParent();
        }
        return new TreePath(path);
    }

    public static boolean treeVisibilityChanged(
            List<BrowserTreeNode> possibleTreeNodes,
            List<BrowserTreeNode> actualTreeNodes,
            Filter<BrowserTreeNode> filter) {
        for (BrowserTreeNode treeNode : possibleTreeNodes) {
            if (treeNode != null) {
                if (filter.accepts(treeNode)) {
                    if (!actualTreeNodes.contains(treeNode)) return true;
                } else {
                    if (actualTreeNodes.contains(treeNode)) return true;
                }
            }
        }
        return false;
    }

    public static List<BrowserTreeNode> createList(BrowserTreeNode... treeNodes) {
        List<BrowserTreeNode> treeNodeList = new ArrayList<BrowserTreeNode>();
        for (BrowserTreeNode treeNode : treeNodes) {
            if (treeNode != null) {
                treeNodeList.add(treeNode);
            }
        }
        return treeNodeList;
    }
}
