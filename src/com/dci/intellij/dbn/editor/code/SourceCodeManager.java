package com.dci.intellij.dbn.editor.code;

import java.sql.SQLException;
import java.sql.Timestamp;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.editor.BasicTextEditor;
import com.dci.intellij.dbn.common.editor.document.OverrideReadonlyFragmentModificationHandler;
import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.thread.WriteActionRunner;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseDDLInterface;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.EditorProviderId;
import com.dci.intellij.dbn.execution.compiler.CompilerAction;
import com.dci.intellij.dbn.execution.compiler.CompilerActionSource;
import com.dci.intellij.dbn.execution.compiler.DatabaseCompilerManager;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.dci.intellij.dbn.language.editor.DBLanguageFileEditorListener;
import com.dci.intellij.dbn.language.psql.PSQLFile;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.common.property.DBObjectProperty;
import com.dci.intellij.dbn.object.common.status.DBObjectStatus;
import com.dci.intellij.dbn.vfs.DBContentVirtualFile;
import com.dci.intellij.dbn.vfs.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.diff.ActionButtonPresentation;
import com.intellij.openapi.diff.DiffManager;
import com.intellij.openapi.diff.DiffRequestFactory;
import com.intellij.openapi.diff.MergeRequest;
import com.intellij.openapi.diff.impl.mergeTool.DiffRequestFactoryImpl;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;

@State(
    name = "DBNavigator.Project.SourceCodeManager",
    storages = {
        @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/dbnavigator.xml", scheme = StorageScheme.DIRECTORY_BASED),
        @Storage(file = StoragePathMacros.PROJECT_FILE)}
)
public class SourceCodeManager extends AbstractProjectComponent implements PersistentStateComponent<Element> {

    private DBLanguageFileEditorListener fileEditorListener;

    public static SourceCodeManager getInstance(@NotNull Project project) {
        return project.getComponent(SourceCodeManager.class);
    }

    private SourceCodeManager(Project project) {
        super(project);
        EditorActionManager.getInstance().setReadonlyFragmentModificationHandler(OverrideReadonlyFragmentModificationHandler.INSTANCE);
        fileEditorListener = new DBLanguageFileEditorListener();
    }

    public void updateSourceToDatabase(final FileEditor fileEditor, final DBSourceCodeVirtualFile virtualFile) {
        DatabaseDebuggerManager debuggerManager = DatabaseDebuggerManager.getInstance(virtualFile.getProject());
        final DBSchemaObject object = virtualFile.getObject();
        if (object != null) {
            if (!debuggerManager.checkForbiddenOperation(virtualFile.getActiveConnection())) {
                object.getStatus().set(DBObjectStatus.SAVING, false);
                return;
            }
            new ConnectionAction(object) {
                @Override
                public void execute() {
                    object.getStatus().set(DBObjectStatus.SAVING, true);
                    final Project project = virtualFile.getProject();
                    final DBContentType contentType = virtualFile.getContentType();

                    new BackgroundTask(project, "Checking for third party changes on " + object.getQualifiedNameWithType(), false) {
                        public void execute(@NotNull ProgressIndicator progressIndicator) {
                            try {
                                Editor editor = EditorUtil.getEditor(fileEditor);
                                if (editor != null) {
                                    String content = editor.getDocument().getText();
                                    if (isValidObjectTypeAndName(content, object, contentType)) {
                                        Timestamp lastUpdated = object.loadChangeTimestamp(contentType);
                                        if (lastUpdated != null && lastUpdated.after(virtualFile.getChangeTimestamp())) {

                                            virtualFile.setContent(content);
                                            String message =
                                                    "The " + object.getQualifiedNameWithType() +
                                                            " has been changed by another user. \nYou will be prompted to merge the changes";
                                            MessageUtil.showErrorDialog(project, "Version conflict", message);

                                            String databaseContent = loadSourceCodeFromDatabase(object, contentType);
                                            showSourceDiffDialog(databaseContent, virtualFile, fileEditor);
                                        } else {
                                            doUpdateSourceToDatabase(object, virtualFile, fileEditor);
                                            //sourceCodeEditor.afterSave();
                                        }

                                    } else {
                                        String message = "You are not allowed to change the name or the type of the object";
                                        object.getStatus().set(DBObjectStatus.SAVING, false);
                                        MessageUtil.showErrorDialog(project, "Illegal action", message);
                                    }
                                }
                            } catch (SQLException ex) {
                                if (DatabaseFeature.OBJECT_REPLACING.isSupported(object)) {
                                    virtualFile.updateChangeTimestamp();
                                }
                                MessageUtil.showErrorDialog(project, "Could not save changes to database.", ex);
                                object.getStatus().set(DBObjectStatus.SAVING, false);
                            }
                        }
                    }.start();
                }
            }.start();
        }
    }

    public String loadSourceCodeFromDatabase(DBSchemaObject object, DBContentType contentType) throws SQLException {
        return loadSourceFromDatabase(object, contentType).getSourceCode();
    }

    public SourceCodeContent loadSourceFromDatabase(DBSchemaObject object, DBContentType contentType) throws SQLException {
        ProgressMonitor.setTaskDescription("Loading source code of " + object.getQualifiedNameWithType());
        String sourceCode = object.loadCodeFromDatabase(contentType);
        SourceCodeContent sourceCodeContent = new SourceCodeContent(sourceCode);
        ConnectionHandler connectionHandler = object.getConnectionHandler();
        DatabaseDDLInterface ddlInterface = null;
        if (connectionHandler != null) {
            ddlInterface = connectionHandler.getInterfaceProvider().getDDLInterface();
            ddlInterface.computeSourceCodeOffsets(sourceCodeContent, object.getObjectType().getTypeId(), object.getName());
        }
        return sourceCodeContent;
    }

    private boolean isValidObjectTypeAndName(String text, DBSchemaObject object, DBContentType contentType) {
        ConnectionHandler connectionHandler = object.getConnectionHandler();
        if (connectionHandler != null) {
            DatabaseDDLInterface ddlInterface = connectionHandler.getInterfaceProvider().getDDLInterface();
            if (ddlInterface.includesTypeAndNameInSourceContent(object.getObjectType().getTypeId())) {
                int typeIndex = StringUtil.indexOfIgnoreCase(text, object.getTypeName(), 0);
                if (typeIndex == -1 || !StringUtil.isEmptyOrSpaces(text.substring(0, typeIndex))) {
                    return false;
                }

                int typeEndIndex = typeIndex + object.getTypeName().length();
                if (!Character.isWhitespace(text.charAt(typeEndIndex))) return false;

                if (contentType.getObjectTypeSubname() != null) {
                    int subnameIndex = StringUtil.indexOfIgnoreCase(text, contentType.getObjectTypeSubname(), typeEndIndex);
                    typeEndIndex = subnameIndex + contentType.getObjectTypeSubname().length();
                    if (!Character.isWhitespace(text.charAt(typeEndIndex))) return false;
                }

                char quotes = DatabaseCompatibilityInterface.getInstance(connectionHandler).getIdentifierQuotes();


                String objectName = object.getName();
                int nameIndex = StringUtil.indexOfIgnoreCase(text, objectName, typeEndIndex);
                if (nameIndex == -1) return false;
                int nameEndIndex = nameIndex + objectName.length();

                if (text.charAt(nameIndex -1) == quotes) {
                    if (text.charAt(nameEndIndex) != quotes) return false;
                    nameIndex = nameIndex -1;
                    nameEndIndex = nameEndIndex + 1;
                }

                String typeNameGap = text.substring(typeEndIndex, nameIndex);
                typeNameGap = StringUtil.replaceIgnoreCase(typeNameGap, object.getSchema().getName(), "").replace(".", " ").replace(quotes, ' ');
                if (!StringUtil.isEmptyOrSpaces(typeNameGap)) return false;
                if (!Character.isWhitespace(text.charAt(nameEndIndex)) && text.charAt(nameEndIndex) != '(') return false;
            }
        }
        return true;
    }

    private void showSourceDiffDialog(final String databaseContent, final DBSourceCodeVirtualFile virtualFile, final FileEditor fileEditor) {
        new SimpleLaterInvocator() {
            public void execute() {
                DiffRequestFactory diffRequestFactory = new DiffRequestFactoryImpl();
                MergeRequest mergeRequest = diffRequestFactory.createMergeRequest(
                        databaseContent,
                        virtualFile.getContent(),
                        virtualFile.getLastSavedContent(),
                        virtualFile,
                        virtualFile.getProject(),
                        ActionButtonPresentation.APPLY,
                        ActionButtonPresentation.CANCEL_WITH_PROMPT);
                mergeRequest.setVersionTitles(new String[]{"Database version", "Merge result", "Your version"});
                final DBSchemaObject object = virtualFile.getObject();
                if (object != null) {
                    mergeRequest.setWindowTitle("Version conflict resolution for " + object.getQualifiedNameWithType());

                    DiffManager.getInstance().getDiffTool().show(mergeRequest);

                    int result = mergeRequest.getResult();
                    if (result == 0) {
                        doUpdateSourceToDatabase(object, virtualFile, fileEditor);
                        //sourceCodeEditor.afterSave();
                    } else if (result == 1) {
                        new WriteActionRunner() {
                            public void run() {
                                Editor editor = EditorUtil.getEditor(fileEditor);
                                if (editor != null) {
                                    editor.getDocument().setText(virtualFile.getContent());
                                    object.getStatus().set(DBObjectStatus.SAVING, false);
                                }
                            }
                        }.start();
                    }
                }
            }
        }.start();
    }


    private void doUpdateSourceToDatabase(final DBSchemaObject object, final DBSourceCodeVirtualFile virtualFile, final FileEditor fileEditor) {
        new BackgroundTask(object.getProject(), "Saving " + object.getQualifiedNameWithType() + " to database", false) {
            @Override
            public void execute(@NotNull ProgressIndicator indicator) {
                Project project = getProject();
                try {
                    Editor editor = EditorUtil.getEditor(fileEditor);
                    if (editor != null) {
                        String content = editor.getDocument().getText();
                        virtualFile.setContent(content);
                        virtualFile.updateToDatabase();

                        ConnectionHandler connectionHandler = object.getConnectionHandler();
                        if (connectionHandler != null) {
                            if (DatabaseFeature.OBJECT_INVALIDATION.isSupported(object)) {
                                connectionHandler.getObjectBundle().refreshObjectsStatus(object);
                            }

                            if (object.getProperties().is(DBObjectProperty.COMPILABLE)) {
                                DatabaseCompilerManager compilerManager = DatabaseCompilerManager.getInstance(project);
                                DBContentType contentType = virtualFile.getContentType();
                                CompilerAction compilerAction = new CompilerAction(CompilerActionSource.SAVE, contentType, virtualFile, fileEditor);
                                compilerManager.createCompilerResult(object, compilerAction);
                            }
                            object.reload();
                        }
                    }
                } catch (SQLException e) {
                    MessageUtil.showErrorDialog(project, "Could not save changes to database.", e);
                } finally {
                     object.getStatus().set(DBObjectStatus.SAVING, false);
                }

            }
        }.start();
    }

    public BasePsiElement getObjectNavigationElement(DBSchemaObject parentObject, DBContentType contentType, DBObjectType objectType, CharSequence objectName) {
        DBEditableObjectVirtualFile databaseFile = parentObject.getVirtualFile();
        DBContentVirtualFile contentFile = databaseFile.getContentFile(contentType);
        if (contentFile != null) {
            PSQLFile file = (PSQLFile) PsiUtil.getPsiFile(getProject(), contentFile);
            if (file != null) {
                return
                    contentType == DBContentType.CODE_BODY ? file.lookupObjectDeclaration(objectType, objectName) :
                    contentType == DBContentType.CODE_SPEC ? file.lookupObjectSpecification(objectType, objectName) : null;
            }
        }
        return null;
    }

    public void navigateToObject(DBSchemaObject parentObject, BasePsiElement basePsiElement) {
        DBEditableObjectVirtualFile databaseFile = parentObject.getVirtualFile();
        VirtualFile virtualFile = basePsiElement.getFile().getVirtualFile();
        if (virtualFile instanceof DBSourceCodeVirtualFile) {
            BasicTextEditor textEditor = EditorUtil.getTextEditor(databaseFile, (DBSourceCodeVirtualFile) virtualFile);
            if (textEditor != null) {
                Project project = getProject();
                EditorProviderId editorProviderId = textEditor.getEditorProviderId();
                FileEditor fileEditor = EditorUtil.selectEditor(project, textEditor, databaseFile, editorProviderId, true);
                basePsiElement.navigateInEditor(fileEditor, true);
            }
        }
    }

    @Override
    public void projectOpened() {
        EventManager.subscribe(getProject(), FileEditorManagerListener.FILE_EDITOR_MANAGER, fileEditorListener);
    }

    @Override
    public void projectClosed() {
        EventManager.unsubscribe(fileEditorListener);
    }

    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.Project.SourceCodeManager";
    }

    /*********************************************
     *            PersistentStateComponent       *
     *********************************************/
    @Nullable
    @Override
    public Element getState() {
        return null;
    }

    @Override
    public void loadState(Element element) {
    }
}
