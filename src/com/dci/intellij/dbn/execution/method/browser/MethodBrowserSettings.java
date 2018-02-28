package com.dci.intellij.dbn.execution.method.browser;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.options.PersistentConfiguration;
import com.dci.intellij.dbn.connection.ConnectionCache;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;

public class MethodBrowserSettings implements PersistentConfiguration {
    private String connectionId;
    private String schemaName;
    private DBObjectRef<DBMethod> method;
    private Map<DBObjectType, Boolean> objectVisibility = new EnumMap<DBObjectType, Boolean>(DBObjectType.class);

    public MethodBrowserSettings() {
        objectVisibility.put(DBObjectType.FUNCTION, true);
        objectVisibility.put(DBObjectType.PROCEDURE, true);
    }

    public ConnectionHandler getConnectionHandler() {
        return ConnectionCache.findConnectionHandler(connectionId);
    }

    public void setConnectionHandler(ConnectionHandler connectionHandler) {
        this.connectionId = connectionHandler == null ? null : connectionHandler.getId();
    }

    public DBSchema getSchema() {
        return getConnectionHandler() == null || schemaName == null ? null : getConnectionHandler().getObjectBundle().getSchema(schemaName);
    }

    public Set<DBObjectType> getVisibleObjectTypes() {
        Set<DBObjectType> objectTypes = EnumSet.noneOf(DBObjectType.class);
        for (DBObjectType objectType : objectVisibility.keySet()) {
            if (objectVisibility.get(objectType)) {
                objectTypes.add(objectType);
            }
        }
        return objectTypes;
    }

    public boolean getObjectVisibility(DBObjectType objectType) {
        return objectVisibility.get(objectType);
    }

    public boolean setObjectVisibility(DBObjectType objectType, boolean visibility) {
        if (getObjectVisibility(objectType) != visibility) {
            objectVisibility.put(objectType, visibility);
            return true;
        }
        return false;        
    }

    public void setSchema(DBSchema schema) {
        this.schemaName = schema == null ? null : schema.getName();
    }

    @Nullable
    public DBMethod getMethod() {
        return method == null ? null : method.get();
    }

    public void setMethod(DBMethod method) {
        this.method = new DBObjectRef<DBMethod>(method);
    }

    public void readConfiguration(Element element) {
        connectionId = element.getAttributeValue("connection-id");
        schemaName = element.getAttributeValue("schema");

        Element methodElement = element.getChild("selected-method");
        if (methodElement != null) {
            method = new DBObjectRef<DBMethod>();
            method.readState(methodElement);
        }
    }

    public void writeConfiguration(Element element) {
        ConnectionHandler connectionHandler = getConnectionHandler();
        if (connectionHandler != null) element.setAttribute("connection-id", connectionHandler.getId());
        if (schemaName != null) element.setAttribute("schema", schemaName);
        if(method != null) {
            Element methodElement = new Element("selected-method");
            method.writeState(methodElement);
            element.addContent(methodElement);
        }
    }
}
