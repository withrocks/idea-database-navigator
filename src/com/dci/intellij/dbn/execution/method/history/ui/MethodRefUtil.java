package com.dci.intellij.dbn.execution.method.history.ui;

import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBProgram;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;

@Deprecated
public class MethodRefUtil {
    public static DBProgram getProgram(DBObjectRef<DBMethod> methodRef) {
        return (DBProgram) methodRef.getParentObject(DBObjectType.PROGRAM);
    }

    public static String getProgramName(DBObjectRef<DBMethod> methodRef) {
        DBObjectRef programRef = methodRef.getParentRef(DBObjectType.PROGRAM);
        return programRef == null ? null : programRef.getObjectName();
    }

    public static DBObjectType getProgramObjectType(DBObjectRef<DBMethod> methodRef) {
        DBObjectRef programRef = methodRef.getParentRef(DBObjectType.PROGRAM);
        return programRef == null ? null : programRef.getObjectType();
    }
}
