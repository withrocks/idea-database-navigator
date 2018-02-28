package com.dci.intellij.dbn.object.lookup;

import java.lang.ref.WeakReference;
import java.util.StringTokenizer;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.Reference;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionCache;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.openapi.project.Project;

public class DBObjectRef<T extends DBObject> implements Comparable, Reference<T> {
    protected DBObjectRef parent;
    protected DBObjectType objectType;
    protected String objectName;
    protected String connectionId;
    protected int overload;

    private WeakReference<T> reference;
    private int hashCode = -1;

    public DBObjectRef(String connectionId, String identifier) {
        deserialize(connectionId, identifier);
    }

    public DBObjectRef(T object) {
        reference = new WeakReference<T>(object);
        objectType = object.getObjectType();
        objectName = object.getName();
        overload = object.getOverload();
        DBObject parentObj = object.getParentObject();
        if (parentObj != null) {
            parent = parentObj.getRef();
        } else {
            ConnectionHandler connectionHandler = object.getConnectionHandler();
            if (connectionHandler != null) {
                connectionId = connectionHandler.getId();
            }
        }
    }

    public DBObjectRef(DBObjectRef parent, DBObjectType objectType, String objectName) {
        this.parent = parent;
        this.objectType = objectType;
        this.objectName = objectName;
    }

    public DBObjectRef(String connectionId, DBObjectType objectType, String objectName) {
        this.connectionId = connectionId;
        this.objectType = objectType;
        this.objectName = objectName;
    }

    public DBObjectRef() {

    }

    public DBObject getParentObject(DBObjectType objectType) {
        DBObjectRef parentRef = getParentRef(objectType);
        return DBObjectRef.get(parentRef);
    }

    public DBObjectRef getParentRef(DBObjectType objectType) {
        DBObjectRef parent = this;
        while (parent != null) {
            if (parent.objectType.matches(objectType)) {
                return parent;
            }
            parent = parent.parent;
        }
        return null;
    }

    public static DBObjectRef from(Element element) {
        String objectRefDefinition = element.getAttributeValue("object-ref");
        if (StringUtil.isNotEmpty(objectRefDefinition)) {
            DBObjectRef objectRef = new DBObjectRef();
            objectRef.readState(element);
            return objectRef;
        }
        return null;
    }

    public void readState(Element element) {
        if (element != null) {
            String connectionId = element.getAttributeValue("connection-id");
            String objectIdentifier = element.getAttributeValue("object-ref");
            deserialize(connectionId, objectIdentifier);
        }
    }

    public void deserialize(String connectionId, String objectIdentifier) {
        // TODO remove this backward compatibility
        boolean isNewImpl = objectIdentifier.contains("/");
        String tokenizerChar = isNewImpl ? "/" : ".";

        int typeEndIndex = objectIdentifier.indexOf("]");
        StringTokenizer objectTypes = new StringTokenizer(objectIdentifier.substring(1, typeEndIndex), tokenizerChar);

        // TODO remove this backward compatibility
        int objectStartIndex = isNewImpl ? typeEndIndex + 2 : typeEndIndex + 1;
        int objectEndIndex = isNewImpl ? objectIdentifier.lastIndexOf("]") : objectIdentifier.length();
        if (isNewImpl && objectIdentifier.length() > objectEndIndex + 1 && objectIdentifier.charAt(objectEndIndex + 1) == '#') {

        }

        StringTokenizer objectNames = new StringTokenizer(objectIdentifier.substring(objectStartIndex, objectEndIndex), tokenizerChar);

        DBObjectRef objectRef = null;
        while (objectTypes.hasMoreTokens()) {
            String objectTypeName = objectTypes.nextToken();
            String objectName = objectNames.nextToken();
            DBObjectType objectType = DBObjectType.getObjectType(objectTypeName);
            if (objectTypes.hasMoreTokens()) {
                objectRef = objectRef == null ?
                        new DBObjectRef(connectionId, objectType, objectName) :
                        new DBObjectRef(objectRef, objectType, objectName);
            } else {
                if (objectNames.hasMoreTokens()) {
                    String overloadToken = objectNames.nextToken();
                    this.overload = Integer.parseInt(overloadToken);
                }
                this.parent = objectRef;
                this.objectType = objectType;
                this.objectName = objectName;
            }
        }
    }

    public void writeState(Element element) {
        String value = serialize();

        element.setAttribute("connection-id", getConnectionId());
        element.setAttribute("object-ref", value);
    }

    @NotNull
    public String serialize() {
        StringBuilder objectTypes = new StringBuilder(objectType.getName());
        StringBuilder objectNames = new StringBuilder(objectName);

        DBObjectRef parent = this.parent;
        while (parent != null) {
            objectTypes.insert(0, "/");
            objectTypes.insert(0, parent.objectType.getName());
            objectNames.insert(0, "/");
            objectNames.insert(0, parent.objectName);
            parent = parent.parent;
        }

        if (overload > 0) {
            objectNames.append("/");
            objectNames.append(overload);
        }

        return "[" + objectTypes + "]" + "[" + objectNames + "]";
    }

    public String getPath() {
        DBObjectRef parent = this.parent;
        if (parent == null) {
            return objectName;
        } else {
            StringBuilder buffer = new StringBuilder(objectName);
            while(parent != null) {
                buffer.insert(0, ".");
                buffer.insert(0, parent.objectName);
                parent = parent.parent;
            }
            return buffer.toString();
        }
    }

    public String getQualifiedName() {
        return getPath();
    }

    /**
     * qualified object name without schema (e.g. PROGRAM.METHOD)
     */
    public String getQualifiedObjectName() {
        DBObjectRef parent = this.parent;
        if (parent == null || parent.objectType == DBObjectType.SCHEMA) {
            return objectName;
        } else {
            StringBuilder buffer = new StringBuilder(objectName);
            while(parent != null && parent.objectType != DBObjectType.SCHEMA) {
                buffer.insert(0, '.');
                buffer.insert(0, parent.objectName);
                parent = parent.parent;
            }
            return buffer.toString();
        }    }

    public String getQualifiedNameWithType() {
        return objectType.getName() + ' ' + getPath();
    }

    public String getTypePath() {
        DBObjectRef parent = this.parent;
        if (parent == null) {
            return objectType.getName();
        } else {
            StringBuilder buffer = new StringBuilder(objectType.getName());
            while(parent != null) {
                buffer.insert(0, '.');
                buffer.insert(0, parent.objectType.getName());
                parent = parent.parent;
            }
            return buffer.toString();
        }
    }


    public String getConnectionId() {
        return parent == null ? connectionId : parent.getConnectionId();
    }

    public boolean is(@NotNull T object) {
        return object.getRef().equals(this);
    }

    @Nullable
    public static <T extends DBObject> DBObjectRef<T> from(T object) {
        return object == null ? null : object.getRef();
    }

    @Nullable
    public static <T extends DBObject> T get(DBObjectRef<T> objectRef) {
        return objectRef == null ? null : objectRef.get();
    }

    @Nullable
    public T get() {
        return load(null);
    }

    @Nullable
    public T get(Project project) {
        return load(project);
    }

    protected final T load(Project project) {
        T object = reference == null ? null : reference.get();
        if (reference == null || object == null || object.isDisposed()) {
            object = null;
            if (reference != null) {
                reference.clear();
                reference = null;
            }
            ConnectionHandler connectionHandler =
                    project == null || project.isDisposed() ?
                            ConnectionCache.findConnectionHandler(getConnectionId()) :
                            ConnectionManager.getInstance(project).getConnectionHandler(getConnectionId());
            if (connectionHandler != null && !connectionHandler.isDisposed() && connectionHandler.isActive()) {
                object = lookup(connectionHandler);
                if (object != null) {
                    reference = new WeakReference<T>(object);
                }
            }

        }
        return object;
    }

    @Nullable
    protected T lookup(@NotNull ConnectionHandler connectionHandler) {
        DBObject object = null;
        if (parent == null) {
            object = connectionHandler.getObjectBundle().getObject(objectType, objectName, overload);
        } else {
            DBObject parentObject = parent.get();
            if (parentObject != null) {
                object = parentObject.getChildObject(objectType, objectName, overload, true);
            }
        }
        return (T) object;
    }

    public ConnectionHandler lookupConnectionHandler() {
        return ConnectionCache.findConnectionHandler(getConnectionId());
    }

    @Override
    public int compareTo(@NotNull Object o) {
        if (o instanceof DBObjectRef) {
            DBObjectRef that = (DBObjectRef) o;
            int result = this.getConnectionId().compareTo(that.getConnectionId());
            if (result != 0) return result;

            if (this.parent != null && that.parent != null) {
                if (this.parent.equals(that.parent)) {
                    result = this.objectType.compareTo(that.objectType);
                    if (result != 0) return result;

                    int nameCompare = this.objectName.compareTo(that.objectName);
                    return nameCompare == 0 ? this.overload - that.overload : nameCompare;
                } else {
                    return this.parent.compareTo(that.parent);
                }
            } else if(this.parent == null && that.parent == null) {
                result = this.objectType.compareTo(that.objectType);
                if (result != 0) return result;

                return this.objectName.compareTo(that.objectName);
            } else if (this.parent == null) {
                return -1;
            } else if (that.parent == null) {
                return 1;
            }
        }
        return 0;
    }

    protected DBSchema getSchema() {
        return (DBSchema) getParentObject(DBObjectType.SCHEMA);
    }

    public String getSchemaName() {
        DBObjectRef schemaRef = getParentRef(DBObjectType.SCHEMA);
        return schemaRef == null ? null : schemaRef.objectName;
    }

    public int getOverload() {
        return overload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DBObjectRef that = (DBObjectRef) o;
        return this.hashCode() == that.hashCode();
    }

    @Override
    public int hashCode() {
        if (hashCode == -1) {
            hashCode = (getConnectionId() + '#' + serialize()).hashCode();
        }
        return hashCode;
    }

    public String getObjectName() {
        return objectName;
    }

    public String getFileName() {
        if (overload == 0) {
            return objectName;
        } else {
            return objectName + '#' + overload;
        }
    }

    public DBObjectType getObjectType() {
        return objectType;
    }

    public boolean isOfType(DBObjectType objectType) {
        return this.objectType.matches(objectType);
    }
}
