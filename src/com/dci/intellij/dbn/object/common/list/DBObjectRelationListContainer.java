package com.dci.intellij.dbn.object.common.list;

import java.util.ArrayList;
import java.util.List;

import com.dci.intellij.dbn.common.content.dependency.ContentDependencyAdapter;
import com.dci.intellij.dbn.common.content.dependency.MultipleContentDependencyAdapter;
import com.dci.intellij.dbn.common.content.dependency.SubcontentDependencyAdapterImpl;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoader;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.GenericDatabaseElement;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectRelationType;
import com.intellij.openapi.Disposable;

public class DBObjectRelationListContainer implements Disposable {
    private GenericDatabaseElement owner;
    private List<DBObjectRelationList> objectRelationLists;

    public DBObjectRelationListContainer(GenericDatabaseElement owner) {
        this.owner = owner;
    }

    public List<DBObjectRelationList> getObjectRelationLists() {
        return objectRelationLists;
    }

    private boolean isSupported(DBObjectRelationType objectRelationType) {
        ConnectionHandler connectionHandler = owner.getConnectionHandler();
        DatabaseCompatibilityInterface compatibilityInterface = DatabaseCompatibilityInterface.getInstance(connectionHandler);
        return connectionHandler == null ||
                (compatibilityInterface.supportsObjectType(objectRelationType.getSourceType().getTypeId()) &&
                 compatibilityInterface.supportsObjectType(objectRelationType.getTargetType().getTypeId()));
    }

    public DBObjectRelationList getObjectRelationList(DBObjectRelationType objectRelationType) {
        if (objectRelationLists != null) {
            for (DBObjectRelationList objectRelationList : objectRelationLists) {
                if (objectRelationList.getObjectRelationType() == objectRelationType) {
                    return objectRelationList;
                }
            }
        }
        return null;
    }

    public DBObjectRelationList createObjectRelationList(
            DBObjectRelationType type,
            GenericDatabaseElement parent,
            String name,
            DynamicContentLoader loader,
            DBObjectList ... sourceContents) {
        if (isSupported(type)) {
            ContentDependencyAdapter dependencyAdapter = new MultipleContentDependencyAdapter(sourceContents);
            return createObjectRelationList(type, parent, name, loader, dependencyAdapter);
        }
        return null;
    }

    public DBObjectRelationList createSubcontentObjectRelationList(
            DBObjectRelationType relationType,
            GenericDatabaseElement parent,
            String name,
            DynamicContentLoader loader,
            DBObject sourceContentObject) {
        if (isSupported(relationType)) {
            ContentDependencyAdapter dependencyAdapter = new SubcontentDependencyAdapterImpl(sourceContentObject, relationType);
            return createObjectRelationList(relationType, parent, name, loader, dependencyAdapter);
        }
        return null;
    }


    private DBObjectRelationList createObjectRelationList(
            DBObjectRelationType type,
            GenericDatabaseElement parent,
            String name,
            DynamicContentLoader loader,
            ContentDependencyAdapter dependencyAdapter) {
        if (isSupported(type)) {
            DBObjectRelationList objectRelationList = new DBObjectRelationListImpl(type, parent, name, loader, dependencyAdapter);
            if (objectRelationLists == null) objectRelationLists = new ArrayList<DBObjectRelationList>();
            objectRelationLists.add(objectRelationList);
            return objectRelationList;
        }
        return null;
    }

    public void dispose() {
        DisposerUtil.dispose(objectRelationLists);
        owner = null;
    }

    public void reload(boolean recursive) {
        for (DBObjectRelationList objectRelationList : objectRelationLists) {
            objectRelationList.reload();
        }        
    }
}
