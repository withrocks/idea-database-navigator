package com.dci.intellij.dbn.database;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.sorting.SortDirection;
import com.dci.intellij.dbn.editor.session.SessionStatus;
import com.dci.intellij.dbn.object.common.DBObject;

public abstract class DatabaseCompatibilityInterface {
    private DatabaseInterfaceProvider provider;

    public DatabaseCompatibilityInterface(DatabaseInterfaceProvider parent) {
        this.provider = parent;
    }

    @NotNull
    public static DatabaseCompatibilityInterface getInstance(DBObject object) {
        ConnectionHandler connectionHandler = FailsafeUtil.get(object.getConnectionHandler());
        return getInstance(connectionHandler);
    }

    public static DatabaseCompatibilityInterface getInstance(ConnectionHandler connectionHandler) {
        return connectionHandler.getInterfaceProvider().getCompatibilityInterface();
    }

    public abstract boolean supportsObjectType(DatabaseObjectTypeId objectTypeId);

    public abstract boolean supportsFeature(DatabaseFeature feature);

    public abstract char getIdentifierQuotes();

    @Nullable
    public String getDatabaseLogName() {
        return null;
    }

    public abstract String getDefaultAlternativeStatementDelimiter();

    public String getOrderByClause(String columnName, SortDirection sortDirection, boolean nullsFirst) {
        return columnName + " " + sortDirection.getSqlToken() + " nulls " + (nullsFirst ? " first" : " last");
    }

    public String getSessionBrowserColumnName(String columnName) {
        return columnName;
    }

    public abstract SessionStatus getSessionStatus(String statusName);

    public abstract String getExplainPlanStatementPrefix();
}
