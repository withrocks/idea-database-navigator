package com.dci.intellij.dbn.vfs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.thread.ConditionalLaterInvocator;
import com.dci.intellij.dbn.common.thread.SimpleTask;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingProvider;
import com.dci.intellij.dbn.ddl.DDLFileAttachmentManager;
import com.dci.intellij.dbn.ddl.DDLFileType;
import com.dci.intellij.dbn.ddl.ObjectToDDLContentSynchronizer;
import com.dci.intellij.dbn.ddl.options.DDLFileGeneralSettings;
import com.dci.intellij.dbn.ddl.options.DDLFileSettings;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.code.SourceCodeEditor;
import com.dci.intellij.dbn.editor.code.SourceCodeManager;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilter;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilterManager;
import com.dci.intellij.dbn.editor.data.options.DataEditorSettings;
import com.dci.intellij.dbn.language.sql.SQLFileType;
import com.dci.intellij.dbn.object.DBDataset;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.common.property.DBObjectProperty;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.impl.FileDocumentManagerImpl;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;

public class DBEditableObjectVirtualFile extends DBObjectVirtualFile<DBSchemaObject> implements FileConnectionMappingProvider {
    public ThreadLocal<Document> FAKE_DOCUMENT = new ThreadLocal<Document>();
    private List<DBContentVirtualFile> contentFiles;

    public DBEditableObjectVirtualFile(DBSchemaObject object) {
        super(object);
    }

    @Nullable
    public ConnectionHandler getActiveConnection() {
        return getConnectionHandler();
    }

    @Nullable
    public DBSchema getCurrentSchema() {
        return getObject().getSchema();
    }

    public boolean preOpen() {
        final DBSchemaObject object = getObject();
        if (object != null) {
            Project project = object.getProject();
            DBContentType contentType = object.getContentType();
            if (contentType == DBContentType.DATA) {
                DBDataset dataset = (DBDataset) object;
                DatasetFilterManager filterManager = DatasetFilterManager.getInstance(project);
                DatasetFilter filter = filterManager.getActiveFilter(dataset);

                if (filter == null) {
                    DataEditorSettings settings = DataEditorSettings.getInstance(project);
                    if (settings.getFilterSettings().isPromptFilterDialog()) {
                        int exitCode = filterManager.openFiltersDialog(dataset, true, false, settings.getFilterSettings().getDefaultFilterType());
                        return exitCode != DialogWrapper.CANCEL_EXIT_CODE;
                    }
                }
            }
            else if (contentType.isOneOf(DBContentType.CODE, DBContentType.CODE_SPEC_AND_BODY)) {

                DDLFileGeneralSettings ddlFileSettings = DDLFileSettings.getInstance(project).getGeneralSettings();
                ConnectionHandler connectionHandler = object.getConnectionHandler();
                boolean ddlFileBinding = connectionHandler.getSettings().getDetailSettings().isEnableDdlFileBinding();
                if (ddlFileBinding && ddlFileSettings.isLookupDDLFilesEnabled()) {
                    List<VirtualFile> attachedDDLFiles = getAttachedDDLFiles();
                    if (attachedDDLFiles == null || attachedDDLFiles.isEmpty()) {
                        final DDLFileAttachmentManager fileAttachmentManager = DDLFileAttachmentManager.getInstance(project);
                        List<VirtualFile> virtualFiles = fileAttachmentManager.lookupDetachedDDLFiles(object);
                        if (virtualFiles.size() > 0) {
                            int exitCode = DDLFileAttachmentManager.showFileAttachDialog(object, virtualFiles, true);
                            return exitCode != DialogWrapper.CANCEL_EXIT_CODE;
                        } else if (ddlFileSettings.isCreateDDLFilesEnabled()) {
                            MessageUtil.showQuestionDialog(
                                    project, "No DDL file found",
                                    "Could not find any DDL file for " + object.getQualifiedNameWithType() + ". Do you want to create one? \n" +
                                    "(You can disable this check in \"DDL File\" options)", MessageUtil.OPTIONS_YES_NO, 0,
                                    new SimpleTask() {
                                        @Override
                                        public void execute() {
                                            if (getResult() == DialogWrapper.OK_EXIT_CODE) {
                                                fileAttachmentManager.createDDLFile(object);
                                            }
                                        }
                                    });
                        }
                    }
                }
            }
        } else {
            return false;
        }

        return true;
    }

    public synchronized List<DBContentVirtualFile> getContentFiles() {
        if (contentFiles == null) {
            contentFiles = new ArrayList<DBContentVirtualFile>();
            DBContentType objectContentType = getObject().getContentType();
            if (objectContentType.isBundle()) {
                DBContentType[] contentTypes = objectContentType.getSubContentTypes();
                for (DBContentType contentType : contentTypes) {
                    DBContentVirtualFile virtualFile =
                            contentType.isCode() ? new DBSourceCodeVirtualFile(this, contentType) :
                            contentType.isData() ? new DBDatasetVirtualFile(this, contentType) : null;
                    contentFiles.add(virtualFile);
                }
            } else {
                DBContentVirtualFile virtualFile =
                        objectContentType.isCode() ? new DBSourceCodeVirtualFile(this, objectContentType) :
                        objectContentType.isData() ? new DBDatasetVirtualFile(this, objectContentType) : null;
                contentFiles.add(virtualFile);
            }
        }
        return contentFiles;
    }

    @Nullable
    public List<VirtualFile> getAttachedDDLFiles() {
        DBSchemaObject object = getObject();
        if (object != null) {
            DDLFileAttachmentManager fileAttachmentManager = DDLFileAttachmentManager.getInstance(object.getProject());
            if (object.getProperties().is(DBObjectProperty.EDITABLE)) {
                return fileAttachmentManager.getAttachedDDLFiles(object);
            }
        }
        return null;
    }

    public void updateDDLFiles() {
        for (DBContentVirtualFile contentFile : getContentFiles()) {
            updateDDLFiles(contentFile.getContentType());
        }
    }

    public void updateDDLFiles(final DBContentType sourceContentType) {
        DDLFileSettings ddlFileSettings = DDLFileSettings.getInstance(getProject());
        if (ddlFileSettings.getGeneralSettings().isSynchronizeDDLFilesEnabled()) {
            new ConditionalLaterInvocator() {
                public void execute() {
                    ObjectToDDLContentSynchronizer synchronizer = new ObjectToDDLContentSynchronizer(sourceContentType, DBEditableObjectVirtualFile.this);
                    ApplicationManager.getApplication().runWriteAction(synchronizer);
                }
            }.start();
        }
    }

    public DBContentVirtualFile getContentFile(DBContentType contentType) {
        for (DBContentVirtualFile contentFile : getContentFiles()) {
            if (contentFile.getContentType() == contentType) {
                return contentFile;
            }
        }
        return null;
    }

    /*********************************************************
     *                     VirtualFile                       *
     *********************************************************/
    @NotNull
    public FileType getFileType() {
        DBSchemaObject object = getObject();
        DDLFileType type = object == null ? null : object.getDDLFileType(null);
        return type == null ? SQLFileType.INSTANCE : type.getLanguageFileType();
    }

    public boolean isWritable() {
        return true;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    public DBContentVirtualFile getDebuggableContentFile(){
        DBContentType contentType = getObject().getContentType();
        if (contentType == DBContentType.CODE) {
            return getContentFile(DBContentType.CODE);
        }

        if (contentType == DBContentType.CODE_SPEC_AND_BODY) {
            return getContentFile(DBContentType.CODE_BODY);
        }
        return null;
    }

    @NotNull
    public byte[] contentsToByteArray() throws IOException {
        DBContentType mainContentType = getMainContentType();
        if (mainContentType != null) {
            DBContentVirtualFile contentFile = getContentFile(mainContentType);
            return contentFile == null ? new byte[0] : contentFile.contentsToByteArray();
        }
        return new byte[0];
    }

    @Override
    public <T> T getUserData(@NotNull Key<T> key) {
        if (key == FileDocumentManagerImpl.HARD_REF_TO_DOCUMENT_KEY) {
            DBContentType mainContentType = getMainContentType();
            boolean isCode = mainContentType == DBContentType.CODE || mainContentType == DBContentType.CODE_BODY;
            if (isCode) {
                if (FAKE_DOCUMENT.get() != null) {
                    return (T) FAKE_DOCUMENT.get();
                }

                DBContentVirtualFile mainContentFile = getMainContentFile();
                if (mainContentFile != null) {
                    Document document = DocumentUtil.getDocument(mainContentFile);
                    return (T) document;
                }
            }
        }
        return super.getUserData(key);
    }

    public DBContentType getMainContentType() {
        DBSchemaObject object = getObject();
        if (object == null) {
            return DBContentType.CODE;
        } else {
            DBContentType contentType = object.getContentType();
            return
                contentType == DBContentType.CODE ? DBContentType.CODE :
                contentType == DBContentType.CODE_SPEC_AND_BODY ? DBContentType.CODE_BODY : null;
        }
    }

    public DBContentVirtualFile getMainContentFile() {
        DBContentType mainContentType = getMainContentType();
        return getContentFile(mainContentType);
    }

    @Override
    public String getExtension() {
        return "psql";
    }

    @Override
    public void dispose() {
        super.dispose();
        DisposerUtil.dispose(contentFiles);
    }


    public boolean isModified() {
        for (DBContentVirtualFile contentVirtualFile : getContentFiles()) {
           if (contentVirtualFile.isModified()) {
               return true;
           }
        }
        return false;
    }

    public void saveChanges() {
        FileDocumentManager.getInstance().saveAllDocuments();
        Project project = getProject();
        if (project != null) {
            SourceCodeManager sourceCodeManager = SourceCodeManager.getInstance(project);
            FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
            for (DBContentVirtualFile contentVirtualFile : getContentFiles()) {
                if (contentVirtualFile.isModified() && contentVirtualFile instanceof DBSourceCodeVirtualFile) {
                    FileEditor[] fileEditors = fileEditorManager.getEditors(this);
                    for (FileEditor fileEditor : fileEditors) {
                        if (fileEditor instanceof SourceCodeEditor) {
                            SourceCodeEditor sourceCodeEditor = (SourceCodeEditor) fileEditor;
                            sourceCodeManager.updateSourceToDatabase(sourceCodeEditor, (DBSourceCodeVirtualFile) contentVirtualFile);
                            break;
                        }
                    }
                }
            }
        }
    }
}

