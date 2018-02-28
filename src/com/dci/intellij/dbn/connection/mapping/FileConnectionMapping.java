package com.dci.intellij.dbn.connection.mapping;

import org.jdom.Element;

import com.dci.intellij.dbn.common.state.PersistentStateElement;

public class FileConnectionMapping implements PersistentStateElement<Element> {
    String fileUrl = "";
    String connectionId = "";
    String currentSchema = "";

    public FileConnectionMapping(){}

    public FileConnectionMapping(String fileUrl, String connectionId, String currentSchema) {
        this.fileUrl = fileUrl;
        this.connectionId = connectionId;
        this.currentSchema = currentSchema;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public String getCurrentSchema() {
        return currentSchema;
    }

    public void setCurrentSchema(String currentSchema) {
        this.currentSchema = currentSchema;
    }

    /*********************************************
     *            PersistentStateElement         *
     *********************************************/
    public void readState(Element element) {
        fileUrl = element.getAttributeValue("file-url");
        // fixme remove this backward compatibility 
        if (fileUrl == null) fileUrl = element.getAttributeValue("file-path");
        connectionId = element.getAttributeValue("connection-id");
        currentSchema = element.getAttributeValue("current-schema");
    }

    public void writeState(Element element) {
        element.setAttribute("file-url", fileUrl);
        element.setAttribute("connection-id", connectionId == null ? "" : connectionId);
        element.setAttribute("current-schema", currentSchema == null ? "" : currentSchema);
    }
}
