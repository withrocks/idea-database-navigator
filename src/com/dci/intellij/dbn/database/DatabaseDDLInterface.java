package com.dci.intellij.dbn.database;

import java.sql.Connection;
import java.sql.SQLException;

import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.code.SourceCodeContent;
import com.dci.intellij.dbn.object.factory.MethodFactoryInput;
import com.intellij.openapi.project.Project;

public interface DatabaseDDLInterface extends DatabaseInterface{
    String createDDLStatement(Project project, DatabaseObjectTypeId objectTypeId, String userName, String schemaName, String objectName, DBContentType contentType, String code, String alternativeDelimiter);

    void computeSourceCodeOffsets(SourceCodeContent content, DatabaseObjectTypeId objectTypeId, String objectName);

    boolean includesTypeAndNameInSourceContent(DatabaseObjectTypeId objectTypeId);

    /*********************************************************
     *                   CREATE statements                   *
     *********************************************************/
    void createView(String viewName, String code, Connection connection) throws SQLException;

    void createMethod(MethodFactoryInput methodFactoryInput, Connection connection) throws SQLException;

    void createObject(String code, Connection connection) throws SQLException;

    /*********************************************************
     *                   CHANGE statements                   *
     *********************************************************/
    void updateView(String viewName, String oldCode, String newCode, Connection connection) throws SQLException;

    void updateTrigger(String tableOwner, String tableName, String triggerName, String oldCode, String newCode, Connection connection) throws SQLException;

    void updateObject(String objectName, String objectType, String oldCode, String newCode, Connection connection) throws SQLException;

   /*********************************************************
    *                   DROP statements                     *
    *********************************************************/
    void dropObject(String objectType, String objectName, Connection connection) throws SQLException;

    void dropObjectBody(String objectType, String objectName, Connection connection) throws SQLException;

   /*********************************************************
    *                   RENAME statements                     *
    *********************************************************/



}
