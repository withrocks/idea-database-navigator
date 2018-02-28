package com.dci.intellij.dbn.database;

import java.sql.SQLException;
import org.jetbrains.annotations.Nullable;

public interface DatabaseMessageParserInterface {

    @Nullable
    DatabaseObjectIdentifier identifyObject(SQLException exception);

    boolean isTimeoutException(SQLException e);

    boolean isModelException(SQLException e);

    boolean isAuthenticationException(SQLException e);
}
