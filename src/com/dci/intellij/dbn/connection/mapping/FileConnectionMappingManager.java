package com.dci.intellij.dbn.connection.mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.list.FiltrableList;
import com.dci.intellij.dbn.common.thread.RunnableTask;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.thread.SimpleTask;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.common.util.VirtualFileUtil;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.ddl.DDLFileAttachmentManager;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.editor.ui.DBLanguageFileEditorToolbarForm;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.options.ConfigId;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.dci.intellij.dbn.vfs.DBConsoleVirtualFile;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileAdapter;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileMoveEvent;
import com.intellij.openapi.vfs.VirtualFilePropertyEvent;
import com.intellij.util.IncorrectOperationException;
import gnu.trove.THashSet;

@State(
    name = "DBNavigator.Project.FileConnectionMappingManager",
    storages = {
        @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/dbnavigator.xml", scheme = StorageScheme.DIRECTORY_BASED),
        @Storage(file = StoragePathMacros.PROJECT_FILE)}
)
public class FileConnectionMappingManager extends VirtualFileAdapter implements ProjectComponent, PersistentStateComponent<Element> {
    private Project project;
    private Set<FileConnectionMapping> mappings = new THashSet<FileConnectionMapping>();
    private Key<ConnectionHandler> ACTIVE_CONNECTION_KEY = Key.create("DBNavigator.ActiveConnection");
    private Key<DBObjectRef<DBSchema>> CURRENT_SCHEMA_KEY = Key.create("DBNavigator.CurrentSchema");

    public boolean setActiveConnection(VirtualFile virtualFile, ConnectionHandler connectionHandler) {
        if (VirtualFileUtil.isLocalFileSystem(virtualFile)) {
            virtualFile.putUserData(ACTIVE_CONNECTION_KEY, connectionHandler);

            String connectionId = connectionHandler == null ? null : connectionHandler.getId();
            String currentSchema = connectionHandler == null ? null  : connectionHandler.getUserName().toUpperCase();

            FileConnectionMapping mapping = lookupMapping(virtualFile);
            if (mapping == null) {
                mapping = new FileConnectionMapping(virtualFile.getUrl(), connectionId, currentSchema);
                mappings.add(mapping);
                return true;
            } else {
                if (mapping.getConnectionId() == null || !mapping.getConnectionId().equals(connectionId)) {
                    mapping.setConnectionId(connectionId);
                    if (connectionHandler != null) {
                        // overwrite current schema only if the existing
                        // selection is not a valid schema for the given connection
                        String currentSchemaName = mapping.getCurrentSchema();
                        DBSchema schema = connectionHandler.isVirtual() || currentSchemaName == null ? null : connectionHandler.getObjectBundle().getSchema(currentSchemaName);
                        setCurrentSchema(virtualFile, schema);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public boolean setCurrentSchema(VirtualFile virtualFile, DBSchema schema) {
        if (VirtualFileUtil.isLocalFileSystem(virtualFile) || VirtualFileUtil.isVirtualFileSystem(virtualFile)) {
            virtualFile.putUserData(CURRENT_SCHEMA_KEY, schema == null ? null : schema.getRef());
            FileConnectionMapping mapping = lookupMapping(virtualFile);
            if (schema != null && mapping != null && !schema.getName().equals(mapping.getCurrentSchema())) {
                mapping.setCurrentSchema(schema.getName());
                return true;
            }
        }

        if (virtualFile instanceof DBConsoleVirtualFile) {
            DBConsoleVirtualFile sqlConsoleFile = (DBConsoleVirtualFile) virtualFile;
            sqlConsoleFile.setCurrentSchema(schema);
            return true;
        }
        return false;
    }

    @Nullable
    public ConnectionHandler getActiveConnection(@NotNull VirtualFile virtualFile) {
        // if the file is a database content file then get the connection from the underlying database object
        if (VirtualFileUtil.isDatabaseFileSystem(virtualFile)) {
            if (virtualFile instanceof FileConnectionMappingProvider) {
                FileConnectionMappingProvider connectionMappingProvider = (FileConnectionMappingProvider) virtualFile;
                return connectionMappingProvider.getActiveConnection();
            }
        }

        if (VirtualFileUtil.isLocalFileSystem(virtualFile)) {
            // if the file is an attached ddl file, then resolve the object which it is
            // linked to, and return its database connection
            DBSchemaObject schemaObject = DDLFileAttachmentManager.getInstance(project).getEditableObject(virtualFile);
            DatabaseFileSystem databaseFileSystem = DatabaseFileSystem.getInstance();
            if (schemaObject != null) {
                ConnectionHandler connectionHandler = schemaObject.getConnectionHandler();
                if (connectionHandler != null && DatabaseFileSystem.isFileOpened(schemaObject)) {
                    return connectionHandler;
                }
            }

            // lookup connection mappings
            ConnectionHandler connectionHandler = virtualFile.getUserData(ACTIVE_CONNECTION_KEY);
            if (connectionHandler == null || connectionHandler.isDisposed()) {
                FileConnectionMapping mapping = lookupMapping(virtualFile);
                if (mapping != null) {
                    ConnectionManager connectionManager = ConnectionManager.getInstance(project);
                    connectionHandler = connectionManager.getConnectionHandler(mapping.getConnectionId());
                    if (connectionHandler == null) {
                        ConnectionBundle connectionBundle = connectionManager.getConnectionBundle();
                        connectionHandler = connectionBundle.getVirtualConnection(mapping.getConnectionId());
                    }

                    if (connectionHandler != null)
                        virtualFile.putUserData(ACTIVE_CONNECTION_KEY, connectionHandler);
                }
            }
            return connectionHandler;
        }

        return null;
    }

    public DBSchema getCurrentSchema(@NotNull VirtualFile virtualFile) {
        // if the file is a database content file then get the schema from the underlying schema object
        if (VirtualFileUtil.isDatabaseFileSystem(virtualFile)) {
            if (virtualFile instanceof FileConnectionMappingProvider) {
                FileConnectionMappingProvider connectionMappingProvider = (FileConnectionMappingProvider) virtualFile;
                return connectionMappingProvider.getCurrentSchema();
            }
        }

        if (VirtualFileUtil.isLocalFileSystem(virtualFile) || VirtualFileUtil.isVirtualFileSystem(virtualFile)) {
            // if the file is an attached ddl file, then resolve the object which it is
            // linked to, and return its parent schema
            DBSchemaObject schemaObject = DDLFileAttachmentManager.getInstance(project).getEditableObject(virtualFile);
            if (schemaObject != null && DatabaseFileSystem.isFileOpened(schemaObject)) {
                return schemaObject.getSchema();
            }

            // lookup schema mappings
            DBObjectRef<DBSchema> currentSchemaRef = virtualFile.getUserData(CURRENT_SCHEMA_KEY);
            if (currentSchemaRef == null) {
                ConnectionHandler connectionHandler = getActiveConnection(virtualFile);
                if (connectionHandler != null && !connectionHandler.isVirtual()) {
                    FileConnectionMapping mapping = lookupMapping(virtualFile);
                    if (mapping != null) {
                        String schemaName = mapping.getCurrentSchema();
                        if (StringUtil.isEmptyOrSpaces(schemaName)) {
                            DBSchema userSchema = connectionHandler.getUserSchema();
                            currentSchemaRef = userSchema == null ? null : userSchema.getRef();
                            schemaName = currentSchemaRef == null ? null : currentSchemaRef.getObjectName();
                        } else {
                            DBSchema schema = connectionHandler.getObjectBundle().getSchema(schemaName);
                            currentSchemaRef = schema == null ? null : schema.getRef();
                        }
                        mapping.setCurrentSchema(schemaName);
                        virtualFile.putUserData(CURRENT_SCHEMA_KEY, currentSchemaRef);
                    }
                }
            } else {
                return currentSchemaRef.get();
            }
        }
        return null;
    }


    private FileConnectionMapping lookupMapping(VirtualFile virtualFile) {
        String fileUrl = virtualFile.getUrl();
        return lookupMapping(fileUrl);
    }

    private FileConnectionMapping lookupMapping(String fileUrl) {
        for (FileConnectionMapping mapping : mappings) {
            if (fileUrl.equals(mapping.getFileUrl())) {
                return mapping;
            }
        }
        return null;
    }

    public void removeMapping(VirtualFile virtualFile) {
        FileConnectionMapping mapping = lookupMapping(virtualFile);
        if (mapping != null) {
            mappings.remove(mapping);
        }
    }

    public List<VirtualFile> getMappedFiles(ConnectionHandler connectionHandler) {
        List<VirtualFile> list = new ArrayList<VirtualFile>();

        VirtualFileManager virtualFileManager = VirtualFileManager.getInstance();
        for (FileConnectionMapping mapping : mappings) {
            String connectionId = mapping.getConnectionId();
            if (connectionHandler.getId().equals(connectionId)) {
                VirtualFile file = virtualFileManager.findFileByUrl(mapping.getFileUrl());
                if (file != null) {
                    list.add(file);
                }
            }
        }
        return list;
    }

    public void selectActiveConnectionForEditor(Editor editor, @Nullable ConnectionHandler connectionHandler) {
        if (editor!= null) {
            Document document = editor.getDocument();
            VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
            if (virtualFile != null && VirtualFileUtil.isLocalFileSystem(virtualFile) ) {
                boolean changed = setActiveConnection(virtualFile, connectionHandler);
                if (changed) {
                    DocumentUtil.touchDocument(editor, true);

                    FileEditor fileEditor = FileEditorManager.getInstance(project).getSelectedEditor(virtualFile);
                    if (fileEditor != null) {
                        DBLanguageFileEditorToolbarForm toolbarForm = fileEditor.getUserData(DBLanguageFileEditorToolbarForm.USER_DATA_KEY);
                        if (toolbarForm != null) {
                            toolbarForm.getAutoCommitLabel().setConnectionHandler(connectionHandler);
                        }
                    }
                }
            }
        }
    }

    public void setCurrentSchemaForEditor(Editor editor, DBSchema schema) {
        if (editor!= null) {
            Document document = editor.getDocument();
            VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
            if (VirtualFileUtil.isLocalFileSystem(virtualFile) || virtualFile instanceof DBConsoleVirtualFile) {
                boolean changed = setCurrentSchema(virtualFile, schema);
                if (changed) {
                    DocumentUtil.touchDocument(editor, false);
                }
            }
        }
    }


    public void selectConnectionAndSchema(@NotNull final DBLanguagePsiFile file, @NotNull final RunnableTask callback) {
        new SimpleLaterInvocator() {
            @Override
            protected void execute() {
                ConnectionHandler activeConnection = file.getActiveConnection();
                if (activeConnection == null || activeConnection.isVirtual()) {
                    String message =
                            activeConnection == null ?
                                    "The file is not linked to any connection.\nTo continue with the statement execution please select a target connection." :
                                    "The connection you selected for this file is a virtual connection, used only to decide the SQL dialect.\n" +
                                            "You can not execute statements against this connection. Please select a proper connection to continue.";


                    MessageUtil.showWarningDialog(project, "No valid Connection", message, new String[]{"Select Connection", "Cancel"}, 0,
                            new SimpleTask() {
                                @Override
                                public void execute() {
                                    if (getResult() == 0) {
                                        promptConnectionSelector(file, false, true,
                                                new SimpleTask() {
                                                    @Override
                                                    public void execute() {
                                                        if (file.getCurrentSchema() == null) {
                                                            promptSchemaSelector(file, callback);
                                                        } else {
                                                            callback.start();
                                                        }

                                                    }
                                                });
                                    }
                                }
                            });

                } else if (file.getCurrentSchema() == null) {
                    String message =
                            "You did not select any schema to run the statement against.\n" +
                                    "To continue with the statement execution please select a schema.";
                    MessageUtil.showWarningDialog(project, "No valid Schema", message, new String[]{"Select Schema", "Cancel"}, 0,
                            new SimpleTask() {
                                @Override
                                public void execute() {
                                    if (getResult() == 0) {
                                        promptSchemaSelector(file, callback);
                                    }
                                }
                            });
                } else {
                    new ConnectionAction(file) {
                        @Override
                        public void execute() {
                            callback.start();
                        }
                    }.start();
                }
            }
        }.start();
    }

    /***************************************************
     *             Select connection popup             *
     ***************************************************/
    public void promptConnectionSelector(DBLanguagePsiFile psiFile, boolean showVirtualConnections, boolean showCreateOption, SimpleTask callback) {
        ConnectionManager connectionManager = ConnectionManager.getInstance(project);
        ConnectionBundle connectionBundle = connectionManager.getConnectionBundle();
        FiltrableList<ConnectionHandler> connectionHandlers = connectionBundle.getConnectionHandlers();

        DefaultActionGroup actionGroup = new DefaultActionGroup();
        if (connectionHandlers.size() > 0) {
            for (ConnectionHandler connectionHandler : connectionHandlers) {
                SelectConnectionAction connectionAction = new SelectConnectionAction(connectionHandler, psiFile, callback);
                actionGroup.add(connectionAction);
            }
        }

        if (showVirtualConnections) {
            actionGroup.addSeparator();
            for (ConnectionHandler virtualConnectionHandler : connectionBundle.getVirtualConnections()) {
                SelectConnectionAction connectionAction = new SelectConnectionAction(virtualConnectionHandler, psiFile, callback);
                actionGroup.add(connectionAction);
            }
        }

        if (showCreateOption) {
            actionGroup.addSeparator();
            actionGroup.add(new SetupConnectionAction());
        }

        ListPopup popupBuilder = JBPopupFactory.getInstance().createActionGroupPopup(
                "Select connection",
                actionGroup,
                SimpleDataContext.getProjectContext(null),
                JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                true);

        popupBuilder.showCenteredInCurrentWindow(project);
    }


    private class SelectConnectionAction extends AnAction {
        private ConnectionHandler connectionHandler;
        private DBLanguagePsiFile file;
        private SimpleTask callback;

        private SelectConnectionAction(ConnectionHandler connectionHandler, DBLanguagePsiFile file, SimpleTask callback) {
            super(connectionHandler.getName(), null, connectionHandler.getIcon());
            this.file = file;
            this.connectionHandler = connectionHandler;
            this.callback = callback;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            DBSchema currentSchema = connectionHandler.getUserSchema();
            file.setActiveConnection(connectionHandler);
            file.setCurrentSchema(currentSchema);
            if (callback != null) {
                callback.start();
            }
        }
    }

    private class SetupConnectionAction extends AnAction {
        private SetupConnectionAction() {
            super("Setup New Connection", null, Icons.CONNECTION_NEW);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            Project project = ActionUtil.getProject(e);
            if (project != null) {
                ProjectSettingsManager settingsManager = ProjectSettingsManager.getInstance(project);
                settingsManager.openProjectSettings(ConfigId.CONNECTIONS);
            }
        }
    }

    /***************************************************
     *             Select schema popup                 *
     ***************************************************/
    public void promptSchemaSelector(final DBLanguagePsiFile psiFile, final RunnableTask callback) throws IncorrectOperationException {
        new ConnectionAction(psiFile) {
            @Override
            public void execute() {
                DefaultActionGroup actionGroup = new DefaultActionGroup();

                ConnectionHandler connectionHandler = psiFile.getActiveConnection();
                if (connectionHandler != null && !connectionHandler.isVirtual() && !connectionHandler.isDisposed()) {
                    List<DBSchema> schemas = connectionHandler.getObjectBundle().getSchemas();
                    for (DBSchema schema  :schemas) {
                        SelectSchemaAction schemaAction = new SelectSchemaAction(psiFile, schema, callback);
                        actionGroup.add(schemaAction);
                    }
                }

                ListPopup popupBuilder = JBPopupFactory.getInstance().createActionGroupPopup(
                        "Select Schema",
                        actionGroup,
                        SimpleDataContext.getProjectContext(null),
                        JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                        true);

                popupBuilder.showCenteredInCurrentWindow(project);
            }
        }.start();
    }


    private class SelectSchemaAction extends AnAction {
        private DBLanguagePsiFile file;
        private DBSchema schema;
        private RunnableTask callback;

        private SelectSchemaAction(DBLanguagePsiFile file, DBSchema schema, RunnableTask callback) {
            super(NamingUtil.enhanceUnderscoresForDisplay(schema.getName()), null, schema.getIcon());
            this.file = file;
            this.schema = schema;
            this.callback = callback;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            file.setCurrentSchema(schema);
            if (callback != null) {
                callback.start();
            }
        }
    }

    /***************************************
     *         VirtualFileListener         *
     ***************************************/

    @Override
    public void fileDeleted(@NotNull VirtualFileEvent event) {
        removeMapping(event.getFile());
    }

    public void fileMoved(@NotNull VirtualFileMoveEvent event) {
        String oldFileUrl = event.getOldParent().getUrl() + "/" + event.getFileName();
        FileConnectionMapping fileConnectionMapping = lookupMapping(oldFileUrl);
        if (fileConnectionMapping != null) {
            fileConnectionMapping.setFileUrl(event.getFile().getUrl());
        }
    }

    public void propertyChanged(@NotNull VirtualFilePropertyEvent event) {
        VirtualFile file = event.getFile();
        VirtualFile parent = file.getParent();
        if (file.isInLocalFileSystem() && parent != null) {
            String oldFileUrl = parent.getUrl() + "/" + event.getOldValue();
            FileConnectionMapping fileConnectionMapping = lookupMapping(oldFileUrl);
            if (fileConnectionMapping != null) {
                fileConnectionMapping.setFileUrl(file.getUrl());
            }
        }
    }

    /***************************************
     *          ProjectComponent         *
     ***************************************/
    private FileConnectionMappingManager(Project project) {
        this.project = project;
        VirtualFileManager.getInstance().addVirtualFileListener(this);
    }

    public static FileConnectionMappingManager getInstance(Project project) {
        return project.getComponent(FileConnectionMappingManager.class);
    }

    @NotNull
    public String getComponentName() {
        return "DBNavigator.Project.FileConnectionMappingManager";
    }

    public void projectOpened() {}
    public void projectClosed() {}
    public void initComponent() {}
    public void disposeComponent() {
        mappings.clear();
        project = null;
    }

    /*********************************************
     *            PersistentStateComponent       *
     *********************************************/
    @Nullable
    @Override
    public Element getState() {
        Element element = new Element("state");
        for (FileConnectionMapping mapping : mappings) {
            Element mappingElement = new Element("mapping");
            mapping.writeState(mappingElement);
            element.addContent(mappingElement);
        }
        return element;
    }

    @Override
    public void loadState(Element element) {
        if (element != null) {
            for (Object child : element.getChildren()) {
                Element mappingElement = (Element) child;
                FileConnectionMapping mapping = new FileConnectionMapping();
                mapping.readState(mappingElement);
                mappings.add(mapping);
            }
        }
    }
}

