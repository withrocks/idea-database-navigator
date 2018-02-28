package com.dci.intellij.dbn.editor.data;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseMessageParserInterface;
import com.dci.intellij.dbn.database.DatabaseObjectIdentifier;
import com.dci.intellij.dbn.object.common.DBObject;

public class DatasetEditorError {
    private boolean isDirty;
    private boolean isNotified;
    private String message;
    private DBObject messageObject;
    private Set<ChangeListener> changeListeners = new HashSet<ChangeListener>();

    public DatasetEditorError(ConnectionHandler connectionHandler, Exception exception) {
        this.message = exception.getMessage();
        if (exception instanceof SQLException) {
            DatabaseMessageParserInterface messageParserInterface = connectionHandler.getInterfaceProvider().getMessageParserInterface();
            DatabaseObjectIdentifier objectIdentifier = messageParserInterface.identifyObject((SQLException) exception);
            if (objectIdentifier != null) {
                messageObject = connectionHandler.getObjectBundle().getObject(objectIdentifier);
            }
        }
    }
    public DatasetEditorError(String message, DBObject messageObject) {
        this.message = message;
        this.messageObject = messageObject;
    }

    public void addChangeListener(ChangeListener changeListener) {
        changeListeners.add(changeListener);
    }

    public void removeChangeListener(ChangeListener changeListener) {
        changeListeners.remove(changeListener);
    }

    public String getMessage() {
        return message;
    }

    public DBObject getMessageObject() {
        return messageObject;
    }


    public void markDirty() {
        isDirty = true;
        ChangeEvent changeEvent = new ChangeEvent(this);
        for (ChangeListener changeListener: changeListeners) {
            changeListener.stateChanged(changeEvent);
        }
    }

    public boolean isDirty() {
        return isDirty;
    }

    public boolean isNotified() {
        return isNotified;
    }

    public void setNotified(boolean notified) {
        isNotified = notified;
    }

    public int hashCode() {
        return message.hashCode();
    }

    public boolean equals(Object obj) {
        if(obj instanceof DatasetEditorError) {
            DatasetEditorError error = (DatasetEditorError) obj;
            return CommonUtil.safeEqual(error.message, message) &&
                   CommonUtil.safeEqual(error.messageObject, messageObject);
        }

        return false;
    }
}
