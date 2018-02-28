package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.ddl.DDLFileManager;
import com.dci.intellij.dbn.ddl.DDLFileType;
import com.dci.intellij.dbn.ddl.DDLFileTypeId;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.object.DBDataset;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.DBTrigger;
import com.dci.intellij.dbn.object.common.DBSchemaObjectImpl;
import com.dci.intellij.dbn.object.common.loader.DBObjectTimestampLoader;
import com.dci.intellij.dbn.object.common.operation.DBOperationExecutor;
import com.dci.intellij.dbn.object.common.operation.DBOperationNotSupportedException;
import com.dci.intellij.dbn.object.common.operation.DBOperationType;
import com.dci.intellij.dbn.object.common.property.DBObjectProperties;
import com.dci.intellij.dbn.object.common.property.DBObjectProperty;
import com.dci.intellij.dbn.object.common.status.DBObjectStatus;
import com.dci.intellij.dbn.object.common.status.DBObjectStatusHolder;
import com.dci.intellij.dbn.object.properties.PresentableProperty;
import com.dci.intellij.dbn.object.properties.SimplePresentableProperty;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class DBTriggerImpl extends DBSchemaObjectImpl implements DBTrigger {
    private boolean isForEachRow;
    private TriggerType triggerType;
    private TriggeringEvent[] triggeringEvents;

    public DBTriggerImpl(DBSchema schema, ResultSet resultSet) throws SQLException {
        super(schema, resultSet);
    }

    public DBTriggerImpl(DBDataset dataset, ResultSet resultSet) throws SQLException {
        super(dataset, resultSet);
    }

    @Override
    protected void initObject(ResultSet resultSet) throws SQLException {
        name = resultSet.getString("TRIGGER_NAME");
        isForEachRow = resultSet.getString("IS_FOR_EACH_ROW").equals("Y");

        String triggerTypeString = resultSet.getString("TRIGGER_TYPE");
        triggerType =
                triggerTypeString.contains("BEFORE") ? TRIGGER_TYPE_BEFORE :
                        triggerTypeString.contains("AFTER") ? TRIGGER_TYPE_AFTER :
                                triggerTypeString.contains("INSTEAD OF") ? TRIGGER_TYPE_INSTEAD_OF :
                                        TRIGGER_TYPE_UNKNOWN;


        String triggeringEventString = resultSet.getString("TRIGGERING_EVENT");
        List<TriggeringEvent> triggeringEventList = new ArrayList<TriggeringEvent>();
        if (triggeringEventString.contains("INSERT")) triggeringEventList.add(TRIGGERING_EVENT_INSERT);
        if (triggeringEventString.contains("UPDATE")) triggeringEventList.add(TRIGGERING_EVENT_UPDATE);
        if (triggeringEventString.contains("DELETE")) triggeringEventList.add(TRIGGERING_EVENT_DELETE);
        if (triggeringEventString.contains("TRUNCATE")) triggeringEventList.add(TRIGGERING_EVENT_TRUNCATE);
        if (triggeringEventString.contains("CREATE")) triggeringEventList.add(TRIGGERING_EVENT_CREATE);
        if (triggeringEventString.contains("ALTER")) triggeringEventList.add(TRIGGERING_EVENT_ALTER);
        if (triggeringEventString.contains("DROP")) triggeringEventList.add(TRIGGERING_EVENT_DROP);
        if (triggeringEventString.contains("RENAME")) triggeringEventList.add(TRIGGERING_EVENT_RENAME);
        if (triggeringEventString.contains("LOGON")) triggeringEventList.add(TRIGGERING_EVENT_LOGON);
        if (triggeringEventString.contains("DDL")) triggeringEventList.add(TRIGGERING_EVENT_DDL);
        if (triggeringEventList.size() == 0) triggeringEventList.add(TRIGGERING_EVENT_UNKNOWN);

        triggeringEvents = triggeringEventList.toArray(new TriggeringEvent[triggeringEventList.size()]);    }

    public void initStatus(ResultSet resultSet) throws SQLException {
        boolean isEnabled = resultSet.getString("IS_ENABLED").equals("Y");
        boolean isValid = resultSet.getString("IS_VALID").equals("Y");
        boolean isDebug = resultSet.getString("IS_DEBUG").equals("Y");
        DBObjectStatusHolder objectStatus = getStatus();
        objectStatus.set(DBObjectStatus.ENABLED, isEnabled);
        objectStatus.set(DBObjectStatus.VALID, isValid);
        objectStatus.set(DBObjectStatus.DEBUG, isDebug);
    }

    @Override
    public void initProperties() {
        DBObjectProperties properties = getProperties();
        properties.set(DBObjectProperty.EDITABLE);
        properties.set(DBObjectProperty.DISABLEABLE);
        properties.set(DBObjectProperty.REFERENCEABLE);
        properties.set(DBObjectProperty.COMPILABLE);
        properties.set(DBObjectProperty.SCHEMA_OBJECT);
    }

    @Override
    public DBContentType getContentType() {
        return DBContentType.CODE;
    }

    public boolean isForEachRow() {
        return isForEachRow;
    }

    public TriggerType getTriggerType() {
        return triggerType;
    }

    public TriggeringEvent[] getTriggeringEvents() {
        return triggeringEvents;
    }

    @Override
    public DBOperationExecutor getOperationExecutor() {
        return new DBOperationExecutor() {
            public void executeOperation(DBOperationType operationType) throws SQLException, DBOperationNotSupportedException {
                ConnectionHandler connectionHandler = getConnectionHandler();
                if (connectionHandler != null) {
                    Connection connection = connectionHandler.getStandaloneConnection(getSchema());
                    DatabaseMetadataInterface metadataInterface = connectionHandler.getInterfaceProvider().getMetadataInterface();
                    if (operationType == DBOperationType.ENABLE) {
                        metadataInterface.enableTrigger(getSchema().getName(), getName(), connection);
                        getStatus().set(DBObjectStatus.ENABLED, true);
                    } else if (operationType == DBOperationType.DISABLE) {
                        metadataInterface.disableTrigger(getSchema().getName(), getName(), connection);
                        getStatus().set(DBObjectStatus.ENABLED, false);
                    } else {
                        throw new DBOperationNotSupportedException(operationType, getObjectType());
                    }
                }
            }
        };
    }

    @Override
    public List<PresentableProperty> getPresentableProperties() {
        List<PresentableProperty> properties = super.getPresentableProperties();
        StringBuilder events = new StringBuilder(triggerType.getName().toLowerCase());
        events.append(" ");
        for (TriggeringEvent triggeringEvent : triggeringEvents) {
            if (triggeringEvent != triggeringEvents[0]) events.append(" or ");
            events.append(triggeringEvent.getName().toUpperCase());
        }

        properties.add(0, new SimplePresentableProperty("Trigger event", events.toString()));
        return properties;
    }

    /*********************************************************
     *                     TreeElement                       *
     *********************************************************/

    public boolean isLeafTreeElement() {
        return true;
    }

    @NotNull
    public List<BrowserTreeNode> buildAllPossibleTreeChildren() {
        return EMPTY_TREE_NODE_LIST;
    }

    private static DBObjectTimestampLoader TIMESTAMP_LOADER = new DBObjectTimestampLoader("TRIGGER");

    /*********************************************************
     *                   DBEditableObject                    *
     ********************************************************/

    public String getCodeParseRootId(DBContentType contentType) {
        return "trigger_definition";
    }

    public DDLFileType getDDLFileType(DBContentType contentType) {
        return DDLFileManager.getInstance(getProject()).getDDLFileType(DDLFileTypeId.TRIGGER);
    }

    public DDLFileType[] getDDLFileTypes() {
        return new DDLFileType[]{getDDLFileType(null)};
    }

    public DBObjectTimestampLoader getTimestampLoader(DBContentType contentType) {
        return TIMESTAMP_LOADER;
    }

}
