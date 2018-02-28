package com.dci.intellij.dbn.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public interface DatabaseMetadataInterface extends DatabaseInterface{
    ResultSet getDistinctValues(String ownerName, String datasetName, String columnName, Connection connection) throws SQLException;

    /**
     * Load all database users
     * Column names of the returned ResultSet
     *  <li> USER_NAME (char)
     *  <li> IS_EXPIRED (Y/N)
     *  <li> IS_LOCKED (Y/N) 
     */
    ResultSet loadUsers(Connection connection) throws SQLException;

    /**
     * Load all database roles
     * Column names of the returned ResultSet
     *  <li> ROLE_NAME (char)
     */
    ResultSet loadRoles(Connection connection) throws SQLException;

    /**
     * Load all database system privileges
     * Column names of the returned ResultSet
     *  <li> PRIVILEGE_NAME (char)
     */
    ResultSet loadSystemPrivileges(Connection connection) throws SQLException;

    /**
     * Load all database object privileges
     * Column names of the returned ResultSet
     *  <li> PRIVILEGE_NAME (char)
     */
    ResultSet loadObjectPrivileges(Connection connection) throws SQLException;

    /**
     * Load all user roles
     * Column names of the returned ResultSet
     *  <li> USER_NAME (char)
     *  <li> GRANTED_ROLE_NAME (char)
     *  <li> IS_ADMIN_OPTION (char)
     *  <li> IS_DEFAULT_ROLE (char)
     */
    ResultSet loadAllUserRoles(Connection connection) throws SQLException;

    /**
     * Load all user privileges 
     * Column names of the returned ResultSet
     *  <li> USER_NAME (char)
     *  <li> GRANTED_PRIVILEGE_NAME (char)
     *  <li> IS_ADMIN_OPTION (char)
     */
    ResultSet loadAllUserPrivileges(Connection connection) throws SQLException;


    /**
     * Load all role privileges
     * Column names of the returned ResultSet
     *  <li> ROLE_NAME (char)
     *  <li> GRANTED_PRIVILEGE_NAME (char)
     *  <li> IS_ADMIN_OPTION (char)
     */
    ResultSet loadAllRolePrivileges(Connection connection) throws SQLException;

    /**
     * Load all role privileges
     * Column names of the returned ResultSet
     *  <li> ROLE_NAME (char)
     *  <li> GRANTED_ROLE_NAME (char)
     *  <li> IS_ADMIN_OPTION (char)
     *  <li> IS_DEFAULT_ROLE (char)
     */
    ResultSet loadAllRoleRoles(Connection connection) throws SQLException;


    /**
     * Load all database users
     * Column names of the returned ResultSet
     *  <li> SCHEMA_NAME (char)
     *  <li> IS_PUBLIC (Y/N)
     *  <li> IS_SYSTEM (Y/N)
     */
    ResultSet loadSchemas(Connection connection) throws SQLException;



    /**
     * Loads available character sets for the database
     * Column names of the returned ResultSet:
     *  <li> CHARSET_NAME (char)
     *  <li> MAX_LENGTH (number)
     */
    ResultSet loadCharsets(Connection connection) throws SQLException;


    /**
     * Load clusters for given owner <br>
     * Column names of the returned ResultSet
     *  <li> CLUSTER_NAME (char)
     */
    ResultSet loadClusters(String ownerName, Connection connection) throws SQLException;

    /**
     * Loads the tables of the given owner<br>
     * Column names of the returned ResultSet
     *  <li> TABLE_NAME (char)
     *  <li> IS_TEMPORARY (Y/N)
     */
    ResultSet loadTables(String ownerName, Connection connection) throws SQLException;

    /**
     * Loads the views of the given owner<br>
     * Column names of the returned ResultSet
     *  <li> VIEW_NAME (char)
     *  <li> IS_EDITABLE (Y/N)
     */
    ResultSet loadViews(String ownerName, Connection connection) throws SQLException;

    /**
     * Loads the materialized views of the given owner<br>
     * Column names of the returned ResultSet
     *  <li> VIEW_NAME (char)
     */
    ResultSet loadMaterializedViews(String ownerName, Connection connection) throws SQLException;


    /**
     * Loads the columns of the given dataset (can be a TABLE, VIEW or MATERIALIZED_VIEW)<br>
     * Column names of the returned ResultSet
     *  <li> COLUMN_NAME (char)
     *  <li> DATA_TYPE_NAME (char)
     *  <li> DATA_TYPE_OWNER (char)
     *  <li> DATA_LENGTH (number)
     *  <li> DATA_PRECISION (number)
     *  <li> DATA_SCALE (number)
     *  <li> IS_NULLABLE (Y/N)
     *  <li> IS_HIDDEN (Y/N)
     *
     */
    ResultSet loadColumns(String ownerName, String datasetName, Connection connection) throws SQLException;

    /**
     * Loads the linkage list between columns and constraints for given owner<br>
     * Column names of the returned ResultSet
     *  <li> DATASET_NAME (char)
     *  <li> COLUMN_NAME (char)
     *  <li> CONSTRAINT_NAME (char)
     *  <li> POSITION (char)
     *
     * Order by DATASET_NAME
     */
    ResultSet loadAllConstraintRelations(String ownerName, Connection connection) throws SQLException;

    /**
     * Loads the linkage list between columns and constraints for given dataset<br>
     * Column names of the returned ResultSet
     *  <li> COLUMN_NAME (char)
     *  <li> CONSTRAINT_NAME (char)
     *  <li> POSITION (char)
     */
    ResultSet loadConstraintRelations(String ownerName, String datasetName, Connection connection) throws SQLException;


    /**
     * Loads the linkage list between columns and indexes for given schema<br>
     * Column names of the returned ResultSet
     *  <li> TABLE_NAME (char)
     *  <li> COLUMN_NAME (char)
     *  <li> INDEX_NAME (char)
     *
     * Order by TABLE_NAME
     */
    ResultSet loadAllIndexRelations(String ownerName, Connection connection) throws SQLException;

    /**
     * Loads the linkage list between columns and indexes for given table<br>
     * Column names of the returned ResultSet
     *  <li> COLUMN_NAME (char)
     *  <li> INDEX_NAME (char)
     *
     */
    ResultSet loadIndexRelations(String ownerName, String tableName, Connection connection) throws SQLException;

    /**
     * Loads the columns of the given owner<br>
     * Column names of the returned ResultSet
     *  <li> DATASET_NAME (char)
     *  <li> COLUMN_NAME (char)
     *  <li> DATA_TYPE_NAME (char)
     *  <li> DATA_TYPE_OWNER (char)
     *  <li> DATA_LENGTH (number)
     *  <li> DATA_PRECISION (number)
     *  <li> DATA_SCALE (number)
     *  <li> IS_NULLABLE (Y/N)
     *  <li> IS_HIDDEN (Y/N)
     *
     * Sort by DATASET_NAME asc
     */
    ResultSet loadAllColumns(String ownerName, Connection connection) throws SQLException;

    /**
     * Loads the constraints of the given schema <br>
     * Column names of the returned ResultSet
     * <li> DATASET_NAME (char)
     * <li> CONSTRAINT_NAME (char)
     * <li> CONSTRAINT_TYPE (CHECK / PRIMARY KEY / FOREIGN KEY / DEFAULT / UNIQUE / VIEW CHECK / VIEW READONLY)
     * <li> CHECK_CONDITION (char - only for CHECK and VIEW CHECK constraints)
     * <li> STATUS (ENABLED/DISABLED)
     * <li> FK_CONSTRAINT_OWNER (char - only for FOREIGN KEY constraints)
     * <li> FK_CONSTRAINT_NAME (char - only for FOREIGN KEY constraints)
     *
     * Sort by DATASET_NAME asc
     */
    ResultSet loadAllConstraints(String ownerName, Connection connection) throws SQLException;

    /**
     * Loads the constraints of the given schema and dataset <br>
     * Column names of the returned ResultSet
     * <li> CONSTRAINT_NAME (char)
     * <li> CONSTRAINT_TYPE (CHECK / PRIMARY KEY / FOREIGN KEY / DEFAULT / UNIQUE / VIEW CHECK / VIEW READONLY)
     * <li> CHECK_CONDITION (char - only for CHECK and VIEW CHECK constraints)
     * <li> STATUS (ENABLED/DISABLED)
     * <li> FK_CONSTRAINT_OWNER (char - only for FOREIGN KEY constraints)
     * <li> FK_CONSTRAINT_NAME (char - only for FOREIGN KEY constraints)
     */
    ResultSet loadConstraints(String ownerName, String datasetName, Connection connection) throws SQLException;

    /**
     * Loads the indexes of the given owner <br>
     * Column names of the returned ResultSet
     * <li> INDEX_NAME (char)
     * <li> TABLE_NAME (char)
     * <li> UNIQUENESS (UNIQUE / NONUNIQUE)
     *
     * Sort by TABLE_NAME
     */
    ResultSet loadAllIndexes(String ownerName, Connection connection) throws SQLException;

    /**
     * Loads the indexes of the given owner and table <br>
     * Column names of the returned ResultSet
     * <li> INDEX_NAME (char)
     * <li> UNIQUENESS (UNIQUE / NONUNIQUE)
     *
     * Sort by TABLE_NAME
     */
    ResultSet loadIndexes(String ownerName, String tableName, Connection connection) throws SQLException;    

    /**
     * Loads the list of nested-tables owned by the given table<br>
     * Column names of the returned ResultSet
     *  <li> NESTED_TABLE_NAME (char)
     *  <li> TABLE_COLUMN (char)
     *  <li> TYPE_NAME (char)
     *  <li> TYPE_OWNER (char)
     */
    ResultSet loadNestedTables(String ownerName, String tableName, Connection connection) throws SQLException;

    /**
     * Loads the list of nested-tables for the given owner<br>
     * Column names of the returned ResultSet
     *  <li> NESTED_TABLE_NAME (char)
     *  <li> TABLE_NAME (char)
     *  <li> TABLE_COLUMN (char)
     *  <li> TYPE_NAME (char)
     *  <li> TYPE_OWNER (char)
     *
     * Sort by TABLE_NAME
     */
    ResultSet loadAllNestedTables(String ownerName, Connection connection) throws SQLException;


    /**
     * Loads the triggers of the given owner<br>
     * Column names of the returned ResultSet
     *  <li> TRIGGER_NAME (char)
     *  <li> TRIGGER_TYPE (char)
     *  <li> TRIGGERING_EVENT (INSERT/DELETE/UPDATE e.g. INSERT or UPDATE)
     *  <li> IS_ENABLED (Y/N)
     *  <li> IS_VALID (Y/N)
     *  <li> IS_FOR_EACH_ROW (Y/N)
     */
    ResultSet loadDatabaseTriggers(String ownerName, Connection connection) throws SQLException;

    /**
     * Loads the triggers of the given dataset (can be a TABLE, VIEW or MATERIALIZED_VIEW)<br>
     * Column names of the returned ResultSet
     *  <li> TRIGGER_NAME (char)
     *  <li> TRIGGER_TYPE (char)
     *  <li> TRIGGERING_EVENT (INSERT/DELETE/UPDATE e.g. INSERT or UPDATE)
     *  <li> IS_ENABLED (Y/N)
     *  <li> IS_VALID (Y/N)
     *  <li> IS_FOR_EACH_ROW (Y/N)
     */
    ResultSet loadDatasetTriggers(String ownerName, String datasetName, Connection connection) throws SQLException;

    /**
     * Loads the triggers of the given owner<br>
     * Column names of the returned ResultSet
     *  <li> DATASET_NAME (char)
     *  <li> TRIGGER_NAME (char)
     *  <li> TRIGGER_TYPE (char)
     *  <li> TRIGGERING_EVENT (INSERT/DELETE/UPDATE e.g. INSERT or UPDATE)
     *  <li> IS_ENABLED (Y/N)
     *  <li> IS_VALID (Y/N)
     *  <li> IS_FOR_EACH_ROW (Y/N)
     *
     * Sort by DATASET_NAME
     */
    ResultSet loadAllDatasetTriggers(String ownerName, Connection connection) throws SQLException;

    /**
     * Loads the sequences of the given owner<br>
     * Column names of the returned ResultSet
     *  <li> SEQUENCE_NAME (char)
     */
    ResultSet loadSequences(String ownerName, Connection connection) throws SQLException;


    /**
     * Loads the synonyms of the given owner
     * Column names of the returned ResultSet
     *  <li> SYNONYM_NAME (char)
     *  <li> OBJECT_OWNER (char)
     *  <li> OBJECT_NAME (char)
     */
    ResultSet loadSynonyms(String ownerName, Connection connection) throws SQLException;



    /**
     * Loads the functions of the given owner<br>
     * Column names of the returned ResultSet
     *  <li> FUNCTION_NAME (char)
     *  <li> VALID (Y/N)
     */
    ResultSet loadFunctions(String ownerName, Connection connection) throws SQLException;

    /**
     * Loads the procedures of the given owner<br>
     * Column names of the returned ResultSet
     *  <li> PROCEDURE_NAME (char)
     *  <li> VALID (Y/N)
     */
    ResultSet loadProcedures(String ownerName, Connection connection) throws SQLException;

    /**
     * Loads the dimensions the given owner<br>
     * Column names of the returned ResultSet
     *  <li> DIMENSION_NAME (char)
     */
    ResultSet loadDimensions(String ownerName, Connection connection) throws SQLException;

   /*********************************************************
    *                        PACKAGES                       *
    *********************************************************/
    /**
     * Loads the package list of the given owner<br>
     * Column names of the returned ResultSet
     *  <li> PACKAGE_NAME (char)
     *  <li> SPEC_STATUS ('VALID'/'INVALID')
     *  <li> BODY_STATUS ('VALID'/'INVALID')
     */
    ResultSet loadPackages(String ownerName, Connection connection) throws SQLException;

    /**
     * Loads the function for the given package<br>
     * Column names of the returned ResultSet
     *  <li> FUNCTION_NAME (char)
     *  <li> OVERLOAD (number: 0 if single instance)
     */
    ResultSet loadPackageFunctions(String ownerName, String packageName, Connection connection) throws SQLException;

    /**
     * Loads all the package functions for the given owner<br>
     * Column names of the returned ResultSet
     *  <li> PACKAGE_NAME (char)
     *  <li> FUNCTION_NAME (char)
     *  <li> OVERLOAD (number: 0 if single instance)
     *
     * Sort by PACKAGE_NAME asc
     */
    ResultSet loadAllPackageFunctions(String ownerName, Connection connection) throws SQLException;


    /**
     * Loads the procedures for the given package<br>
     * Column names of the returned ResultSet
     *  <li> PROCEDURE_NAME (char)
     *  <li> OVERLOAD (number: 0 if single instance)
     */
    ResultSet loadPackageProcedures(String ownerName, String packageName, Connection connection) throws SQLException;

    /**
     * Loads all the package procedures for the given owner<br>
     * Column names of the returned ResultSet
     *  <li> PACKAGE_NAME (char)
     *  <li> PROCEDURE_NAME (char)
     *  <li> OVERLOAD (number: 0 if single instance)
     *
     * Sort by PACKAGE_NAME asc
     */
    ResultSet loadAllPackageProcedures(String ownerName, Connection connection) throws SQLException;

    /**
     * Loads the package types for the given owner and package<br>
     * Column names of the returned ResultSet
     *  <li> TYPE_NAME (char)
     *
     * Sort by TYPE_NAME asc
     */
    ResultSet loadPackageTypes(String ownerName, String packageName, Connection connection) throws SQLException;

    /**
     * Loads all package types for the given owner<br>
     * Column names of the returned ResultSet
     *  <li> PACKAGE_NAME (char)
     *  <li> TYPE_NAME (char)
     *
     * Sort by TYPE_NAME asc
     */
    ResultSet loadAllPackageTypes(String ownerName, Connection connection) throws SQLException;


   /*********************************************************
    *                        TYPES                          *
    *********************************************************/

    /**
     * Loads the user defined types (UDT) of the given owner<br>
     * Column names of the returned ResultSet
     *  <li> TYPE_NAME (char)
     *  <li> SPEC_STATUS ('VALID'/'INVALID')
     *  <li> BODY_STATUS ('VALID'/'INVALID')
     *  <li> SUPERTYPE_OWNER (char)
     *  <li> SUPERTYPE_NAME (char)
     *  <li> TYPECODE (char)
     */
    ResultSet loadTypes(String ownerName, Connection connection) throws SQLException;



    /**
     * Loads attributes of the given user defined type <br>
     * Column names of the returned ResultSet
     *  <li> ATTRIBUTE_NAME (char)
     *  <li> ATTRIBUTE_TYPE_OWNER (char)
     *  <li> ATTRIBUTE_TYPE_NAME (char)
     */
    ResultSet loadTypeAttributes(String ownerName, String typeName, Connection connection) throws SQLException;

    /**
     * Loads attributes for all declared types of the given owner <br>
     * Column names of the returned ResultSet
     *  <li> TYPE_NAME (char)
     *  <li> ATTRIBUTE_NAME (char)
     *  <li> ATTRIBUTE_TYPE_OWNER (char)
     *  <li> ATTRIBUTE_TYPE_NAME (char)
     */
    ResultSet loadAllTypeAttributes(String ownerName, Connection connection) throws SQLException;

    public ResultSet loadProgramTypeAttributes(String ownerName, String programName, String typeName, Connection connection) throws SQLException;    

    /**
     * Loads the function for the given type<br>
     * Column names of the returned ResultSet
     *  <li> FUNCTION_NAME (char)
     *  <li> OVERLOAD (number: 0 if single instance)
     */
    ResultSet loadTypeFunctions(String ownerName, String typeName, Connection connection) throws SQLException;

    /**
     * Loads all the type functions for the given owner<br>
     * Column names of the returned ResultSet
     *  <li> TYPE_NAME (char)
     *  <li> FUNCTION_NAME (char)
     *  <li> OVERLOAD (number: 0 if single instance)
     *
     * Sort by TYPE_NAME asc
     */
    ResultSet loadAllTypeFunctions(String ownerName, Connection connection) throws SQLException;


    /**
     * Loads the procedures for the given type<br>
     * Column names of the returned ResultSet
     *  <li> PROCEDURE_NAME (char)
     *  <li> OVERLOAD (number: 0 if single instance)
     */
    ResultSet loadTypeProcedures(String ownerName, String typeName, Connection connection) throws SQLException;

    /**
     * Loads all the type procedures for the given owner<br>
     * Column names of the returned ResultSet
     *  <li> TYPE_NAME (char)
     *  <li> PROCEDURE_NAME (char)
     *  <li> OVERLOAD (number: 0 if single instance)
     *
     * Sort by TYPE_NAME asc
     */
    ResultSet loadAllTypeProcedures(String ownerName, Connection connection) throws SQLException;

    /*********************************************************
     *                     ARGUMENTS                         *
     *********************************************************/

    /**
     * Loads all arguments for a given loose method (not owned by package or type).<br>
     *  <li> ARGUMENT_NAME (char, nullable)
     *  <li> OVERLOAD (number: 0 if single instance)
     *  <li> POSITION (number)
     *  <li> SEQUENCE (number)
     *  <li> IN_OUT ('IN', 'OUT', 'IN/OUT')
     */
    ResultSet loadMethodArguments(String ownerName, String methodName, String methodType, int overload, Connection connection) throws SQLException;

    /**
     * Loads all arguments for a given program method (function or procedure of a package or type).<br>
     *  <li> ARGUMENT_NAME (char, nullable)
     *  <li> OVERLOAD (number: 0 if single instance)
     *  <li> POSITION (number)
     *  <li> SEQUENCE (number)
     *  <li> IN_OUT ('IN', 'OUT', 'IN/OUT')
     */
    ResultSet loadProgramMethodArguments(String ownerName, String programName, String methodName, int overload, Connection connection) throws SQLException;

    /**
     * Loads all method (function/procedure) arguments for a given owner.<br>
     * PACKAGE_NAME may stand for TYPE_NAME as well, or can be null for loose functions and procedures<br>
     *  <li> ARGUMENT_NAME (char, nullable)
     *  <li> PROGRAM_NAME (char, nullable)
     *  <li> METHOD_NAME (char)
     *  <li> OVERLOAD (number: 0 if single instance)
     *  <li> POSITION (number)
     *  <li> SEQUENCE (number)
     *  <li> IN_OUT ('IN', 'OUT', 'IN/OUT')
     */
    ResultSet loadAllMethodArguments(String ownerName, Connection connection) throws SQLException;

   /*********************************************************
    *                   DATABASE LINKS                      *
    *********************************************************/

    /**
     * Loads the database links for the given owner<br>
     * Column names of the returned ResultSet
     *  <li> DBLINK_NAME (char)
     *  <li> USER_NAME (char)
     *  <li> HOST (char)
     */
    ResultSet loadDatabaseLinks(String ownerName, Connection connection) throws SQLException;


    /**
     * Loads the referenced objects for the given object (objects on which it depends)
     * Column names of the returned ResultSet
     *  <li> OBJECT_OWNER (char)
     *  <li> OBJECT_NAME (char)
     */
    ResultSet loadReferencedObjects(String ownerName, String objectName, Connection connection) throws SQLException;

    ResultSet loadReferencingSchemas(String ownerName, String objectName, Connection connection) throws SQLException;

    /**
     * Loads the referencing objects for the given object (objects depending on it)
     * Column names of the returned ResultSet
     *  <li> OBJECT_OWNER (char)
     *  <li> OBJECT_NAME (char)
     */
    ResultSet loadReferencingObjects(String ownerName, String objectName, Connection connection) throws SQLException;


    void setCurrentSchema(String schemaName, Connection connection) throws SQLException;

    /**
     * Loads the source code (select statement) for the given view;
     * ResultSet should contain only one column (name is not relevant).
     * View source-code may be split on more than one line.
     */
    ResultSet loadViewSourceCode(String ownerName, String viewName, Connection connection) throws SQLException;

    ResultSet loadMaterializedViewSourceCode(String ownerName, String viewName, Connection connection) throws SQLException;

    ResultSet loadDatabaseTriggerSourceCode(String ownerName, String triggerName, Connection connection) throws SQLException;

    ResultSet loadDatasetTriggerSourceCode(String tableOwner, String tableName, String ownerName, String triggerName, Connection connection) throws SQLException;

    /**
     * Loads the source code (select statement) for the given view;
     * ResultSet should contain only one column (name is not relevant).
     * View source-code may be split on more than one line.
     */
    ResultSet loadObjectSourceCode(String ownerName, String objectName, String objectType, Connection connection) throws SQLException;
    ResultSet loadObjectSourceCode(String ownerName, String objectName, String objectType, int overload, Connection connection) throws SQLException;

    /**
     * Loads a list of invalid object names for the given owner.
     * This is used to update the status of the objects.
     *  <li> OBJECT_NAME (char)
     *  <li> OBJECT_TYPE (char)
     */
    ResultSet loadInvalidObjects(String ownerName, Connection connection) throws SQLException;

    /**
     * Loads a list of debug object names for the given owner.
     * This is used to update the status of the objects.
     *  <li> OBJECT_NAME (char)
     *  <li> OBJECT_TYPE (char)
     */
    ResultSet loadDebugObjects(String ownerName, Connection connection) throws SQLException;

    /**
     *  Loads object errors. To be used after compiling objects to hint on syntax errors.
     *  <li> LINE (number)
     *  <li> POSITION (number)
     *  <li> TEXT (number)
     *  <li> OBJECT_TYPE (char)
     */
    ResultSet loadCompileObjectErrors(String ownerName, String objectName, Connection connection) throws SQLException;

    void compileObject(String ownerName, String objectName, String objectType, boolean debug, Connection connection) throws SQLException;

    void compileObjectBody(String ownerName, String objectName, String objectType, boolean debug, Connection connection) throws SQLException;

    ResultSet loadObjectChangeTimestamp(String ownerName, String objectName, String objectType, Connection connection) throws SQLException;

    void enableTrigger(String ownerName, String triggerName, Connection connection) throws SQLException;

    void disableTrigger(String ownerName, String triggerName, Connection connection) throws SQLException;

    void enableConstraint(String ownerName, String tableName, String constraintName, Connection connection) throws SQLException;

    void disableConstraint(String ownerName, String tableName, String constraintName, Connection connection) throws SQLException;

    ResultSet loadSessions(Connection connection) throws SQLException;

    ResultSet loadSessionCurrentSql(Object sessionId, Connection connection) throws SQLException;

    void killSession(Object sessionId, Object serialNumber, boolean immediate, Connection connection) throws SQLException;

    void disconnectSession(Object sessionId, Object serialNumber, boolean postTransaction, boolean immediate, Connection connection) throws SQLException;

    ResultSet loadExplainPlan(Connection connection) throws SQLException;

    void clearExplainPlanData(Connection connection) throws SQLException;

    void enableLogger(Connection connection) throws SQLException;
    void disableLogger(Connection connection) throws SQLException;
    String readLoggerOutput(Connection connection) throws SQLException;

    boolean isValid(Connection connection);

    String createDateString(Date date);
}