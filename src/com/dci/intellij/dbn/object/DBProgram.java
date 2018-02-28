package com.dci.intellij.dbn.object;

import java.util.List;

import com.dci.intellij.dbn.object.common.DBSchemaObject;

public interface DBProgram<P extends DBProcedure, F extends DBFunction> extends DBSchemaObject {
    List<P> getProcedures();
    List<F> getFunctions();
    F getFunction(String name, int overload);
    P getProcedure(String name, int overload);
    DBMethod getMethod(String name, int overload);
    boolean isEmbedded();
}
