package com.dci.intellij.dbn.vfs;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.ReadActionRunner;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionCache;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.ddl.DDLFileType;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.EditorProviderId;
import com.dci.intellij.dbn.editor.code.SourceCodeMainEditor;
import com.dci.intellij.dbn.language.common.DBLanguageFileType;
import com.dci.intellij.dbn.language.sql.SQLFileType;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.common.property.DBObjectProperty;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileSystem;

public class DatabaseFileSystem extends VirtualFileSystem implements ApplicationComponent {
    public static final String PROTOCOL = "db";
    public static final String PROTOCOL_PREFIX = PROTOCOL + "://";

    private static final String ERR = "File manipulation not allowed within database file system!";
    private Map<DBObjectRef, DBEditableObjectVirtualFile> filesCache = new HashMap<DBObjectRef, DBEditableObjectVirtualFile>();

    public DatabaseFileSystem() {
    }

    public static DatabaseFileSystem getInstance() {
        return ApplicationManager.getApplication().getComponent(DatabaseFileSystem.class);
    }
                                                                            
    @NotNull
    public String getProtocol() {
        return PROTOCOL;
    }

    @Nullable
    public VirtualFile findFileByPath(@NotNull @NonNls String url) {
        int startIndex = 0;
        if (url.startsWith(PROTOCOL_PREFIX)) {
            startIndex = PROTOCOL_PREFIX.length();
        }

        int index = url.indexOf('/', startIndex);

        if (index > -1) {
            String connectionId = url.substring(startIndex, index);
            ConnectionHandler connectionHandler = ConnectionCache.findConnectionHandler(connectionId);
            if (connectionHandler != null && !connectionHandler.isDisposed() && connectionHandler.isActive()) {
                String objectPath = url.substring(index + 1);
                if (objectPath.startsWith("null")) {
                    return null;
                } else if (objectPath.startsWith("console#")) {
                    String consoleName = objectPath.substring(8);
                    return connectionHandler.getConsoleBundle().getConsole(consoleName);
                } else if (objectPath.startsWith("session_browser#")) {
                    return connectionHandler.getSessionBrowserFile();
                } else if (objectPath.startsWith("object#")) {
                    String identifier = objectPath.substring(7);
                    DBObjectRef objectRef = new DBObjectRef(connectionId, identifier);
                    DBObject object = objectRef.get();

                    if (object != null && object.getProperties().is(DBObjectProperty.EDITABLE)) {
                        return findDatabaseFile((DBSchemaObject) object);
                    }
                } else {
                    // TODO remove this backward compatibility
                    StringTokenizer path = new StringTokenizer(objectPath, ".");
                    DBObject object = connectionHandler.getObjectBundle().getSchema(path.nextToken());
                    if (object != null) {
                        while (path.hasMoreElements() && object != null) {
                            String token = path.nextToken();
                            if (path.hasMoreTokens()) {
                                int idx = token.indexOf('#');
                                if (idx > -1) {
                                    String type = token.substring(0, idx);
                                    String name = token.substring(idx + 1);
                                    DBObjectType objectType = DBObjectType.getObjectType(type);
                                    object = object.getChildObject(objectType, name, 0, false);
                                } else {
                                    object = object.getChildObject(token, 0, false);
                                }
                            }
                        }
                        // object may have been deleted by another party
                        if (object != null && object.getProperties().is(DBObjectProperty.EDITABLE)) {
                            return findDatabaseFile((DBSchemaObject) object);
                        }
                    }
                }
            }
        }
        return null;
    }

    private static DBEditableObjectVirtualFile createDatabaseFile(final DBSchemaObject object) {
        return new ReadActionRunner<DBEditableObjectVirtualFile>() {
            @Override
            protected DBEditableObjectVirtualFile run() {
                return new DBEditableObjectVirtualFile(object);
            }
        }.start();
    }

    @NotNull
    public DBEditableObjectVirtualFile findDatabaseFile(DBSchemaObject object) {
        DBObjectRef objectRef = object.getRef();
        DBEditableObjectVirtualFile databaseFile = filesCache.get(objectRef);
        if (databaseFile == null ){
            databaseFile = createDatabaseFile(object);

            filesCache.put(objectRef, databaseFile);
        }
        return databaseFile;
    }

    public static boolean isFileOpened(DBSchemaObject object) {
        Project project = object.getProject();
        return DatabaseFileManager.getInstance(project).isFileOpened(object);
    }

    public static String createPath(DBObject object, DBContentType contentType) {
        StringBuilder buffer = new StringBuilder(object.getRef().getFileName());
        DBObject parent = object.getParentObject();
        while (parent != null) {
            buffer.insert(0, '.');
            buffer.insert(0, parent.getName());
            parent = parent.getParentObject();
        }
        buffer.insert(0, " - ");
        if (contentType == DBContentType.CODE_SPEC) {
            buffer.insert(0, " SPEC");
        }

        if (contentType == DBContentType.CODE_BODY) {
            buffer.insert(0, " BODY");
        }

        buffer.insert(0, object.getTypeName().toUpperCase());
        buffer.insert(0, "] ");
        buffer.insert(0, object.getConnectionHandler().getName());
        buffer.insert(0, '[');

        return buffer.toString();
    }

    public static String createPath(DBObject object) {
        StringBuilder buffer = new StringBuilder(object.getRef().getFileName());
        DBObject parent = object.getParentObject();
        while (parent != null) {
            buffer.insert(0, '.');
            buffer.insert(0, parent.getName());
            parent = parent.getParentObject();
        }
        buffer.insert(0, " - ");
        buffer.insert(0, object.getTypeName().toUpperCase());
        buffer.insert(0, "] ");
        buffer.insert(0, object.getConnectionHandler().getName());
        buffer.insert(0, '[');

        return buffer.toString();
    }

    public static String createUrl(DBObject object) {
        if (object == null) {
            return PROTOCOL + "://" + UUID.randomUUID() + "/null";
        } else {
            ConnectionHandler connectionHandler = object.getConnectionHandler();
            String connectionId = connectionHandler == null ? "null" : connectionHandler.getId();
            return PROTOCOL + "://" + connectionId + "/object#" + object.getRef().serialize()/* + "." + getDefaultExtension(object)*/;
        }

/*
        StringBuilder buffer = new StringBuilder(object.getRef().getFileName());
        DBObjectType objectType = object.getObjectType();
        buffer.insert(0, "#");
        buffer.insert(0, objectType);
        buffer.append(".");
        buffer.append(getDefaultExtension(object));

        DBObject parent = object.getParentObject();
        while (parent != null) {
            buffer.insert(0, ".");
            buffer.insert(0, parent.getName());
            objectType = parent.getObjectType();
            if (objectType != DBObjectType.SCHEMA) {
                buffer.insert(0, "#");
                buffer.insert(0, objectType);
            }
            if (parent instanceof DBSchema) break;
            parent = parent.getParentObject();
        }
        buffer.insert(0, "/");
        buffer.insert(0, object.getConnectionHandler().getId());
        buffer.insert(0, "://");
        buffer.insert(0, PROTOCOL);
        return buffer.toString();
*/
    }

    public static String createPath(ConnectionHandler connectionHandler) {
        return '[' + connectionHandler.getName() + ']';

    }

    public static String createUrl(ConnectionHandler connectionHandler) {
        return PROTOCOL + "://" + connectionHandler.getId();
    }

    public static String getDefaultExtension(DBObject object) {
        if (object instanceof DBSchemaObject) {
            DBSchemaObject schemaObject = (DBSchemaObject) object;
            DDLFileType ddlFileType = schemaObject.getDDLFileType(null);
            DBLanguageFileType fileType = ddlFileType == null ? SQLFileType.INSTANCE : ddlFileType.getLanguageFileType();
            return fileType.getDefaultExtension();
        }
        return "";
    }

    /*********************************************************
     *                  VirtualFileSystem                    *
     *********************************************************/

    public void refresh(boolean b) {

    }

    @Nullable
    public VirtualFile refreshAndFindFileByPath(@NotNull String s) {
        return null;
    }

    public void addVirtualFileListener(@NotNull VirtualFileListener listener) {

    }

    public void removeVirtualFileListener(@NotNull VirtualFileListener listener) {

    }

    public void forceRefreshFiles(boolean b, @NotNull VirtualFile... virtualFiles) {

    }

    protected void deleteFile(Object o, @NotNull VirtualFile virtualFile) throws IOException {
        throw new UnsupportedOperationException(ERR);
    }

    protected void moveFile(Object o, @NotNull VirtualFile virtualFile, @NotNull VirtualFile virtualFile1) throws IOException {
        throw new UnsupportedOperationException(ERR);
    }

    protected void renameFile(Object o, @NotNull VirtualFile virtualFile, @NotNull String s) throws IOException {
        throw new UnsupportedOperationException(ERR);
    }

    protected VirtualFile createChildFile(Object o, @NotNull VirtualFile virtualFile, @NotNull String s) throws IOException {
        throw new UnsupportedOperationException(ERR);
    }

    @NotNull
    protected VirtualFile createChildDirectory(Object o, @NotNull VirtualFile virtualFile, @NotNull String s) throws IOException {
        throw new UnsupportedOperationException(ERR);
    }

    protected VirtualFile copyFile(Object o, @NotNull VirtualFile virtualFile, @NotNull VirtualFile virtualFile1, @NotNull String s) throws IOException {
        throw new UnsupportedOperationException(ERR);
    }

    public boolean isReadOnly() {
        return false;
    }

    /*********************************************************
     *                ApplicationComponent                   *
     *********************************************************/

    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.DatabaseFileSystem";
    }

    public void initComponent() {}

    public void disposeComponent() {}

    /*********************************************************
     *              FileEditorManagerListener                *
     *********************************************************/
    public void openEditor(final DBObject object, boolean focusEditor) {
        openEditor(object, null, false, focusEditor);
    }

    public void openEditor(final DBObject object, @Nullable EditorProviderId editorProviderId, boolean focusEditor) {
        openEditor(object, editorProviderId, false, focusEditor);
    }

    public void openEditor(final DBObject object, @Nullable final EditorProviderId editorProviderId, final boolean scrollBrowser, final boolean focusEditor) {
        new ConnectionAction(object) {
            @Override
            public void execute() {
                new BackgroundTask(object.getProject(), "Opening editor", false, true) {
                    @Override
                    public void execute(@NotNull ProgressIndicator progressIndicator) {
                        if (object.getProperties().is(DBObjectProperty.SCHEMA_OBJECT)) {
                            DBObjectListContainer childObjects = object.getChildObjects();
                            if (childObjects != null) childObjects.load();
                            openSchemaObject((DBSchemaObject) object, editorProviderId, scrollBrowser, focusEditor);

                        } else if (object.getParentObject().getProperties().is(DBObjectProperty.SCHEMA_OBJECT)) {
                            DBObjectListContainer childObjects = object.getParentObject().getChildObjects();
                            if (childObjects != null) childObjects.load();
                            openChildObject(object, scrollBrowser, focusEditor, editorProviderId);
                        }

                    }
                }.start();
            }
        }.start();
    }

    private void openSchemaObject(final DBSchemaObject object, final EditorProviderId editorProviderId, final boolean scrollBrowser, final boolean focusEditor) {
        final DBEditableObjectVirtualFile databaseFile = findDatabaseFile(object);
        if (!BackgroundTask.isProcessCancelled()) {
            new SimpleLaterInvocator() {
                @Override
                public void execute() {
                    if (isFileOpened(object) || databaseFile.preOpen()) {
                        DatabaseBrowserManager.AUTOSCROLL_FROM_EDITOR.set(scrollBrowser);
                        FileEditorManager fileEditorManager = FileEditorManager.getInstance(object.getProject());
                        fileEditorManager.openFile(databaseFile, focusEditor);
                        EditorUtil.selectEditor(object.getProject(), null, databaseFile, editorProviderId, focusEditor);
                        DatabaseBrowserManager.AUTOSCROLL_FROM_EDITOR.set(true);
                    }
                }
            }.start();
        }
    }

    private void openChildObject(final DBObject object, final boolean scrollBrowser, final boolean focusEditor, final EditorProviderId editorProviderId) {
        final DBSchemaObject schemaObject = (DBSchemaObject) object.getParentObject();
        final DBEditableObjectVirtualFile databaseFile = findDatabaseFile(schemaObject);
        if (!BackgroundTask.isProcessCancelled()) {
            new SimpleLaterInvocator() {

                @Override
                public void execute() {
                    if (isFileOpened(schemaObject) || databaseFile.preOpen()) {
                        DatabaseBrowserManager.AUTOSCROLL_FROM_EDITOR.set(scrollBrowser);
                        FileEditorManager fileEditorManager = FileEditorManager.getInstance(object.getProject());
                        FileEditor[] fileEditors = fileEditorManager.openFile(databaseFile, focusEditor);
                        for (FileEditor fileEditor : fileEditors) {
                            if (fileEditor instanceof SourceCodeMainEditor) {
                                SourceCodeMainEditor sourceCodeEditor = (SourceCodeMainEditor) fileEditor;
                                EditorUtil.selectEditor(schemaObject.getProject(), fileEditor, databaseFile, editorProviderId, focusEditor);
                                sourceCodeEditor.navigateTo(object);
                                break;
                            }
                        }
                        DatabaseBrowserManager.AUTOSCROLL_FROM_EDITOR.set(true);
                    }

                }
            }.start();
        }
    }

    public void closeEditor(DBSchemaObject object) {
        VirtualFile virtualFile = findDatabaseFile(object);
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(object.getProject());
        fileEditorManager.closeFile(virtualFile);
    }

    public void reopenEditor(DBSchemaObject object) {
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(object.getProject());
        VirtualFile virtualFile = findDatabaseFile(object);
        if (fileEditorManager.isFileOpen(virtualFile)) {
            fileEditorManager.closeFile(virtualFile);
            fileEditorManager.openFile(virtualFile, false);
        }
    }

    public void clearCachedFiles(Project project) {
        Iterator<DBObjectRef> objectRefs = filesCache.keySet().iterator();
        while (objectRefs.hasNext()) {
            DBObjectRef objectRef = objectRefs.next();
            DBEditableObjectVirtualFile file = filesCache.get(objectRef);
            if (file.getProject() == project) {
                objectRefs.remove();
                Disposer.dispose(file);
            }
        }
    }
}
