package com.dci.intellij.dbn.database;

import java.sql.SQLException;

public interface DatabaseInterface {
    SQLException DBN_INTERRUPTED_EXCEPTION = new SQLException("DBN_INTERRUPTED_EXCEPTION");
    SQLException DBN_NOT_CONNECTED_EXCEPTION = new SQLException("DBN_NOT_CONNECTED_EXCEPTION");
    void reset();
}
