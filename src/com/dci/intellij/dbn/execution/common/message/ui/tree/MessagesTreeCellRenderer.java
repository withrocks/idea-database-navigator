package com.dci.intellij.dbn.execution.common.message.ui.tree;

import javax.swing.Icon;
import javax.swing.JTree;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.common.util.VirtualFileUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.execution.compiler.CompilerMessage;
import com.dci.intellij.dbn.execution.explain.result.ExplainPlanMessage;
import com.dci.intellij.dbn.execution.statement.StatementExecutionMessage;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;

public class MessagesTreeCellRenderer extends ColoredTreeCellRenderer {
    public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        if (value instanceof StatementExecutionMessagesNode) {
            BundleTreeNode node = (BundleTreeNode) value;
            append("Statement Execution Messages", SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
            append(" (" + node.getChildCount() + " files)", SimpleTextAttributes.GRAY_ATTRIBUTES);
        }
        else if (value instanceof ExplainPlanMessagesNode) {
            BundleTreeNode node = (BundleTreeNode) value;
            append("Explain Plan Messages", SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
            append(" (" + node.getChildCount() + " files)", SimpleTextAttributes.GRAY_ATTRIBUTES);
        }
        else if (value instanceof CompilerMessagesNode) {
            BundleTreeNode node = (BundleTreeNode) value;
            append("Compiler Messages", SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
            append(" (" + node.getChildCount() + " objects)", SimpleTextAttributes.GRAY_ATTRIBUTES);
            }
        else if (value instanceof StatementExecutionMessagesFileNode){
            StatementExecutionMessagesFileNode node = (StatementExecutionMessagesFileNode) value;
            VirtualFile virtualFile = node.getVirtualFile();

            setIcon(VirtualFileUtil.getIcon(virtualFile));
            append(virtualFile.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
            append(" (" + virtualFile.getPath() + ")", SimpleTextAttributes.GRAY_ATTRIBUTES);
        }
        else if (value instanceof ExplainPlanMessagesFileNode) {
            ExplainPlanMessagesFileNode node = (ExplainPlanMessagesFileNode) value;
            VirtualFile virtualFile = node.getVirtualFile();

            setIcon(VirtualFileUtil.getIcon(virtualFile));
            append(virtualFile.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
            append(" (" + virtualFile.getPath() + ")", SimpleTextAttributes.GRAY_ATTRIBUTES);

        }
        else if (value instanceof CompilerMessagesObjectNode){
            CompilerMessagesObjectNode compilerMessagesObjectNode = (CompilerMessagesObjectNode) value;
            DBSchemaObject object = compilerMessagesObjectNode.getObject();

            ConnectionHandler connectionHandler;
            if (object == null) {
                DBObjectRef<DBSchemaObject> objectRef = compilerMessagesObjectNode.getObjectRef();
                setIcon(objectRef.getObjectType().getIcon());
                append(objectRef.getPath(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
                connectionHandler = objectRef.lookupConnectionHandler();
            } else {
                setIcon(object.getOriginalIcon());
                append(object.getQualifiedName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
                connectionHandler = object.getConnectionHandler();
            }

            if (connectionHandler != null) {
                append(" - " + connectionHandler.getPresentableText(), SimpleTextAttributes.GRAY_ATTRIBUTES);
            }
        }
        else if (value instanceof CompilerMessageNode) {
            CompilerMessageNode node = (CompilerMessageNode) value;
            CompilerMessage message = node.getCompilerMessage();
            append(message.getText(), SimpleTextAttributes.REGULAR_ATTRIBUTES);

            MessageType messageType = message.getType();
            Icon icon =
                    messageType == MessageType.ERROR ? Icons.EXEC_MESSAGES_ERROR :
                    messageType == MessageType.WARNING ? Icons.EXEC_MESSAGES_WARNING_INACTIVE :
                    messageType == MessageType.INFO ? Icons.EXEC_MESSAGES_INFO : null;

            append(" (line " + message.getLine() + " / position " + message.getPosition() + ")", SimpleTextAttributes.GRAY_ATTRIBUTES);
            setIcon(icon);
        }
        else if (value instanceof StatementExecutionMessageNode) {
            StatementExecutionMessageNode execMessageNode = (StatementExecutionMessageNode) value;
            StatementExecutionMessage message = execMessageNode.getExecutionMessage();
            boolean isOrphan = message.isOrphan();

            MessageType messageType = message.getType();
            Icon icon =
                    messageType == MessageType.ERROR ? (isOrphan ? Icons.EXEC_MESSAGES_ERROR_INACTIVE : Icons.EXEC_MESSAGES_ERROR) :
                    messageType == MessageType.WARNING ? (isOrphan ? Icons.EXEC_MESSAGES_WARNING_INACTIVE : Icons.EXEC_MESSAGES_WARNING) :
                    messageType == MessageType.INFO ? (isOrphan ? Icons.EXEC_MESSAGES_INFO_INACTIVE : Icons.EXEC_MESSAGES_INFO) : null;

            setIcon(icon);

            append(message.getText(), isOrphan ?
                    SimpleTextAttributes.GRAY_ATTRIBUTES :
                    SimpleTextAttributes.REGULAR_ATTRIBUTES);

            if (message.getCauseMessage() != null) {
                append(" " + message.getCauseMessage(), isOrphan ?
                        SimpleTextAttributes.GRAY_ATTRIBUTES :
                        SimpleTextAttributes.ERROR_ATTRIBUTES);
            }

            ConnectionHandler connectionHandler = message.getExecutionResult().getConnectionHandler();
            if (connectionHandler != null) {
                append(" - Connection: " + connectionHandler.getName() + ": " + message.getExecutionResult().getExecutionDuration() + "ms", isOrphan ?
                        SimpleTextAttributes.GRAY_ATTRIBUTES :
                        SimpleTextAttributes.GRAY_ATTRIBUTES);
            }
        }
        else if (value instanceof ExplainPlanMessageNode) {
            ExplainPlanMessageNode explainPlanMessageNode = (ExplainPlanMessageNode) value;
            ExplainPlanMessage message = explainPlanMessageNode.getExplainPlanMessage();
            MessageType messageType = message.getType();
            Icon icon =
                    messageType == MessageType.ERROR ? Icons.EXEC_MESSAGES_ERROR :
                    messageType == MessageType.WARNING ? Icons.EXEC_MESSAGES_WARNING_INACTIVE :
                    messageType == MessageType.INFO ? Icons.EXEC_MESSAGES_INFO : null;

            setIcon(icon);

            append(message.getText(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
            ConnectionHandler connectionHandler = message.getConnectionHandler();
            if (connectionHandler != null) {
                append(" - Connection: " + connectionHandler.getName(), SimpleTextAttributes.GRAY_ATTRIBUTES);
            }
        }
    }

}