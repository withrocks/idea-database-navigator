package com.dci.intellij.dbn.execution.method.history.ui;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.List;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.object.common.DBObjectType;

public class MethodExecutionHistoryTreeNode extends DefaultMutableTreeNode {
    public static enum Type {
        ROOT,
        CONNECTION,
        SCHEMA,
        PACKAGE,
        TYPE,
        PROCEDURE,
        FUNCTION,
        UNKNOWN
    }
    private String name;
    private Type type;

    public MethodExecutionHistoryTreeNode(MethodExecutionHistoryTreeNode parent, Type type, String name) {
        this.name = name;
        this.type = type;
        if (parent != null) {
            parent.add(this);
        }
    }
    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public Icon getIcon() {
        return
            type == Type.CONNECTION? Icons.CONNECTION_ACTIVE :
            type == Type.SCHEMA ? Icons.DBO_SCHEMA :
            type == Type.PACKAGE ? Icons.DBO_PACKAGE :
            type == Type.TYPE ? Icons.DBO_TYPE :
            type == Type.PROCEDURE ? Icons.DBO_PROCEDURE :
            type == Type.FUNCTION ? Icons.DBO_FUNCTION : null;
    }

    public static Type getNodeType(DBObjectType objectType) {
        return
            objectType == DBObjectType.SCHEMA ? Type.SCHEMA :
            objectType == DBObjectType.PACKAGE ? Type.PACKAGE :
            objectType == DBObjectType.TYPE ? Type.TYPE :
            objectType == DBObjectType.PROCEDURE ||
                    objectType == DBObjectType.PACKAGE_PROCEDURE ||
                    objectType == DBObjectType.TYPE_PROCEDURE ? Type.PROCEDURE :
            objectType == DBObjectType.FUNCTION ||
                    objectType == DBObjectType.PACKAGE_FUNCTION ||
                    objectType == DBObjectType.TYPE_FUNCTION ? Type.FUNCTION : Type.UNKNOWN;
    }

    public List<MethodExecutionHistoryTreeNode> getChildren() {
        return children;
    }

    public boolean getAllowsChildren() {
        return
            type != Type.PROCEDURE &&
            type != Type.FUNCTION;
    }

    public boolean isValid() {
        return true;
    }
}
