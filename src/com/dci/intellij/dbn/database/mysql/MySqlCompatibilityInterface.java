package com.dci.intellij.dbn.database.mysql;

import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.data.sorting.SortDirection;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.DatabaseObjectTypeId;
import com.dci.intellij.dbn.editor.session.SessionStatus;

public class MySqlCompatibilityInterface extends DatabaseCompatibilityInterface {

    public MySqlCompatibilityInterface(DatabaseInterfaceProvider parent) {
        super(parent);
    }

    public boolean supportsObjectType(DatabaseObjectTypeId objectTypeId) {
        return
            objectTypeId == DatabaseObjectTypeId.CHARSET ||
            objectTypeId == DatabaseObjectTypeId.USER ||
            objectTypeId == DatabaseObjectTypeId.SCHEMA ||
            objectTypeId == DatabaseObjectTypeId.TABLE ||
            objectTypeId == DatabaseObjectTypeId.VIEW ||
            objectTypeId == DatabaseObjectTypeId.COLUMN ||
            objectTypeId == DatabaseObjectTypeId.CONSTRAINT ||
            objectTypeId == DatabaseObjectTypeId.INDEX ||
            objectTypeId == DatabaseObjectTypeId.DATASET_TRIGGER ||
            objectTypeId == DatabaseObjectTypeId.FUNCTION ||
            objectTypeId == DatabaseObjectTypeId.PROCEDURE ||
            objectTypeId == DatabaseObjectTypeId.ARGUMENT ||
            objectTypeId == DatabaseObjectTypeId.SYSTEM_PRIVILEGE ||
            objectTypeId == DatabaseObjectTypeId.GRANTED_PRIVILEGE;
    }

    public boolean supportsFeature(DatabaseFeature feature) {
        switch (feature) {
            case SESSION_BROWSING: return true;
            case SESSION_KILL: return true;
            case OBJECT_CHANGE_TRACING: return true;
            default: return false;
        }
    }

    public char getIdentifierQuotes() {
        return '`';
    }

    @Override
    public String getDefaultAlternativeStatementDelimiter() {
        return "$$";
    }

    @Override
    public String getOrderByClause(String columnName, SortDirection sortDirection, boolean nullsFirst) {
        nullsFirst = (nullsFirst && sortDirection == SortDirection.ASCENDING) || (!nullsFirst && sortDirection == SortDirection.DESCENDING);
        return "(" + columnName + " is" + (nullsFirst ? "" : " not") + " null), " + columnName + " " + sortDirection.getSqlToken();
    }

    @Override
    public String getExplainPlanStatementPrefix() {
        return null;
    }

    @Override
    public String getSessionBrowserColumnName(String columnName) {
        if (columnName.equalsIgnoreCase("id")) return "SESSION_ID";
        if (columnName.equalsIgnoreCase("user")) return "USER";
        if (columnName.equalsIgnoreCase("host")) return "HOST";
        if (columnName.equalsIgnoreCase("db")) return "DATABASE";
        if (columnName.equalsIgnoreCase("command")) return "COMMAND";
        if (columnName.equalsIgnoreCase("time")) return "TIME";
        if (columnName.equalsIgnoreCase("state")) return "STATUS";
        if (columnName.equalsIgnoreCase("info")) return "CLIENT_INFO";
        return super.getSessionBrowserColumnName(columnName);
    }

    @Override
    public SessionStatus getSessionStatus(String statusName) {
        if (StringUtil.isEmpty(statusName)) return SessionStatus.INACTIVE;
        else return SessionStatus.ACTIVE;
    }
}
