package com.dci.intellij.dbn.navigation.psi;

import java.util.Map;

import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.Disposable;
import gnu.trove.THashMap;

public class NavigationPsiCache implements Disposable {
    private Map<DBObjectRef, DBObjectPsiFile> objectPsiFiles = new THashMap<DBObjectRef, DBObjectPsiFile>();
    private Map<DBObjectRef, DBObjectPsiDirectory> objectPsiDirectories = new THashMap<DBObjectRef, DBObjectPsiDirectory>();
    private Map<DBObjectList, DBObjectListPsiDirectory> objectListPsiDirectories = new THashMap<DBObjectList, DBObjectListPsiDirectory>();
    private DBConnectionPsiDirectory connectionPsiDirectory;

    public NavigationPsiCache(ConnectionHandler connectionHandler) {
        connectionPsiDirectory = new DBConnectionPsiDirectory(connectionHandler);
    }

    public DBConnectionPsiDirectory getConnectionPsiDirectory() {
        return connectionPsiDirectory;
    }

    private synchronized DBObjectPsiFile lookupPsiFile(DBObject object) {
        DBObjectRef objectRef = object.getRef();
        DBObjectPsiFile psiFile = objectPsiFiles.get(objectRef);
        if (psiFile == null) {
            psiFile = new DBObjectPsiFile(object);
            objectPsiFiles.put(objectRef, psiFile);
        }

        return psiFile;
    }

    private synchronized DBObjectPsiDirectory lookupPsiDirectory(DBObject object) {
        DBObjectRef objectRef = object.getRef();
        DBObjectPsiDirectory psiDirectory = objectPsiDirectories.get(objectRef);
        if (psiDirectory == null) {
            psiDirectory = new DBObjectPsiDirectory(objectRef);
            objectPsiDirectories.put(objectRef, psiDirectory);
        }

        return psiDirectory;
    }
    
    private synchronized DBObjectListPsiDirectory lookupPsiDirectory(DBObjectList objectList) {
        DBObjectListPsiDirectory psiDirectory = objectListPsiDirectories.get(objectList);
        if (psiDirectory == null) {
            psiDirectory = new DBObjectListPsiDirectory(objectList);
            objectListPsiDirectories.put(objectList, psiDirectory);
        }

        return psiDirectory;
    }
    
    
    public static DBObjectPsiFile getPsiFile(DBObject object) {
        return object == null ? null :
                object.getConnectionHandler().getPsiCache().lookupPsiFile(object);
    }

    public static DBObjectPsiDirectory getPsiDirectory(DBObject object) {
        return object == null ? null :
                object.getConnectionHandler().getPsiCache().lookupPsiDirectory(object);
    }
    
    public static DBObjectListPsiDirectory getPsiDirectory(DBObjectList objectList) {
        return objectList == null ? null :
                objectList.getConnectionHandler().getPsiCache().lookupPsiDirectory(objectList);
    }

    public static DBConnectionPsiDirectory getPsiDirectory(ConnectionHandler connectionHandler) {
        return connectionHandler.getPsiCache().connectionPsiDirectory;
    }

    @Override
    public void dispose() {
        DisposerUtil.dispose(connectionPsiDirectory);
        DisposerUtil.dispose(objectListPsiDirectories);
        DisposerUtil.dispose(objectPsiDirectories);
        DisposerUtil.dispose(objectPsiFiles);
    }
}
