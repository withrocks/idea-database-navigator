package com.dci.intellij.dbn.execution.method.history.ui;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;

import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;

public class MethodExecutionHistoryGroupedTreeModel extends MethodExecutionHistoryTreeModel {
    private List<MethodExecutionInput> executionInputs;
    public MethodExecutionHistoryGroupedTreeModel(List<MethodExecutionInput> executionInputs) {
        super(executionInputs);
        this.executionInputs = executionInputs;
        for (MethodExecutionInput executionInput : executionInputs) {
            RootTreeNode rootNode = getRoot();

            ConnectionTreeNode connectionNode = rootNode.getConnectionNode(executionInput);
            SchemaTreeNode schemaNode = connectionNode.getSchemaNode(executionInput);

            DBObjectRef<DBMethod> methodRef = executionInput.getMethodRef();
            DBObjectRef parentRef = methodRef.getParentRef(DBObjectType.PROGRAM);
            if (parentRef != null) {
                ProgramTreeNode programNode = schemaNode.getProgramNode(executionInput);
                programNode.getMethodNode(executionInput);
            } else {
                schemaNode.getMethodNode(executionInput);
            }
        }
    }

    @Override
    protected String getMethodName(MethodExecutionInput executionInput) {
        return executionInput.getMethodRef().getObjectName();
    }

    @Override
    public TreePath getTreePath(MethodExecutionInput executionInput) {
        List<MethodExecutionHistoryTreeNode> path = new ArrayList<MethodExecutionHistoryTreeNode>();
        MethodExecutionHistoryTreeModel.RootTreeNode rootTreeNode = getRoot();
        path.add(rootTreeNode);
        ConnectionTreeNode connectionTreeNode = rootTreeNode.getConnectionNode(executionInput);
        path.add(connectionTreeNode);
        SchemaTreeNode schemaTreeNode = connectionTreeNode.getSchemaNode(executionInput);
        path.add(schemaTreeNode);
        if (executionInput.getMethodRef().getParentObject(DBObjectType.PROGRAM) != null) {
            ProgramTreeNode programTreeNode = schemaTreeNode.getProgramNode(executionInput);
            path.add(programTreeNode);
            MethodTreeNode methodTreeNode = programTreeNode.getMethodNode(executionInput);
            path.add(methodTreeNode);
        } else {
            MethodTreeNode methodTreeNode = schemaTreeNode.getMethodNode(executionInput);
            path.add(methodTreeNode);
        }

        return new TreePath(path.toArray());
    }

    public List<MethodExecutionInput> getExecutionInputs() {
        List<MethodExecutionInput> executionInputs = new ArrayList<MethodExecutionInput>();
        for (TreeNode connectionTreeNode : getRoot().getChildren()) {
            ConnectionTreeNode connectionNode = (ConnectionTreeNode) connectionTreeNode;
            for (TreeNode schemaTreeNode : connectionNode.getChildren()) {
                SchemaTreeNode schemaNode = (SchemaTreeNode) schemaTreeNode;
                for (TreeNode node : schemaNode.getChildren()) {
                    if (node instanceof ProgramTreeNode) {
                        ProgramTreeNode programNode = (ProgramTreeNode) node;
                        for (TreeNode methodTreeNode : programNode.getChildren()) {
                            MethodTreeNode methodNode = (MethodTreeNode) methodTreeNode;
                            MethodExecutionInput executionInput =
                                    getExecutionInput(connectionNode, schemaNode, programNode, methodNode);

                            if (executionInput != null) {
                                executionInputs.add(executionInput);
                            }
                        }

                    } else {
                        MethodTreeNode methodNode = (MethodTreeNode) node;
                        MethodExecutionInput executionInput =
                                getExecutionInput(connectionNode, schemaNode, null, methodNode);

                        if (executionInput != null) {
                            executionInputs.add(executionInput);
                        }
                    }
                }
            }
        }
        return executionInputs;
    }

    private MethodExecutionInput getExecutionInput(
            ConnectionTreeNode connectionNode,
            SchemaTreeNode schemaNode,
            ProgramTreeNode programNode,
            MethodTreeNode methodNode) {
        for (MethodExecutionInput executionInput : executionInputs) {
            DBObjectRef<DBMethod> methodRef = executionInput.getMethodRef();
            ConnectionHandler connectionHandler = FailsafeUtil.get(executionInput.getConnectionHandler());
            if (connectionHandler.getId().equals(connectionNode.getConnectionHandlerId()) &&
                methodRef.getSchemaName().equalsIgnoreCase(schemaNode.getName()) &&
                methodRef.getObjectName().equalsIgnoreCase(methodNode.getName()) &&
                methodRef.getOverload() == methodNode.getOverload() ) {

                DBObjectRef programRef = methodRef.getParentRef(DBObjectType.PROGRAM);
                if (programNode == null && programRef == null) {
                    return executionInput;
                } else if (programNode != null && programRef != null){
                    String programName = programNode.getName();
                    String inputProgramName = programRef.getObjectName();
                    if (programName.equalsIgnoreCase(inputProgramName)) {
                        return executionInput;
                    }
                }
            }
        }
        return null;
    }



}
