package com.dci.intellij.dbn.database.common;

import com.dci.intellij.dbn.database.DatabaseObjectIdentifier;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;

import java.util.ArrayList;
import java.util.List;

public class DatabaseObjectIdentifierImpl implements DatabaseObjectIdentifier {
    private DBObjectType[] objectTypes;
    private String[] objectNames;

    public DatabaseObjectIdentifierImpl(DBObject object) {
        List<DBObject> chain = new ArrayList<DBObject>();
        chain.add(object);

        DBObject parentObject = object.getParentObject();
        while (parentObject != null) {
            chain.add(0, parentObject);
            parentObject = parentObject.getParentObject();
        }
        int length = chain.size();
        objectTypes = new DBObjectType[length];
        objectNames = new String[length];

        for (int i = 0; i<length; i++) {
            DBObject chainObject = chain.get(i);
            objectTypes[i] = chainObject.getObjectType();
            objectNames[i] = chainObject.getName();
        }
    }

    public int getObjectTypeIndex(DBObjectType objectType) {
        for (int i=0; i< objectTypes.length; i++) {
            if (objectTypes[i] == objectType) {
                return i;
            }
        }
        return -1;
    }

    public int getObjectTypeIndex(DBObjectType[] objectTypes) {
        for (DBObjectType objectType : objectTypes) {
            int index = getObjectTypeIndex(objectType);
            if (index > -1) {
                return index;
            }
        }
        return -1;
    }

    public String getObjectName(DBObjectType objectType) {
        int index = getObjectTypeIndex(objectType);
        return index > -1 ? objectNames[index] : null;
    }

    public String getObjectName(DBObjectType[] objectTypes) {
        int index = getObjectTypeIndex(objectTypes);
        return index > -1 ? objectNames[index] : null;
    }


    public DatabaseObjectIdentifierImpl(DBObjectType[] objectTypes, String[] objectNames) {
        this.objectNames = objectNames;
        this.objectTypes = objectTypes;
    }

    public String[] getObjectNames() {
        return objectNames;
    }

    public void setObjectNames(String[] objectNames) {
        this.objectNames = objectNames;
    }

    public DBObjectType[] getObjectTypes() {
        return objectTypes;
    }

    public void setObjectTypes(DBObjectType[] objectTypes) {
        this.objectTypes = objectTypes;
    }

    public String getQualifiedType() {
        StringBuilder buffer = new StringBuilder();
        for (DBObjectType objectType : objectTypes) {
            if(buffer.length() > 0) {
                buffer.append('.');
            }
            String typeName = objectType.getName();
            buffer.append(typeName);
        }

        return buffer.toString();
    }

    public String getQualifiedName() {
        StringBuilder buffer = new StringBuilder();
        for (String objectName : objectNames) {
            if(buffer.length() > 0) {
                buffer.append('.');
            }
            buffer.append(objectName);
        }

        return buffer.toString();
    }

    public boolean matches(DBObject object) {
        int index = objectTypes.length - 1;
        while (object != null && index > -1) {
            if (object.getObjectType() == objectTypes[index] &&
                object.getName().equalsIgnoreCase(objectNames[index])) {
                object = object.getParentObject();
                index--;
            } else {
                return false;
            }
        }
        return true;
    }
}
