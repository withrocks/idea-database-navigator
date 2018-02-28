package com.dci.intellij.dbn.vfs;

import javax.swing.Icon;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.DevNullStreams;
import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.GenericDatabaseElement;
import com.dci.intellij.dbn.navigation.psi.NavigationPsiCache;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.UnknownFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class DBObjectListVirtualFile<T extends DBObjectList> extends VirtualFile implements DBVirtualFile {
    private static final byte[] EMPTY_BYTE_CONTENT = new byte[0];
    protected T objectList;

    protected String name;
    protected String path;
    protected String url;

    public DBObjectListVirtualFile(T objectList) {
        this.objectList = objectList;
        this.name = NamingUtil.capitalize(objectList.getName());
    }

    public T getObjectList() {
        return objectList;
    }

    @Nullable
    public ConnectionHandler getConnectionHandler() {
        return objectList.getConnectionHandler();
    }

    public Project getProject() {
        return objectList.getProject();
    }

    public boolean equals(Object obj) {
        if (obj instanceof DBObjectListVirtualFile) {
            DBObjectListVirtualFile objectListFile = (DBObjectListVirtualFile) obj;
            return objectListFile.objectList.equals(objectList);
        }
        return false;
    }

    @Override
    public int hashCode() {
        GenericDatabaseElement parent = objectList.getParent();
        if (parent instanceof DBObject) {
            DBObject object = (DBObject) parent;
            String qualifiedName = object.getQualifiedNameWithType() + '.' + name;
            return qualifiedName.hashCode();
        }

        if (parent instanceof DBObjectBundle) {
            DBObjectBundle objectBundle = (DBObjectBundle) parent;
            String qualifiedName = objectBundle.getConnectionHandler().getName() + '.' + name;
            return qualifiedName.hashCode();
        }

        return super.hashCode();
    }

    /*********************************************************
     *                     VirtualFile                       *
     *********************************************************/
    @NotNull
    @NonNls
    public String getName() {
        return name;
    }

    @Override
    public String getPresentableName() {
        return name;
    }

    @NotNull
    public FileType getFileType() {
        return UnknownFileType.INSTANCE;
    }

    @NotNull
    public DatabaseFileSystem getFileSystem() {
        return DatabaseFileSystem.getInstance();
    }

    @NotNull
    public String getPath() {
        if (path == null) {
            //path = DatabaseFileSystem.createPath(object);
            path="";
        }
        return path;
    }

    @NotNull
    public String getUrl() {
        if (url == null) {
            //url = DatabaseFileSystem.createUrl(object);
            url = "";
        }
        return url;
    }

    public boolean isWritable() {
        return false;
    }

    public boolean isDirectory() {
        return true;
    }

    public boolean isValid() {
        return true;
    }

    @Override
    public boolean isInLocalFileSystem() {
        return false;
    }

    @Nullable
    public VirtualFile getParent() {
        GenericDatabaseElement parent = objectList.getParent();
        if (parent instanceof DBObject) {
            DBObject parentObject = (DBObject) parent;
            return NavigationPsiCache.getPsiDirectory(parentObject).getVirtualFile();
        }

        if (parent instanceof DBObjectBundle) {
            DBObjectBundle objectBundle = (DBObjectBundle) parent;
            return NavigationPsiCache.getPsiDirectory(objectBundle.getConnectionHandler()).getVirtualFile();

        }

        return null;
    }

    public Icon getIcon() {
        return null;
    }

    public VirtualFile[] getChildren() {
        return VirtualFile.EMPTY_ARRAY;
    }

    @NotNull
    public OutputStream getOutputStream(Object o, long l, long l1) throws IOException {
        return DevNullStreams.OUTPUT_STREAM;
    }

    @NotNull
    public byte[] contentsToByteArray() throws IOException {
        return EMPTY_BYTE_CONTENT;
    }

    public long getTimeStamp() {
        return 0;
    }

    public long getLength() {
        return 0;
    }

    public void refresh(boolean b, boolean b1, Runnable runnable) {

    }

    public InputStream getInputStream() throws IOException {
        return DevNullStreams.INPUT_STREAM;
    }

    public long getModificationStamp() {
        return 1;
    }

    @Override
    public String getExtension() {
        return null;
    }
    /********************************************************
     *                    Disposable                        *
     ********************************************************/
    private boolean disposed;

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    @Override
    public void dispose() {
        disposed = true;
        objectList = null;
    }

}

