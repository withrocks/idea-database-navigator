package com.dci.intellij.dbn.database;

import com.dci.intellij.dbn.database.common.execution.MethodExecutionProcessor;
import com.dci.intellij.dbn.object.DBMethod;

public interface DatabaseExecutionInterface {
    MethodExecutionProcessor createExecutionProcessor(DBMethod method);
    MethodExecutionProcessor createDebugExecutionProcessor(DBMethod method);
}
