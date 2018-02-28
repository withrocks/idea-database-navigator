package com.dci.intellij.dbn.debugger.frame;

import javax.swing.Icon;
import java.sql.SQLException;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.database.common.debug.VariableInfo;
import com.dci.intellij.dbn.debugger.DBProgramDebugProcess;
import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XNamedValue;
import com.intellij.xdebugger.frame.XValueChildrenList;
import com.intellij.xdebugger.frame.XValueModifier;
import com.intellij.xdebugger.frame.XValueNode;
import com.intellij.xdebugger.frame.XValuePlace;

public class DBProgramDebugValue extends XNamedValue implements Comparable<DBProgramDebugValue>{
    private DBProgramDebugValueModifier modifier;
    private DBProgramDebugProcess debugProcess;
    private String value;
    private String errorMessage;
    private Icon icon;
    private int frameIndex;
    private Set<String> childVariableNames;

    public DBProgramDebugValue(DBProgramDebugProcess debugProcess, DBProgramDebugValue parentValue, String variableName, @Nullable Set<String> childVariableNames, Icon icon, int frameIndex) {
        super(variableName);
        this.debugProcess = debugProcess;
        if (icon == null) {
            if (parentValue == null) {
                icon = Icons.DBO_VARIABLE;
            } else {
                icon = Icons.DBO_ATTRIBUTE;
            }
        }
        this.icon = icon;

        this.frameIndex = frameIndex;
        this.childVariableNames = childVariableNames;
        try {
            String databaseVariableName = parentValue == null ? variableName : parentValue.getVariableName() + "." + variableName;
            VariableInfo variableInfo = debugProcess.getDebuggerInterface().getVariableInfo(
                    databaseVariableName.toUpperCase(), frameIndex,
                    debugProcess.getDebugConnection());
            value = variableInfo.getValue();
            errorMessage = variableInfo.getError();
            if (childVariableNames != null) {
                errorMessage = null;
            }

            if (value == null) {
                value = childVariableNames != null ? "" : "null";
            } else {
                if (!StringUtil.isNumber(value)) {
                    value = '"' + value + '"';
                }
            }

            if (errorMessage != null) {
                errorMessage = errorMessage.toLowerCase();
            }
            if (childVariableNames != null) {
                errorMessage = "record";
            }
        } catch (SQLException e) {
            value = "";
            errorMessage = e.getMessage();
        }
    }

    public DBProgramDebugProcess getDebugProcess() {
        return debugProcess;
    }

    public String getVariableName() {
        return getName();
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getValue() {
        return value;
    }

    @Override
    public void computePresentation(@NotNull XValueNode node, @NotNull XValuePlace place) {
        node.setPresentation(icon, errorMessage, value, childVariableNames != null);
    }

    @Override
    public XValueModifier getModifier() {
        if (modifier == null) modifier = new DBProgramDebugValueModifier(this);
        return modifier;
    }

    public int compareTo(@NotNull DBProgramDebugValue remote) {
        return getName().compareTo(remote.getName());
    }

    @Override
    public void computeChildren(@NotNull XCompositeNode node) {
        if (childVariableNames != null) {
            for (String childVariableName : childVariableNames) {
                childVariableName = childVariableName.substring(getVariableName().length() + 1);
                XValueChildrenList debugValueChildren = new XValueChildrenList();
                DBProgramDebugValue value = new DBProgramDebugValue(debugProcess, this, childVariableName, null, null, frameIndex);
                debugValueChildren.add(value);
                node.addChildren(debugValueChildren, true);
            }
        } else {
            super.computeChildren(node);
        }

    }

/*    private List<DBObject> getChildObjects() {
        DBObject object = DBObjectRef.get(objectRef);
        if (object instanceof DBVirtualObject) {
            DBObjectListContainer childObjectsContainer = object.getChildObjects();
            if (childObjectsContainer != null) {
                List<DBObjectList<DBObject>> objectLists = childObjectsContainer.getAllObjectLists();
                if (objectLists.size() > 0) {
                    return objectLists.get(0).getObjects();
                }
            }
        }
        return null;
    }*/
}
