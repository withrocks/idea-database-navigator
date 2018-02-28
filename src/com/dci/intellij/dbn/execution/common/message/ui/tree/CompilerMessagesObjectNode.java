package com.dci.intellij.dbn.execution.common.message.ui.tree;

import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.common.ui.tree.TreeUtil;
import com.dci.intellij.dbn.execution.compiler.CompilerMessage;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.vfs.DBEditableObjectVirtualFile;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.TreePath;
import java.util.List;

public class CompilerMessagesObjectNode extends BundleTreeNode {
    private DBObjectRef<DBSchemaObject> objectRef;

    public CompilerMessagesObjectNode(CompilerMessagesNode parent, DBObjectRef<DBSchemaObject> objectRef) {
        super(parent);
        this.objectRef = objectRef;
    }

    @Nullable
    public DBEditableObjectVirtualFile getVirtualFile() {
        DBSchemaObject object = getObject();
        return object == null ? null : object.getVirtualFile();
    }

    @Nullable
    public DBSchemaObject getObject() {
        return DBObjectRef.get(objectRef);
    }

    public DBObjectRef<DBSchemaObject> getObjectRef() {
        return objectRef;
    }

    public TreePath addCompilerMessage(CompilerMessage compilerMessage) {
        List<MessagesTreeNode> children = getChildren();
        if (children.size() > 0) {
            CompilerMessageNode firstChild = (CompilerMessageNode) children.get(0);
            if (firstChild.getCompilerMessage().getCompilerResult() != compilerMessage.getCompilerResult()) {
                clearChildren();
            }
        }
        CompilerMessageNode messageNode = new CompilerMessageNode(this, compilerMessage);
        addChild(messageNode);

        getTreeModel().notifyTreeModelListeners(this, TreeEventType.STRUCTURE_CHANGED);
        return TreeUtil.createTreePath(messageNode);
    }

    @Nullable
    public TreePath getTreePath(CompilerMessage compilerMessage) {
        for (MessagesTreeNode messageNode : getChildren()) {
            CompilerMessageNode compilerMessageNode = (CompilerMessageNode) messageNode;
            if (compilerMessageNode.getCompilerMessage() == compilerMessage) {
                return TreeUtil.createTreePath(compilerMessageNode);
            }
        }
        return null;
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
