package com.dci.intellij.dbn.language.common;

import java.util.ArrayList;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.common.util.VirtualFileUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionProvider;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingProvider;
import com.dci.intellij.dbn.ddl.DDLFileAttachmentManager;
import com.dci.intellij.dbn.language.common.element.ElementTypeBundle;
import com.dci.intellij.dbn.language.common.element.lookup.ElementLookupContext;
import com.dci.intellij.dbn.language.sql.SQLLanguage;
import com.dci.intellij.dbn.navigation.psi.NavigationPsiCache;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.vfs.DBContentVirtualFile;
import com.dci.intellij.dbn.vfs.DBObjectVirtualFile;
import com.dci.intellij.dbn.vfs.DBParseableVirtualFile;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.intellij.ide.util.EditSourceUtil;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.lang.ParserDefinition;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.SingleRootFileViewProvider;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.testFramework.LightVirtualFile;

public abstract class DBLanguagePsiFile extends PsiFileImpl implements FileConnectionMappingProvider, ConnectionProvider, Disposable {
    private Language language;
    private DBLanguageFileType fileType;
    private ParserDefinition parserDefinition;
    private ConnectionHandler activeConnection;
    private DBSchema currentSchema;
    private DBObjectRef<DBSchemaObject> underlyingObjectRef;

    public DBLanguagePsiFile(FileViewProvider viewProvider, DBLanguageFileType fileType, DBLanguage language) {
        super(viewProvider);
        this.language = findLanguage(language);
        this.fileType = fileType;
        parserDefinition = LanguageParserDefinitions.INSTANCE.forLanguage(language);
        if (parserDefinition == null) {
            throw new RuntimeException("PsiFileBase: language.getParserDefinition() returned null.");
        }
        VirtualFile virtualFile = viewProvider.getVirtualFile();
        if (virtualFile instanceof DBSourceCodeVirtualFile) {
            DBSourceCodeVirtualFile sourceCodeFile = (DBSourceCodeVirtualFile) virtualFile;
            this.underlyingObjectRef = DBObjectRef.from(sourceCodeFile.getObject());
        }

        IFileElementType nodeType = parserDefinition.getFileNodeType();
        //assert nodeType.getLanguage() == this.language;
        init(nodeType, nodeType);
    }

    public void setUnderlyingObject(DBSchemaObject underlyingObject) {
        this.underlyingObjectRef = DBObjectRef.from(underlyingObject);
    }

    public DBObject getUnderlyingObject() {
        VirtualFile virtualFile = getVirtualFile();
        if (virtualFile instanceof DBObjectVirtualFile) {
            DBObjectVirtualFile databaseObjectFile = (DBObjectVirtualFile) virtualFile;
            return databaseObjectFile.getObject();
        }

        if (virtualFile instanceof DBSourceCodeVirtualFile) {
            DBSourceCodeVirtualFile sourceCodeFile = (DBSourceCodeVirtualFile) virtualFile;
            return sourceCodeFile.getObject();
        }

        DDLFileAttachmentManager instance = DDLFileAttachmentManager.getInstance(getProject());
        DBSchemaObject editableObject = instance.getEditableObject(virtualFile);
        if (editableObject != null) {
            return editableObject;
        }


        return DBObjectRef.get(underlyingObjectRef);
    }

    @Nullable
    @Override
    public ConnectionHandler getConnectionHandler() {
        return getActiveConnection();
    }

    public DBLanguagePsiFile(Project project, DBLanguageFileType fileType, @NotNull DBLanguage language) {
        this(createFileViewProvider(project), fileType, language);
    }

    private static SingleRootFileViewProvider createFileViewProvider(Project project) {
        return new SingleRootFileViewProvider(PsiManager.getInstance(project), new LightVirtualFile());
    }

    private Language findLanguage(Language baseLanguage) {
        final FileViewProvider viewProvider = getViewProvider();
        final Set<Language> languages = viewProvider.getLanguages();
        for (final Language actualLanguage : languages) {
            if (actualLanguage.isKindOf(baseLanguage)) {
                return actualLanguage;
            }
        }
        throw new AssertionError(
                "Language " + baseLanguage + " doesn't participate in view provider " + viewProvider + ": " + new ArrayList<Language>(languages));
    }

    public void accept(@NotNull PsiElementVisitor visitor) {
        visitor.visitFile(this);
    }

    @NotNull
    public ParserDefinition getParserDefinition() {
        return parserDefinition;
    }

    @Nullable
    public DBLanguageDialect getLanguageDialect() {
        VirtualFile virtualFile = getVirtualFile();
        if (virtualFile instanceof DBContentVirtualFile) {
            DBContentVirtualFile contentFile = (DBContentVirtualFile) virtualFile;
            return contentFile.getLanguageDialect();
        }
        
        if (language instanceof DBLanguage) {
            DBLanguage dbLanguage = (DBLanguage) language;
            ConnectionHandler connectionHandler = getActiveConnection();
            if (connectionHandler != null) {

                DBLanguageDialect languageDialect = connectionHandler.getLanguageDialect(dbLanguage);
                if (languageDialect != null){
                    return languageDialect;
                }
            } else {
                return dbLanguage.getAvailableLanguageDialects()[0];
            }
        } else if (language instanceof DBLanguageDialect) {
            return (DBLanguageDialect) language;
        }
        
        return null;
    }

    public VirtualFile getVirtualFile() {
        DBLanguagePsiFile originalFile = (DBLanguagePsiFile) getOriginalFile();
        return originalFile == this ?
                super.getVirtualFile() :
                originalFile.getVirtualFile();

    }

    private FileConnectionMappingManager getConnectionMappingManager() {
        return FileConnectionMappingManager.getInstance(getProject());
    }

    @Nullable
    public ConnectionHandler getActiveConnection() {
        VirtualFile file = getVirtualFile();
        if (file != null && !getProject().isDisposed()) {
            if (VirtualFileUtil.isVirtualFileSystem(file)) {
                DBLanguagePsiFile originalFile = (DBLanguagePsiFile) getOriginalFile();
                return originalFile == this ? activeConnection : originalFile.getActiveConnection();
            } else {
                return getConnectionMappingManager().getActiveConnection(file);
            }
        }
        return null;
    }

    public void setActiveConnection(ConnectionHandler activeConnection) {
        VirtualFile file = getVirtualFile();
        if (file != null) {
            if (VirtualFileUtil.isVirtualFileSystem(file)) {
                this.activeConnection = activeConnection;
            } else {
                getConnectionMappingManager().setActiveConnection(file, activeConnection);
            }
        }
    }

    @Nullable
    public DBSchema getCurrentSchema() {
        VirtualFile file = getVirtualFile();
        if (file != null) {
            if (VirtualFileUtil.isVirtualFileSystem(file)) {
                DBLanguagePsiFile originalFile = (DBLanguagePsiFile) getOriginalFile();
                return originalFile == this ? currentSchema : originalFile.getCurrentSchema();
            } else {
                return getConnectionMappingManager().getCurrentSchema(file);
            }
        }
        return null;
    }

    public void setCurrentSchema(DBSchema schema) {
        VirtualFile file = getVirtualFile();
        if (file != null) {
            if (VirtualFileUtil.isVirtualFileSystem(file)) {
                this.currentSchema = schema;
            } else {
                getConnectionMappingManager().setCurrentSchema(file, schema);
            }
        }
    }

    @NotNull
    @Override
    public Language getLanguage() {
        return language;
    }

    public DBLanguage getDBLanguage() {
        return language instanceof DBLanguage ? (DBLanguage) language : null;
    }

    @Override
    public void navigate(boolean requestFocus) {
        Editor selectedEditor = EditorUtil.getSelectedEditor(getProject());
        if (selectedEditor != null) {
            Document document = DocumentUtil.getDocument(getContainingFile());
            Editor[] editors = EditorFactory.getInstance().getEditors(document);
            for (Editor editor : editors) {
                if (editor == selectedEditor) {
                    OpenFileDescriptor descriptor = (OpenFileDescriptor) EditSourceUtil.getDescriptor(this);
                    if (descriptor != null) {
                        descriptor.navigateIn(selectedEditor);
                        return;
                    }
                }
            }
        }
        if (!(getVirtualFile() instanceof DBParseableVirtualFile)) {
            super.navigate(requestFocus);
        }
    }

    @NotNull
    public DBLanguageFileType getFileType() {
        return fileType;
    }

    public ElementTypeBundle getElementTypeBundle() {
        DBLanguageDialect languageDialect = getLanguageDialect();
        languageDialect = CommonUtil.nvl(languageDialect, SQLLanguage.INSTANCE.getMainLanguageDialect());
        return languageDialect.getParserDefinition().getParser().getElementTypes();
    }

    @Override
    public PsiDirectory getParent() {
        DBObject underlyingObject = getUnderlyingObject();
        if (underlyingObject != null) {
            DBObject parentObject = underlyingObject.getParentObject();
            return NavigationPsiCache.getPsiDirectory(parentObject);

        }
        return super.getParent();
    }

    @Override
    public boolean isValid() {
        VirtualFile virtualFile = getViewProvider().getVirtualFile();
        return virtualFile.getFileSystem() instanceof DatabaseFileSystem ?
                virtualFile.isValid() :
                super.isValid();
    }

    public String getParseRootId() {
        VirtualFile virtualFile = getVirtualFile();
        if (virtualFile != null) {
            String parseRootId = virtualFile.getUserData(DBParseableVirtualFile.PARSE_ROOT_ID_KEY);
            if (parseRootId == null && virtualFile instanceof DBSourceCodeVirtualFile) {
                DBSourceCodeVirtualFile sourceCodeFile = (DBSourceCodeVirtualFile) virtualFile;
                parseRootId = sourceCodeFile.getParseRootId();
                if (parseRootId != null) {
                    virtualFile.putUserData(DBParseableVirtualFile.PARSE_ROOT_ID_KEY, parseRootId);
                }
            }

            return parseRootId;
        }
        return null;
    }

    public double getDatabaseVersion() {
        ConnectionHandler activeConnection = getActiveConnection();
        return activeConnection == null ? ElementLookupContext.MAX_DB_VERSION : activeConnection.getDatabaseVersion();
    }

    public static DBLanguagePsiFile createFromText(Project project, String fileName, DBLanguageDialect languageDialect, String text, ConnectionHandler activeConnection, DBSchema currentSchema) {
        PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(project);
        DBLanguagePsiFile psiFile = (DBLanguagePsiFile) psiFileFactory.createFileFromText(fileName, languageDialect, text);
        psiFile.setActiveConnection(activeConnection);
        psiFile.setCurrentSchema(currentSchema);
        return psiFile;
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
        if (!disposed) {
            disposed = true;
        }
    }
}
