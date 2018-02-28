package com.dci.intellij.dbn.database.common;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.connection.ConnectionUtil;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.database.common.logging.ExecutionLogOutput;
import com.intellij.openapi.diagnostic.Logger;

public abstract class DatabaseMetadataInterfaceImpl extends DatabaseInterfaceImpl implements DatabaseMetadataInterface {
    private Logger logger = Logger.getInstance(getClass().getName());

    public DatabaseMetadataInterfaceImpl(String fileName, DatabaseInterfaceProvider provider) {
        super(fileName, provider);
    }

    protected void executeStatement(Connection connection, String statementText) throws SQLException {
        if (SettingsUtil.isDebugEnabled)
            logger.info("[DBN-INFO] Executing statement: " + statementText);

        Statement statement = connection.createStatement();
        statement.setQueryTimeout(20);
        try {
            statement.execute(statementText);
            ConnectionUtil.closeStatement(statement);
        } catch (SQLException exception) {
            if (SettingsUtil.isDebugEnabled)
                logger.info("[DBN-ERROR] Error executing statement: " + statementText +
                                "\nCause: " + exception.getMessage());
            throw exception;
        }
    }

    public ResultSet getDistinctValues(String ownerName, String datasetName, String columnName, Connection connection) throws SQLException {
        return executeQuery(connection, "load-distinct-values", ownerName, datasetName, columnName);
    }

    public void setCurrentSchema(String schemaName, Connection connection) throws SQLException {
        executeQuery(connection, "set-current-schema", schemaName);
    }

    public ResultSet loadUsers(Connection connection) throws SQLException {
        return executeQuery(connection, "users");
    }

    public ResultSet loadRoles(Connection connection) throws SQLException {
        return executeQuery(connection, "roles");
    }

    public ResultSet loadAllUserRoles(Connection connection) throws SQLException {
        return executeQuery(connection, "all-user-roles");
    }


    public ResultSet loadSystemPrivileges(Connection connection) throws SQLException {
        return executeQuery(connection, "system-privileges");
    }

    public ResultSet loadObjectPrivileges(Connection connection) throws SQLException {
        return executeQuery(connection, "object-privileges");
    }

    public ResultSet loadAllUserPrivileges(Connection connection) throws SQLException {
        return executeQuery(connection, "all-user-privileges");
    }

    public ResultSet loadAllRolePrivileges(Connection connection) throws SQLException {
        return executeQuery(connection, "all-role-privileges");
    }

    public ResultSet loadAllRoleRoles(Connection connection) throws SQLException {
        return executeQuery(connection, "all-role-roles");
    }

    public ResultSet loadSchemas(Connection connection) throws SQLException {
        return executeQuery(connection, "schemas");
    }

    public ResultSet loadClusters(String ownerName, Connection connection) throws SQLException {
        return executeQuery(connection, "clusters", ownerName);
    }

    public ResultSet loadTables(String ownerName, Connection connection) throws SQLException {
        return executeQuery(connection, "tables", ownerName);
    }

    public ResultSet loadViews(String ownerName, Connection connection) throws SQLException {
        return executeQuery(connection, "views", ownerName);
    }

    public ResultSet loadMaterializedViews(String ownerName, Connection connection) throws SQLException {
        return executeQuery(connection, "materialized-views", ownerName);
    }

    public ResultSet loadColumns(String ownerName, String datasetName, Connection connection) throws SQLException {
        return executeQuery(connection, "dataset-columns", ownerName, datasetName);
    }

    public ResultSet loadAllColumns(String ownerName, Connection connection) throws SQLException {
        return executeQuery(connection, "all-dataset-columns", ownerName);
    }

    public ResultSet loadConstraintRelations(String ownerName, String datasetName, Connection connection) throws SQLException {
        return executeQuery(connection, "column-constraint-relations", ownerName, datasetName);
    }

    public ResultSet loadAllConstraintRelations(String ownerName, Connection connection) throws SQLException {
        return executeQuery(connection, "all-column-constraint-relations", ownerName);
    }

    public ResultSet loadIndexRelations(String ownerName, String tableName, Connection connection) throws SQLException {
        return executeQuery(connection, "column-index-relations", ownerName, tableName);
    }

    public ResultSet loadAllIndexRelations(String ownerName, Connection connection) throws SQLException {
        return executeQuery(connection, "all-column-index-relations", ownerName);
    }

    public ResultSet loadConstraints(String ownerName, String datasetName, Connection connection) throws SQLException {
        return executeQuery(connection, "constraints", ownerName, datasetName);
    }

    public ResultSet loadAllConstraints(String ownerName, Connection connection) throws SQLException {
        return executeQuery(connection, "all-constraints", ownerName);
    }

    public ResultSet loadIndexes(String ownerName, String tableName, Connection connection) throws SQLException {
        return executeQuery(connection, "indexes", ownerName, tableName);
    }

    public ResultSet loadAllIndexes(String ownerName, Connection connection) throws SQLException {
        return executeQuery(connection, "all-indexes", ownerName);
    }

    public ResultSet loadNestedTables(String ownerName, String tableName, Connection connection) throws SQLException {
        return executeQuery(connection, "nested-tables", ownerName, tableName);
    }

    public ResultSet loadAllNestedTables(String ownerName, Connection connection) throws SQLException {
        return executeQuery(connection, "all-nested-tables", ownerName);
    }

    @Override
    public ResultSet loadDatabaseTriggers(String ownerName, Connection connection) throws SQLException {
        return executeQuery(connection, "database-triggers", ownerName);
    }

    public ResultSet loadDatasetTriggers(String ownerName, String datasetName, Connection connection) throws SQLException {
        return executeQuery(connection, "dataset-triggers", ownerName, datasetName);
    }

    public ResultSet loadAllDatasetTriggers(String ownerName, Connection connection) throws SQLException {
        return executeQuery(connection, "all-dataset-triggers", ownerName);
    }

    public ResultSet loadFunctions(String ownerName, Connection connection) throws SQLException {
        return executeQuery(connection, "functions", ownerName);
    }

    public ResultSet loadProcedures(String ownerName, Connection connection) throws SQLException {
        return executeQuery(connection, "procedures", ownerName);
    }

    public ResultSet loadDimensions(String ownerName, Connection connection) throws SQLException {
        return executeQuery(connection, "dimensions", ownerName);
    }


   /*********************************************************
    *                        PACKAGES                       *
    *********************************************************/
    public ResultSet loadPackages(String ownerName, Connection connection) throws SQLException {
        return executeQuery(connection, "packages", ownerName);
    }

    public ResultSet loadPackageFunctions(String ownerName, String packageName, Connection connection) throws SQLException {
        return executeQuery(connection, "package-functions", ownerName, packageName);
    }

    public ResultSet loadAllPackageFunctions(String ownerName, Connection connection) throws SQLException {
        return executeQuery(connection, "all-package-functions", ownerName);
    }

    public ResultSet loadPackageProcedures(String ownerName, String packageName, Connection connection) throws SQLException {
        return executeQuery(connection, "package-procedures", ownerName, packageName);
    }

    public ResultSet loadAllPackageProcedures(String ownerName, Connection connection) throws SQLException {
        return executeQuery(connection, "all-package-procedures", ownerName);
    }

    public ResultSet loadPackageTypes(String ownerName, String packageName, Connection connection) throws SQLException {
        return executeQuery(connection, "package-types", ownerName, packageName);
    }

    public ResultSet loadAllPackageTypes(String ownerName, Connection connection) throws SQLException {
        return executeQuery(connection, "all-package-types", ownerName);
    }

    /*********************************************************
     *                        TYPES                          *
     *********************************************************/
    public ResultSet loadTypes(String ownerName, Connection connection) throws SQLException {
        return executeQuery(connection, "types", ownerName);
    }

    public ResultSet loadTypeAttributes(String ownerName, String typeName, Connection connection) throws SQLException {
        return executeQuery(connection, "type-attributes", ownerName, typeName);
    }

    public ResultSet loadAllTypeAttributes(String ownerName, Connection connection) throws SQLException {
        return executeQuery(connection, "all-type-attributes", ownerName);
    }

    public ResultSet loadProgramTypeAttributes(String ownerName, String programName, String typeName, Connection connection) throws SQLException {
        return executeQuery(connection, "program-type-attributes", ownerName, programName, typeName);
    }


    public ResultSet loadTypeFunctions(String ownerName, String typeName, Connection connection) throws SQLException {
        return executeQuery(connection, "type-functions", ownerName, typeName);
    }

    public ResultSet loadAllTypeFunctions(String ownerName, Connection connection) throws SQLException {
        return executeQuery(connection, "all-type-functions", ownerName);
    }

    public ResultSet loadTypeProcedures(String ownerName, String typeName, Connection connection) throws SQLException {
        return executeQuery(connection, "type-procedures", ownerName, typeName);
    }

    public ResultSet loadAllTypeProcedures(String ownerName, Connection connection) throws SQLException {
        return executeQuery(connection, "all-type-procedures", ownerName);
    }

    public ResultSet loadProgramMethodArguments(String ownerName, String programName, String methodName, int overload, Connection connection) throws SQLException {
        return executeQuery(connection, "program-method-arguments", ownerName, programName, methodName, overload);
    }

    public ResultSet loadMethodArguments(String ownerName, String methodName, String methodType, int overload, Connection connection) throws SQLException {
        return executeQuery(connection, "method-arguments", ownerName, methodName, methodType, overload);
    }

    public ResultSet loadAllMethodArguments(String ownerName, Connection connection) throws SQLException {
        return executeQuery(connection, "all-method-arguments", ownerName);
    }


   /*********************************************************
    *                   DATABASE LINKS                      *
    *********************************************************/

    public ResultSet loadDatabaseLinks(String ownerName, Connection connection) throws SQLException {
        return executeQuery(connection, "database-links", ownerName);
    }

   /*********************************************************
    *                      SEQUENCES                        *
    *********************************************************/

    public ResultSet loadSequences(String ownerName, Connection connection) throws SQLException {
        return executeQuery(connection, "sequences", ownerName);
    }

    /*********************************************************
     *                       SYNONYMS                        *
     *********************************************************/

    public ResultSet loadSynonyms(final String ownerName, Connection connection) throws SQLException {
        return executeQuery(connection, "synonyms", ownerName);
    }

    /*********************************************************
     *                      REFERENCES                       *
     *********************************************************/
    public ResultSet loadReferencedObjects(String ownerName, String objectName, Connection connection) throws SQLException {
        return executeQuery(connection, "referenced-objects", ownerName, objectName);
    }

    public ResultSet loadReferencingObjects(String ownerName, String objectName, Connection connection) throws SQLException {
        return executeQuery(connection, "referencing-objects", ownerName, objectName);
    }

    public ResultSet loadReferencingSchemas(String ownerName, String objectName, Connection connection) throws SQLException {
        return executeQuery(connection, "referencing-schemas", ownerName, objectName);
    }


   /*********************************************************
    *                     SOURCE CODE                       *
    *********************************************************/
     public ResultSet loadViewSourceCode(String ownerName, String viewName, Connection connection) throws SQLException {
        return executeQuery(connection, "view-source-code", ownerName, viewName);
    }

    public ResultSet loadMaterializedViewSourceCode(String ownerName, String viewName, Connection connection) throws SQLException {
        return executeQuery(connection, "materialized-view-source-code", ownerName, viewName);
   }

    public ResultSet loadDatabaseTriggerSourceCode(String ownerName, String triggerName, Connection connection) throws SQLException {
        return executeQuery(connection, "database-trigger-source-code", ownerName, triggerName);
    }

    public ResultSet loadDatasetTriggerSourceCode(String tableOwner, String tableName, String ownerName, String triggerName, Connection connection) throws SQLException {
        return executeQuery(connection, "dataset-trigger-source-code", tableOwner, tableName, ownerName, triggerName);
    }

    public ResultSet loadObjectSourceCode(String ownerName, String objectName, String objectType, Connection connection) throws SQLException {
        return loadObjectSourceCode(ownerName, objectName, objectType, 0, connection);
    }
    public ResultSet loadObjectSourceCode(String ownerName, String objectName, String objectType, int overload, Connection connection) throws SQLException {
        return executeQuery(connection, "object-source-code", ownerName, objectName, objectType, overload);
    }

   /*********************************************************
    *                   MISCELLANEOUS                       *
    *********************************************************/

    public ResultSet loadObjectChangeTimestamp(String ownerName, String objectName, String objectType, Connection connection) throws SQLException {
        return executeQuery(connection, "object-change-timestamp", ownerName, objectName, objectType);
    }

    public ResultSet loadInvalidObjects(String ownerName, Connection connection) throws SQLException {
        return executeQuery(connection, "invalid-objects", ownerName);
    }

    @Override
    public ResultSet loadDebugObjects(String ownerName, Connection connection) throws SQLException {
        return executeQuery(connection, "debug-objects", ownerName);
    }

    @Override
    public ResultSet loadCompileObjectErrors(String ownerName, String objectName, Connection connection) throws SQLException {
        return executeQuery(connection, "object-compile-errors", ownerName, objectName);
    }


    @Override
    public void compileObject(String ownerName, String objectName, String objectType, boolean debug, Connection connection) throws SQLException {
        executeStatement(connection, "compile-object", ownerName, objectName, objectType, debug ? "DEBUG" : "");
    }

    @Override
    public void compileObjectBody(String ownerName, String objectName, String objectType, boolean debug, Connection connection) throws SQLException {
        executeStatement(connection, "compile-object-body", ownerName, objectName, objectType, debug ? "DEBUG" : "");
    }

    @Override
    public void enableTrigger(String ownerName, String triggerName, Connection connection) throws SQLException {
        executeQuery(connection, "enable-trigger", ownerName, triggerName);
    }

    @Override
    public void disableTrigger(String ownerName, String triggerName, Connection connection) throws SQLException {
        executeQuery(connection, "disable-trigger", ownerName, triggerName);
    }

    @Override
    public void enableConstraint(String ownerName, String tableName, String constraintName, Connection connection) throws SQLException {
        executeQuery(connection, "enable-constraint", ownerName, tableName, constraintName);
    }

    @Override
    public void disableConstraint(String ownerName, String tableName, String constraintName, Connection connection) throws SQLException {
        executeQuery(connection, "disable-constraint", ownerName, tableName, constraintName);
    }

    @Override
    public ResultSet loadSessions(Connection connection) throws SQLException {
        return executeQuery(connection, "sessions");
    }

    @Override
    public ResultSet loadSessionCurrentSql(Object sessionId, Connection connection) throws SQLException {
        return executeQuery(connection, "session-sql", sessionId);
    }

    @Override
    public void killSession(Object sessionId, Object serialNumber, boolean immediate, Connection connection) throws SQLException {
        String loaderId = immediate ? "kill-session-immediate" : "kill-session";
        executeStatement(connection, loaderId, sessionId, serialNumber);
    }

    @Override
    public void disconnectSession(Object sessionId, Object serialNumber, boolean postTransaction, boolean immediate, Connection connection) throws SQLException {
        String loaderId =
                postTransaction ? "disconnect-session-post-transaction" :
                immediate ? "disconnect-session-immediate" : "disconnect-session";
        executeStatement(connection, loaderId, sessionId, serialNumber);
    }

    @Override
    public ResultSet loadExplainPlan(Connection connection) throws SQLException {
        return executeQuery(connection, "explain-plan-result");
    }

    @Override
    public void clearExplainPlanData(Connection connection) throws SQLException {
        executeUpdate(connection, "clear-explain-plan-data");
    }

    @Override
    public void enableLogger(Connection connection) throws SQLException {
        executeCall(connection, null, "enable-log-output");
    }

    @Override
    public void disableLogger(Connection connection) throws SQLException {
        executeCall(connection, null, "disable-log-output");
    }

    @Override
    public String readLoggerOutput(Connection connection) throws SQLException {
        ExecutionLogOutput outputReader = new ExecutionLogOutput();
        executeCall(connection, outputReader, "read-log-output");
        return outputReader.getLog();
    }

    public boolean isValid(Connection connection) {
        try {
            if (connection == null || connection.isClosed()) return false;
            ResultSet resultSet = executeQuery(connection, true, "validate-connection");
            ConnectionUtil.closeResultSet(resultSet);
        } catch (SQLException e) {
            return false;
        }
        return true;
    }


}
