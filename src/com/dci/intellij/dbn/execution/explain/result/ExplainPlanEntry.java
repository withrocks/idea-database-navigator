package com.dci.intellij.dbn.execution.explain.result;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.Disposable;

public class ExplainPlanEntry implements Disposable {
    private DBObjectRef objectRef;
    private String operation;
    private String operationOptions;
    private String optimizer;
    private Integer id;
    private Integer parentId;
    private BigDecimal depth;
    private BigDecimal position;
    private BigDecimal cost;
    private BigDecimal cardinality;
    private BigDecimal bytes;
    private BigDecimal cpuCost;
    private BigDecimal ioCost;
    private String accessPredicates;
    private String filterPredicates;
    private String projection;

    private ExplainPlanEntry parent;
    private List<ExplainPlanEntry> children;

    public ExplainPlanEntry(ConnectionHandler connectionHandler, ResultSet resultSet) throws SQLException {
        operation = resultSet.getString("OPERATION");
        operationOptions = resultSet.getString("OPTIONS");
        optimizer = resultSet.getString("OPTIMIZER");
        id = resultSet.getInt("ID");
        parentId = resultSet.getInt("PARENT_ID");
        if (resultSet.wasNull()) {
            parentId = null;
        }

        depth = resultSet.getBigDecimal("DEPTH");
        position = resultSet.getBigDecimal("POSITION");
        cost = resultSet.getBigDecimal("COST");
        cpuCost = resultSet.getBigDecimal("CPU_COST");
        ioCost = resultSet.getBigDecimal("IO_COST");
        cardinality = resultSet.getBigDecimal("CARDINALITY");
        bytes = resultSet.getBigDecimal("BYTES");

        accessPredicates = resultSet.getString("ACCESS_PREDICATES");
        filterPredicates = resultSet.getString("FILTER_PREDICATES");
        projection = resultSet.getString("PROJECTION");

        String objectOwner = resultSet.getString("OBJECT_OWNER");
        String objectName = resultSet.getString("OBJECT_NAME");
        String objectTypeName = resultSet.getString("OBJECT_TYPE");
        if (StringUtil.isNotEmpty(objectOwner) && StringUtil.isNotEmpty(objectName) && StringUtil.isNotEmpty(objectTypeName)) {
            DBObjectType objectType = DBObjectType.ANY;
            if (objectTypeName.startsWith("TABLE")) {
                objectType = DBObjectType.TABLE;
            } else if (objectTypeName.startsWith("MAT_VIEW")) {
                objectType = DBObjectType.MATERIALIZED_VIEW;
            } else if (objectTypeName.startsWith("VIEW")) {
                objectType = DBObjectType.VIEW;
            } else if (objectTypeName.startsWith("INDEX")) {
                objectType = DBObjectType.INDEX;
            }


            DBObjectRef schemaRef = new DBObjectRef(connectionHandler.getId(), DBObjectType.SCHEMA, objectOwner);
            objectRef = new DBObjectRef(schemaRef, objectType, objectName);
        }

    }

    public ExplainPlanEntry getParent() {
        return parent;
    }

    public void setParent(ExplainPlanEntry parent) {
        this.parent = parent;
    }

    public void addChild(ExplainPlanEntry child) {
        if (children == null) {
            children = new ArrayList<ExplainPlanEntry>();
        }
        children.add(child);
    }

    public List<ExplainPlanEntry> getChildren() {
        return children;
    }

    public DBObjectRef getObjectRef() {
        return objectRef;
    }

    public String getOperation() {
        return operation;
    }

    public String getOperationOptions() {
        return operationOptions;
    }

    public String getOptimizer() {
        return optimizer;
    }

    public Integer getId() {
        return id;
    }

    public Integer getParentId() {
        return parentId;
    }

    public BigDecimal getDepth() {
        return depth;
    }

    public BigDecimal getPosition() {
        return position;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public BigDecimal getCardinality() {
        return cardinality;
    }

    public BigDecimal getBytes() {
        return bytes;
    }

    public BigDecimal getCpuCost() {
        return cpuCost;
    }

    public BigDecimal getIoCost() {
        return ioCost;
    }

    public String getAccessPredicates() {
        return accessPredicates;
    }

    public String getFilterPredicates() {
        return filterPredicates;
    }

    public String getProjection() {
        return projection;
    }

    @Override
    public void dispose() {
        DisposerUtil.dispose(children);
        parent = null;
    }
}
