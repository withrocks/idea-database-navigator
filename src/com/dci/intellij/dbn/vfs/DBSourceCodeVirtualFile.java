package com.dci.intellij.dbn.vfs;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.DevNullStreams;
import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.SimpleTask;
import com.dci.intellij.dbn.common.thread.WriteActionRunner;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionProvider;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.code.SourceCodeContent;
import com.dci.intellij.dbn.editor.code.SourceCodeEditor;
import com.dci.intellij.dbn.editor.code.SourceCodeLoadListener;
import com.dci.intellij.dbn.editor.code.SourceCodeManager;
import com.dci.intellij.dbn.editor.code.SourceCodeOffsets;
import com.dci.intellij.dbn.execution.statement.DataDefinitionChangeListener;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.impl.FileDocumentManagerImpl;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.PsiDocumentManagerImpl;

public class DBSourceCodeVirtualFile extends DBContentVirtualFile implements DBParseableVirtualFile, DocumentListener, ConnectionProvider {

    private String originalContent;
    private String lastSavedContent;
    private String content;
    private Timestamp changeTimestamp;
    private String sourceLoadError;
    public int documentHashCode;
    private SourceCodeOffsets offsets;

    public DBSourceCodeVirtualFile(final DBEditableObjectVirtualFile databaseFile, DBContentType contentType) {
        super(databaseFile, contentType);
        DBSchemaObject object = getObject();
        if (object != null) {
            updateChangeTimestamp();
            setCharset(databaseFile.getConnectionHandler().getSettings().getDetailSettings().getCharset());
            try {
                SourceCodeManager sourceCodeManager = SourceCodeManager.getInstance(getProject());
                SourceCodeContent sourceCodeContent = sourceCodeManager.loadSourceFromDatabase(object, contentType);
                content = sourceCodeContent.getSourceCode();
                offsets = sourceCodeContent.getOffsets();
                sourceLoadError = null;
            } catch (SQLException e) {
                content = "";
                sourceLoadError = e.getMessage();
                //MessageUtil.showErrorDialog("Could not load sourcecode for " + object.getQualifiedNameWithType() + " from database.", e);
            }
        } else {
            sourceLoadError = "Could not find object in database";
        }
        EventManager.subscribe(databaseFile.getProject(), DataDefinitionChangeListener.TOPIC, dataDefinitionChangeListener);
    }

    private final DataDefinitionChangeListener dataDefinitionChangeListener = new DataDefinitionChangeListener() {
        @Override
        public void dataDefinitionChanged(DBSchema schema, DBObjectType objectType) {
/*
            if (schema.getConnectionHandler() == connectionHandler) {
                DBObjectList childObjectList = schema.getChildObjectList(objectType);
                if (childObjectList != null && childObjectList.isLoaded()) {
                    childObjectList.reload();
                }

                Set<DBObjectType> childObjectTypes = objectType.getChildren();
                for (DBObjectType childObjectType : childObjectTypes) {
                    DBObjectListContainer childObjects = schema.getChildObjects();
                    if (childObjects != null) {
                        childObjectList = childObjects.getHiddenObjectList(childObjectType);
                        if (childObjectList != null && childObjectList.isLoaded()) {
                            childObjectList.reload();
                        }
                    }
                }
            }
*/
        }

        @Override
        public void dataDefinitionChanged(@NotNull final DBSchemaObject schemaObject) {
            if (schemaObject.equals(getObject())) {
                if (isModified()) {
                    MessageUtil.showQuestionDialog(
                            getProject(), "Unsaved changes",
                            "The " + schemaObject.getQualifiedNameWithType() + " has been updated in database. You have unsaved changes in the object editor.\n" +
                            "Do you want to discard the changes and reload the updated database version?",
                            new String[]{"Reload", "Keep changes"}, 0,
                            new SimpleTask() {
                                @Override
                                public void execute() {
                                    if (getResult() == 0) {
                                        reloadAndUpdateEditors(false);
                                    }
                                }
                            });
                } else {
                    reloadAndUpdateEditors(true);
                }
            }
        }
    };

    private void reloadAndUpdateEditors(boolean startInBackground) {
        new BackgroundTask(getProject(), "Reloading object source code", startInBackground) {
            @Override
            protected void execute(@NotNull ProgressIndicator progressIndicator) throws InterruptedException {
                boolean reloaded = reloadFromDatabase();
                if (reloaded) {
                    new WriteActionRunner() {
                        public void run() {
                            FileEditorManager fileEditorManager = FileEditorManager.getInstance(getProject());
                            FileEditor[] allEditors = fileEditorManager.getAllEditors(getMainDatabaseFile());
                            for (FileEditor fileEditor : allEditors) {
                                if (fileEditor instanceof SourceCodeEditor) {
                                    SourceCodeEditor sourceCodeEditor = (SourceCodeEditor) fileEditor;
                                    if (sourceCodeEditor.getVirtualFile().equals(DBSourceCodeVirtualFile.this)) {
                                        Editor editor = sourceCodeEditor.getEditor();
                                        editor.getDocument().setText(content);
                                        setModified(false);
                                    }
                                }
                            }
                        }
                    }.start();
                }
            }
        }.start();
    }

    public SourceCodeOffsets getOffsets() {
        return offsets;
    }

    public PsiFile initializePsiFile(DatabaseFileViewProvider fileViewProvider, Language language) {
        ConnectionHandler connectionHandler = getConnectionHandler();
        String parseRootId = getParseRootId();
        if (connectionHandler != null && parseRootId != null) {
            DBLanguageDialect languageDialect = connectionHandler.resolveLanguageDialect(language);
            if (languageDialect != null) {
                DBSchemaObject underlyingObject = getObject();
                fileViewProvider.getVirtualFile().putUserData(PARSE_ROOT_ID_KEY, getParseRootId());

                DBLanguagePsiFile file = (DBLanguagePsiFile) languageDialect.getParserDefinition().createFile(fileViewProvider);
                file.setUnderlyingObject(underlyingObject);
                fileViewProvider.forceCachedPsi(file);
                Document document = DocumentUtil.getDocument(fileViewProvider.getVirtualFile());
                document.putUserData(FILE_KEY, getMainDatabaseFile());
                PsiDocumentManagerImpl.cachePsi(document, file);
                return file;
            }
        }
        return null;
    }

    public String getParseRootId() {
        DBSchemaObject schemaObject = getObject();
        return schemaObject == null ? null : schemaObject.getCodeParseRootId(contentType);
    }

    public DBLanguagePsiFile getPsiFile() {
        return (DBLanguagePsiFile) PsiUtil.getPsiFile(getProject(), this);
    }

    public void updateChangeTimestamp() {
        DBSchemaObject object = getObject();
        if (object != null) {
            try {
                Timestamp timestamp = object.loadChangeTimestamp(getContentType());
                if (timestamp != null) {
                    changeTimestamp = timestamp;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public Timestamp getChangeTimestamp() {
        return changeTimestamp;
    }

    public String getOriginalContent() {
        return originalContent;
    }

    public String getLastSavedContent() {
        return lastSavedContent == null ? originalContent : lastSavedContent;
    }

    public void setContent(String content) {
        if (originalContent == null) {
            originalContent = this.content;
        }
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public boolean reloadFromDatabase() {
        Project project = getProject();
        try {
            updateChangeTimestamp();
            originalContent = null;

            DBSchemaObject object = getObject();
            if (object != null) {
                SourceCodeManager sourceCodeManager = SourceCodeManager.getInstance(getProject());
                SourceCodeContent sourceCodeContent = sourceCodeManager.loadSourceFromDatabase(object, contentType);
                content = sourceCodeContent.getSourceCode();
                offsets = sourceCodeContent.getOffsets();

                getMainDatabaseFile().updateDDLFiles(getContentType());
                setModified(false);
                sourceLoadError = null;
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            sourceLoadError = e.getMessage();
            DBSchemaObject object = mainDatabaseFile.getObject();
            if (object != null) {
                MessageUtil.showErrorDialog(project, "Could not reload sourcecode for " + object.getQualifiedNameWithType() + " from database.", e);
            }
            return false;
        } finally {
            if (project != null && !project.isDisposed()) {
                EventManager.notify(project, SourceCodeLoadListener.TOPIC).sourceCodeLoaded(mainDatabaseFile);
            }
        }
    }

    public void updateToDatabase() throws SQLException {
        DBSchemaObject object = getObject();
        if (object != null) {
            object.executeUpdateDDL(getContentType(), getLastSavedContent(), content);
            updateChangeTimestamp();
            getMainDatabaseFile().updateDDLFiles(getContentType());
            setModified(false);
            lastSavedContent = content;
        }
    }

    @NotNull
    public OutputStream getOutputStream(Object o, long l, long l1) throws IOException {
        return DevNullStreams.OUTPUT_STREAM;
    }

    @NotNull
    public byte[] contentsToByteArray() {
        return content.getBytes(getCharset());
    }

    public long getLength() {
        return content.length();
    }

    public int getDocumentHashCode() {
        return documentHashCode;
    }

    public void setDocumentHashCode(int documentHashCode) {
        this.documentHashCode = documentHashCode;
    }

    public String getSourceLoadError() {
        return sourceLoadError;
    }

    @Override
    public <T> void putUserData(@NotNull Key<T> key, T value) {
        if (key == FileDocumentManagerImpl.HARD_REF_TO_DOCUMENT_KEY && contentType.isOneOf(DBContentType.CODE, DBContentType.CODE_BODY) ) {
            mainDatabaseFile.putUserData(FileDocumentManagerImpl.HARD_REF_TO_DOCUMENT_KEY, (Document) value);
        }
        super.putUserData(key, value);
    }

    /**
     * ******************************************************
     * DocumentListener                    *
     * *******************************************************
     */
    public void beforeDocumentChange(DocumentEvent event) {

    }

    public void documentChanged(DocumentEvent event) {
        setModified(true);
    }

    @Override
    public void dispose() {
        EventManager.unsubscribe(dataDefinitionChangeListener);
        originalContent = null;
        lastSavedContent = null;
        content = "";
        super.dispose();
    }
}
