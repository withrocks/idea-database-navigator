package com.dci.intellij.dbn.object.common.operation;

import java.sql.SQLException;

public interface DBOperationExecutor {
    void executeOperation(DBOperationType operationType) throws SQLException, DBOperationNotSupportedException;      
}
