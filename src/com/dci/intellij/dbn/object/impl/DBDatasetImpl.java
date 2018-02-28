package com.dci.intellij.dbn.object.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.dci.intellij.dbn.browser.ui.HtmlToolTipBuilder;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentElement;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoader;
import com.dci.intellij.dbn.common.content.loader.DynamicContentResultSetLoader;
import com.dci.intellij.dbn.common.content.loader.DynamicSubcontentLoader;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.ddl.DDLFileType;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBConstraint;
import com.dci.intellij.dbn.object.DBDataset;
import com.dci.intellij.dbn.object.DBDatasetTrigger;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObjectRelationType;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.DBSchemaObjectImpl;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;

public abstract class DBDatasetImpl extends DBSchemaObjectImpl implements DBDataset {
    protected DBObjectList<DBColumn> columns;
    protected DBObjectList<DBConstraint> constraints;
    protected DBObjectList<DBDatasetTrigger> triggers;

    public DBDatasetImpl(DBSchema parent, ResultSet resultSet) throws SQLException {
        super(parent, resultSet);
    }

    protected void initLists() {
        super.initLists();
        DBSchema schema = getSchema();
        DBObjectListContainer childObjects = initChildObjects();
        columns = childObjects.createSubcontentObjectList(DBObjectType.COLUMN, this, COLUMNS_LOADER, schema, true);
        constraints = childObjects.createSubcontentObjectList(DBObjectType.CONSTRAINT, this, CONSTRAINTS_LOADER, schema, true);
        triggers = childObjects.createSubcontentObjectList(DBObjectType.DATASET_TRIGGER, this, TRIGGERS_LOADER, schema, true);

        initChildObjectRelations().createSubcontentObjectRelationList(
                DBObjectRelationType.CONSTRAINT_COLUMN, this,
                "Constraint column relations", 
                CONSTRAINT_COLUMN_RELATION_LOADER,
                schema);
    }

    public List<DBColumn> getColumns() {
        return columns.getObjects();
    }

    public List<DBConstraint> getConstraints() {
        return constraints.getObjects();
    }

    public List<DBDatasetTrigger> getTriggers() {
        return triggers.getObjects();
    }

    public DBColumn getColumn(String name) {
        return columns.getObject(name);
    }

    public DBConstraint getConstraint(String name) {
        return constraints.getObject(name);
    }

    public DBDatasetTrigger getTrigger(String name) {
        return triggers.getObject(name);
    }

    public boolean hasLobColumns() {
        for (DBColumn column : getColumns()) {
            DBDataType dataType = column.getDataType();
            if (dataType.isNative() && dataType.getNativeDataType().isLargeObject()) {
                return true;
            }

        }
        return false;
    }

    public void buildToolTip(HtmlToolTipBuilder ttb) {
        ttb.append(true, getObjectType().getName(), true);
        ttb.createEmptyRow();
        super.buildToolTip(ttb);
    }

    /*********************************************************
     *                         Loaders                       *
     *********************************************************/
    private static final DynamicSubcontentLoader CONSTRAINT_COLUMN_RELATION_LOADER = new DynamicSubcontentLoader(true) {
        public DynamicContentLoader getAlternativeLoader() {
            return CONSTRAINT_COLUMN_RELATION_ALTERNATIVE_LOADER;
        }

        public boolean match(DynamicContentElement sourceElement, DynamicContent dynamicContent) {
            DBConstraintColumnRelation constraintColumnRelation = (DBConstraintColumnRelation) sourceElement;
            DBDataset dataset = (DBDataset) dynamicContent.getParent();
            return constraintColumnRelation.getColumn().getDataset() == dataset;
        }
    };

    private static final DynamicContentLoader CONSTRAINT_COLUMN_RELATION_ALTERNATIVE_LOADER = new DynamicContentResultSetLoader() {
        public ResultSet createResultSet(DynamicContent dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            DBDataset dataset = (DBDataset) dynamicContent.getParent();
            return metadataInterface.loadConstraintRelations(dataset.getSchema().getName(), dataset.getName(), connection);
        }

        public DynamicContentElement createElement(DynamicContent dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            String columnName = resultSet.getString("COLUMN_NAME");
            String constraintName = resultSet.getString("CONSTRAINT_NAME");
            int position = resultSet.getInt("POSITION");

            DBDataset dataset = (DBDataset) dynamicContent.getParent();
            DBColumn column = dataset.getColumn(columnName);
            DBConstraint constraint = dataset.getConstraint(constraintName);

            if (column != null && constraint != null) {
                return new DBConstraintColumnRelation(constraint, column, position);
            }
            return null;
        }
    };

    private static final DynamicSubcontentLoader COLUMNS_LOADER = new DynamicSubcontentLoader<DBColumn>(true) {
        public boolean match(DBColumn column, DynamicContent dynamicContent) {
            DBDataset dataset = (DBDataset) dynamicContent.getParent();
            return CommonUtil.safeEqual(column.getDataset(), dataset);
        }

        public DynamicContentLoader<DBColumn> getAlternativeLoader() {
            return COLUMNS_ALTERNATIVE_LOADER;
        }
    };
    private static final DynamicContentLoader<DBColumn> COLUMNS_ALTERNATIVE_LOADER = new DynamicContentResultSetLoader<DBColumn>() {
        public ResultSet createResultSet(DynamicContent<DBColumn> dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            DBDataset dataset = (DBDataset) dynamicContent.getParent();
            return metadataInterface.loadColumns(dataset.getSchema().getName(), dataset.getName(), connection);
        }

        public DBColumn createElement(DynamicContent<DBColumn> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            DBDatasetImpl dataset = (DBDatasetImpl) dynamicContent.getParent();
            return new DBColumnImpl(dataset, resultSet);
        }
    };

    private static final DynamicSubcontentLoader<DBConstraint> CONSTRAINTS_LOADER = new DynamicSubcontentLoader<DBConstraint>(true) {
        public boolean match(DBConstraint constraint, DynamicContent dynamicContent) {
            DBDataset dataset = (DBDataset) dynamicContent.getParent();
            DBDataset constraintDataset = constraint.getDataset();
            return constraintDataset != null && constraintDataset.equals(dataset);
        }

        public DynamicContentLoader<DBConstraint> getAlternativeLoader() {
            return CONSTRAINTS_ALTERNATIVE_LOADER;
        }
    };

    private static final DynamicContentLoader<DBConstraint> CONSTRAINTS_ALTERNATIVE_LOADER = new DynamicContentResultSetLoader<DBConstraint>() {
        public ResultSet createResultSet(DynamicContent<DBConstraint> dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            DBDataset dataset = (DBDataset) dynamicContent.getParent();
            return metadataInterface.loadConstraints(dataset.getSchema().getName(), dataset.getName(), connection);
        }

        public DBConstraint createElement(DynamicContent<DBConstraint> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            DBDatasetImpl dataset = (DBDatasetImpl) dynamicContent.getParent();
            return new DBConstraintImpl(dataset, resultSet);
        }
    };

    private static final DynamicSubcontentLoader TRIGGERS_LOADER = new DynamicSubcontentLoader<DBDatasetTrigger>(true) {
        public boolean match(DBDatasetTrigger trigger, DynamicContent dynamicContent) {
            DBDataset dataset = (DBDataset) dynamicContent.getParent();
            return trigger.getDataset().equals(dataset);
        }

        public DynamicContentLoader<DBDatasetTrigger> getAlternativeLoader() {
            return TRIGGERS_ALTERNATIVE_LOADER;
        }
    };

    private static final DynamicContentLoader<DBDatasetTrigger> TRIGGERS_ALTERNATIVE_LOADER = new DynamicContentResultSetLoader<DBDatasetTrigger>() {
        public ResultSet createResultSet(DynamicContent<DBDatasetTrigger> dynamicContent, Connection connection) throws SQLException {
            DatabaseMetadataInterface metadataInterface = dynamicContent.getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            DBDataset dataset = (DBDataset) dynamicContent.getParent();
            return metadataInterface.loadDatasetTriggers(dataset.getSchema().getName(), dataset.getName(), connection);
        }

        public DBDatasetTrigger createElement(DynamicContent<DBDatasetTrigger> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException {
            DBDatasetImpl dataset = (DBDatasetImpl) dynamicContent.getParent();
            return new DBDatasetTriggerImpl(dataset, resultSet);
        }
    };

    /*********************************************************
     *                    DBEditableObject                   *
     ********************************************************/
    public DDLFileType getDDLFileType(DBContentType contentType) {
        return null;
    }

    public DDLFileType[] getDDLFileTypes() {
        return null;
    }
}
