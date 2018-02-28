package com.dci.intellij.dbn.database.common;

import com.dci.intellij.dbn.database.DatabaseExecutionInterface;
import com.dci.intellij.dbn.database.common.execution.MethodExecutionProcessor;
import com.dci.intellij.dbn.database.common.execution.SimpleFunctionExecutionProcessor;
import com.dci.intellij.dbn.database.common.execution.SimpleProcedureExecutionProcessor;
import com.dci.intellij.dbn.object.DBFunction;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBProcedure;

public abstract class DatabaseExecutionInterfaceImpl implements DatabaseExecutionInterface {

    public MethodExecutionProcessor createSimpleMethodExecutionProcessor(DBMethod method) {
        if (method instanceof DBFunction) {
            DBFunction function = (DBFunction) method;
            return new SimpleFunctionExecutionProcessor(function);
        }
        if (method instanceof DBProcedure) {
            DBProcedure procedure = (DBProcedure) method;
            return new SimpleProcedureExecutionProcessor(procedure);

        }
        return null;
    }

}
