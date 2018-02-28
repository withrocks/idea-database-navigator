package com.dci.intellij.dbn.object;

import java.sql.SQLException;
import java.util.List;

import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;

public interface DBSchema extends DBObject {
    boolean isPublicSchema();
    boolean isUserSchema();
    boolean isSystemSchema();
    boolean isEmptySchema();
    List<DBDataset> getDatasets();
    List<DBTable> getTables();
    List<DBView> getViews();
    List<DBMaterializedView> getMaterializedViews();
    List<DBIndex> getIndexes();
    List<DBSynonym> getSynonyms();
    List<DBSequence> getSequences();
    List<DBProcedure> getProcedures();
    List<DBFunction> getFunctions();
    List<DBPackage> getPackages();
    List<DBDatasetTrigger> getDatasetTriggers();
    List<DBDatabaseTrigger> getDatabaseTriggers();
    List<DBType> getTypes();
    List<DBDimension> getDimensions();
    List<DBCluster> getClusters();
    List<DBDatabaseLink> getDatabaseLinks();

    DBDataset getDataset(String name);
    DBTable getTable(String name);
    DBView getView(String name);
    DBMaterializedView getMaterializedView(String name);
    DBIndex getIndex(String name);
    DBType getType(String name);
    DBPackage getPackage(String name);
    DBProgram getProgram(String name);
    DBMethod getMethod(String name, DBObjectType methodType, int overload);
    DBMethod getMethod(String name, int overload);
    DBProcedure getProcedure(String name, int overload);
    DBFunction getFunction(String name, int overload);
    DBCluster getCluster(String name);
    DBDatabaseLink getDatabaseLink(String name);

    void refreshObjectsStatus() throws SQLException;

    @Override
    DBObjectRef<DBSchema> getRef();
}
