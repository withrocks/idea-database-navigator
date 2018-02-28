package com.dci.intellij.dbn.execution.statement.processor;

import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.editor.BasicTextEditor;
import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.common.thread.ReadActionRunner;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.EditorProviderId;
import com.dci.intellij.dbn.execution.ExecutionManager;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.execution.compiler.CompilerAction;
import com.dci.intellij.dbn.execution.compiler.CompilerActionSource;
import com.dci.intellij.dbn.execution.compiler.CompilerResult;
import com.dci.intellij.dbn.execution.logging.DatabaseLoggingManager;
import com.dci.intellij.dbn.execution.statement.DataDefinitionChangeListener;
import com.dci.intellij.dbn.execution.statement.StatementExecutionInput;
import com.dci.intellij.dbn.execution.statement.options.StatementExecutionSettings;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionBasicResult;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionResult;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionStatus;
import com.dci.intellij.dbn.execution.statement.variables.StatementExecutionVariablesBundle;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.ChameleonPsiElement;
import com.dci.intellij.dbn.language.common.psi.ExecutablePsiElement;
import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;
import com.dci.intellij.dbn.language.common.psi.QualifiedIdentifierPsiElement;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;

public class StatementExecutionBasicProcessor implements StatementExecutionProcessor {

    protected WeakReference<FileEditor> fileEditorRef;
    protected WeakReference<DBLanguagePsiFile> psiFileRef;
    protected ExecutablePsiElement cachedExecutable;
    private EditorProviderId editorProviderId;

    protected String resultName;
    protected int index;

    private StatementExecutionInput executionInput;
    private StatementExecutionResult executionResult;

    public StatementExecutionBasicProcessor(FileEditor fileEditor, ExecutablePsiElement psiElement, int index) {
        this.fileEditorRef = new WeakReference<FileEditor>(fileEditor);
        this.psiFileRef = new WeakReference<DBLanguagePsiFile>(psiElement.getFile());

        this.cachedExecutable = psiElement;
        this.index = index;
        executionInput = new StatementExecutionInput(psiElement.getText(), psiElement.prepareStatementText(), this);
        initEditorProviderId(fileEditor);
    }

    public StatementExecutionBasicProcessor(FileEditor fileEditor, DBLanguagePsiFile psiFile, String sqlStatement, int index) {
        this.fileEditorRef = new WeakReference<FileEditor>(fileEditor);
        this.psiFileRef = new WeakReference<DBLanguagePsiFile>(psiFile);
        this.index = index;
        sqlStatement = sqlStatement.trim();
        executionInput = new StatementExecutionInput(sqlStatement, sqlStatement, this);

        initEditorProviderId(fileEditor);
    }

    private void initEditorProviderId(FileEditor fileEditor) {
        if (fileEditor instanceof BasicTextEditor) {
            BasicTextEditor basicTextEditor = (BasicTextEditor) fileEditor;
            editorProviderId = basicTextEditor.getEditorProviderId();
        }
    }

    public boolean isDirty(){
        if (getConnectionHandler() != executionInput.getConnectionHandler() || // connection changed since execution
            getCurrentSchema() != executionInput.getCurrentSchema()) { // current schema changed since execution)
            return true;

        } else {
            ExecutablePsiElement executablePsiElement = executionInput.getExecutablePsiElement();
            return
                this.cachedExecutable == null ||
                executablePsiElement == null ||
                !this.cachedExecutable.matches(executablePsiElement, BasePsiElement.MatchType.STRONG);
        }
    }

    @Override
    public void bind(ExecutablePsiElement executablePsiElement) {
        this.cachedExecutable = executablePsiElement;
        executablePsiElement.setExecutionProcessor(this);
    }

    @Override
    public void unbind() {
        cachedExecutable = null;
    }

    @Override
    public boolean isBound() {
        return cachedExecutable != null;
    }

    public DBLanguagePsiFile getPsiFile() {
        return psiFileRef.get();
    }

    @Override
    public VirtualFile getVirtualFile() {
        DBLanguagePsiFile psiFile = getPsiFile();
        return psiFile == null ? null : psiFile.getVirtualFile();
    }

    @Override
    public FileEditor getFileEditor() {
        FileEditor fileEditor = this.fileEditorRef == null ? null : this.fileEditorRef.get();
        if (fileEditor != null) {
            Editor editor = EditorUtil.getEditor(fileEditor);
            if (editor != null && editor.isDisposed()) {
                this.fileEditorRef = null;
            }
        }
        return fileEditor;
    }

    @Override
    @Nullable
    public EditorProviderId getEditorProviderId() {
        return editorProviderId;
    }

    @Override
    @Nullable
    public ExecutablePsiElement getCachedExecutable() {
        return cachedExecutable;
    }

    public static boolean contains(PsiElement parent, BasePsiElement childElement, BasePsiElement.MatchType matchType) {
        PsiElement child = parent.getFirstChild();
        while (child != null) {
            if (child == childElement) {
                return true;
            }
            if (child instanceof ChameleonPsiElement) {
                if (contains(child, childElement, matchType)) {
                    return true;
                }
            } else if(child instanceof BasePsiElement) {
                BasePsiElement basePsiElement = (BasePsiElement) child;
                if (basePsiElement.matches(childElement, matchType)) {
                    return true;
                }
            }
            child = child.getNextSibling();
        }
        return false;
    }

    @Override
    public String toString() {
        return executionInput.getOriginalStatementText();
    }

    @Override
    public StatementExecutionInput getExecutionInput() {
        return executionInput;
    }

    public StatementExecutionResult getExecutionResult() {
        if (executionResult != null && executionResult.isDisposed()) {
            executionResult = null;
        }
        return executionResult;
    }

    @Override
    public void initExecutionInput(boolean bulkExecution) {
        // overwrite the input if it was leniently bound
        if (cachedExecutable != null) {
            executionInput.setOriginalStatementText(cachedExecutable.getText());
            executionInput.setExecutableStatementText(cachedExecutable.prepareStatementText());
            executionInput.setConnectionHandler(getConnectionHandler());
            executionInput.setCurrentSchema(getCurrentSchema());
            executionInput.setBulkExecution(bulkExecution);
        }

    }

    public void execute(ProgressIndicator progressIndicator) {
        progressIndicator.setText("Executing " + getStatementName());
        long startTimeMillis = System.currentTimeMillis();
        resultName = null;
        ConnectionHandler activeConnection = getConnectionHandler();
        DBSchema currentSchema = getCurrentSchema();

        boolean continueExecution = true;

        String executableStatementText = executionInput.getExecutableStatementText();
        StatementExecutionVariablesBundle executionVariables = executionInput.getExecutionVariables();
        if (executionVariables != null) {
            executableStatementText = executionVariables.prepareStatementText(activeConnection, executableStatementText, false);
            executionInput.setExecutableStatementText(executableStatementText);

            if (executionVariables.hasErrors()) {
                executionResult = createErrorExecutionResult("Could not bind all variables.");
                continueExecution = false;
            }
        }

        Project project = getProject();
        boolean loggingEnabled = false;
        if (continueExecution) {
            DatabaseLoggingManager loggingManager = DatabaseLoggingManager.getInstance(project);
            Connection connection = null;
            try {
                if (activeConnection != null && !activeConnection.isDisposed()) {
                    connection = activeConnection.getStandaloneConnection(currentSchema);

                    if (activeConnection.isLoggingEnabled() && executionInput.isDatabaseLogProducer()) {
                        loggingEnabled = loggingManager.enableLogger(activeConnection, connection);
                    }
                    Statement statement = connection.createStatement();

                    statement.setQueryTimeout(getStatementExecutionSettings().getExecutionTimeout());
                    statement.execute(executableStatementText);
                    executionResult = createExecutionResult(statement, executionInput);
                    ExecutablePsiElement executablePsiElement = executionInput.getExecutablePsiElement();
                    VirtualFile virtualFile = getPsiFile().getVirtualFile();
                    if (executablePsiElement != null) {
                        if (executablePsiElement.isTransactional()) activeConnection.notifyChanges(virtualFile);
                        if (executablePsiElement.isTransactionControl()) activeConnection.resetChanges();
                    } else{
                        if (executionResult.getUpdateCount() > 0) activeConnection.notifyChanges(virtualFile);
                    }

                    executionResult.setLoggingActive(loggingEnabled);
                    if (loggingEnabled) {
                        String logOutput = loggingManager.readLoggerOutput(activeConnection, connection);
                        executionResult.setLoggingOutput(logOutput);
                    }


                    if (isDataDefinitionStatement()) {
                        DBSchemaObject affectedObject = getAffectedObject();
                        if (affectedObject != null) {
                            DataDefinitionChangeListener listener = EventManager.notify(project, DataDefinitionChangeListener.TOPIC);
                            listener.dataDefinitionChanged(affectedObject);
                        } else {
                            DBSchema affectedSchema = getAffectedSchema();
                            IdentifierPsiElement subjectPsiElement = getSubjectPsiElement();
                            if (affectedSchema != null && subjectPsiElement != null) {
                                DataDefinitionChangeListener listener = EventManager.notify(project, DataDefinitionChangeListener.TOPIC);
                                listener.dataDefinitionChanged(affectedSchema, subjectPsiElement.getObjectType());
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                executionResult = createErrorExecutionResult(e.getMessage());
            } finally {
                if (loggingEnabled) {
                    loggingManager.disableLogger(activeConnection, connection);
                }
            }
        }

        executionResult.setExecutionDuration((int) (System.currentTimeMillis() - startTimeMillis));
        ExecutionManager executionManager = ExecutionManager.getInstance(project);
        executionManager.addExecutionResult(executionResult);
    }

    public StatementExecutionVariablesBundle getExecutionVariables() {
        return executionInput.getExecutionVariables();
    }

    protected StatementExecutionResult createExecutionResult(Statement statement, final StatementExecutionInput executionInput) throws SQLException {
        final StatementExecutionBasicResult executionResult = new StatementExecutionBasicResult(this, getResultName(), statement.getUpdateCount());
        boolean isDdlStatement = isDataDefinitionStatement();
        boolean hasCompilerErrors = false;
        if (isDdlStatement) {
            final BasePsiElement compilablePsiElement = getCompilableBlockPsiElement();
            if (compilablePsiElement != null) {
                hasCompilerErrors = new ReadActionRunner<Boolean>() {
                    @Override
                    protected Boolean run() {
                        DBContentType contentType = getCompilableContentType();
                        CompilerAction compilerAction = new CompilerAction(CompilerActionSource.DDL, contentType, getPsiFile().getVirtualFile(), getFileEditor());
                        compilerAction.setStartOffset(compilablePsiElement.getTextOffset());
                        CompilerResult compilerResult = null;

                        DBSchemaObject underlyingObject = getAffectedObject();
                        if (underlyingObject == null) {
                            ConnectionHandler connectionHandler = executionInput.getConnectionHandler();
                            DBSchema schema = getAffectedSchema();
                            IdentifierPsiElement subjectPsiElement = getSubjectPsiElement();
                            if (connectionHandler != null && schema != null && subjectPsiElement != null) {
                                DBObjectType objectType = subjectPsiElement.getObjectType();
                                String objectName = subjectPsiElement.getUnquotedText().toString().toUpperCase();
                                compilerResult = new CompilerResult(compilerAction, connectionHandler, schema, objectType, objectName);
                            }
                        } else {
                            compilerResult = new CompilerResult(compilerAction, underlyingObject);
                        }

                        if (compilerResult != null) {
                            executionResult.setCompilerResult(compilerResult);
                            return compilerResult.hasErrors();
                        }
                        return false;
                    }
                }.start();
            }
        }

        if (hasCompilerErrors) {
            String message = executionInput.getStatementDescription() + " executed with warnings";
            executionResult.updateExecutionMessage(MessageType.WARNING, message);
            executionResult.setExecutionStatus(StatementExecutionStatus.WARNING);

        } else {
            String message = executionInput.getStatementDescription() + " executed successfully";
            int updateCount = executionResult.getUpdateCount();
            if (!isDdlStatement && updateCount > -1) {
                message = message + ": " + updateCount + (updateCount != 1 ? " rows" : " row") + " affected";
            }
            executionResult.updateExecutionMessage(MessageType.INFO, message);
            executionResult.setExecutionStatus(StatementExecutionStatus.SUCCESS);
        }

        return executionResult;
    }



    public StatementExecutionResult createErrorExecutionResult(String cause) {
        StatementExecutionResult executionResult = new StatementExecutionBasicResult(this, getResultName(), 0);
        executionResult.updateExecutionMessage(MessageType.ERROR, "Could not execute " + getStatementName() + '.', cause);
        executionResult.setExecutionStatus(StatementExecutionStatus.ERROR);
        return executionResult;
    }

    public StatementExecutionSettings getStatementExecutionSettings() {
        return ExecutionEngineSettings.getInstance(getProject()).getStatementExecutionSettings();
    }

    @Nullable
    public ConnectionHandler getConnectionHandler() {
        DBLanguagePsiFile psiFile = getPsiFile();
        return psiFile == null ? null : psiFile.getActiveConnection();
    }

    public DBSchema getCurrentSchema() {
        DBLanguagePsiFile psiFile = getPsiFile();
        return psiFile == null ? null : psiFile.getCurrentSchema();
    }

    public Project getProject() {
        DBLanguagePsiFile psiFile = getPsiFile();
        return psiFile == null ? null : psiFile.getProject();
    }

    @NotNull
    public synchronized String getResultName() {
        if (resultName == null) {
            ExecutablePsiElement executablePsiElement = executionInput.getExecutablePsiElement();
            if (executablePsiElement!= null) {
                 resultName = executablePsiElement.createSubjectList();
            }
            if (StringUtil.isEmptyOrSpaces(resultName)) {
                resultName = "Result " + index;
            }
        }
        return resultName;
    }

    public String getStatementName() {
        ExecutablePsiElement executablePsiElement = executionInput.getExecutablePsiElement();
        return executablePsiElement == null ? "SQL statement" : executablePsiElement.getElementType().getDescription();
    }

    public int getIndex() {
        return index;
    }

    public boolean canExecute() {
        return !disposed;
    }

    public void navigateToResult() {
        StatementExecutionResult executionResult = getExecutionResult();
        if (executionResult != null) {
            ExecutionManager executionManager = ExecutionManager.getInstance(getProject());
            executionManager.selectExecutionResult(executionResult);
        }
    }

    public void navigateToEditor(boolean requestFocus) {
        FileEditor fileEditor = getFileEditor();
        if (cachedExecutable != null) {
            if (fileEditor != null) {
                cachedExecutable.navigateInEditor(fileEditor, requestFocus);
            } else {
                cachedExecutable.navigate(requestFocus);
            }
        }
    }

    /********************************************************
     *                    Disposable                        *
     ********************************************************/
    public boolean isDataDefinitionStatement() {
        return cachedExecutable != null && cachedExecutable.is(ElementTypeAttribute.DATA_DEFINITION);
    }

    @Nullable
    public DBSchemaObject getAffectedObject() {
        if (isDataDefinitionStatement()) {
            IdentifierPsiElement subjectPsiElement = getSubjectPsiElement();
            if (subjectPsiElement != null) {
                DBSchema currentSchema = getCurrentSchema();
                if (currentSchema != null) {
                    DBObjectListContainer childObjects = currentSchema.getChildObjects();
                    if (childObjects != null) {
                        DBObjectList objectList = childObjects.getObjectList(subjectPsiElement.getObjectType());
                        if (objectList != null) {
                            return (DBSchemaObject) objectList.getObject(subjectPsiElement.getText());
                        }
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    public DBSchema getAffectedSchema() {
        if (isDataDefinitionStatement()) {
            IdentifierPsiElement subjectPsiElement = getSubjectPsiElement();
            if (subjectPsiElement != null) {
                PsiElement parent = subjectPsiElement.getParent();
                if (parent instanceof QualifiedIdentifierPsiElement) {
                    QualifiedIdentifierPsiElement qualifiedIdentifierPsiElement = (QualifiedIdentifierPsiElement) parent;
                    DBObject parentObject = qualifiedIdentifierPsiElement.lookupParentObjectFor(subjectPsiElement.getElementType());
                    if (parentObject instanceof DBSchema) {
                        return (DBSchema) parentObject;
                    }
                }
            }
        }
        return getCurrentSchema();
    }

    @Nullable
    public IdentifierPsiElement getSubjectPsiElement() {
        return cachedExecutable == null ? null : (IdentifierPsiElement) cachedExecutable.findFirstPsiElement(ElementTypeAttribute.SUBJECT);
    }

    public BasePsiElement getCompilableBlockPsiElement() {
        return cachedExecutable == null ? null : cachedExecutable.findFirstPsiElement(ElementTypeAttribute.COMPILABLE_BLOCK);
    }

    public DBContentType getCompilableContentType() {
        BasePsiElement compilableBlockPsiElement = getCompilableBlockPsiElement();
        if (compilableBlockPsiElement != null) {
            //if (compilableBlockPsiElement.is(ElementTypeAttribute.OBJECT_DEFINITION)) return DBContentType.CODE;
            if (compilableBlockPsiElement.is(ElementTypeAttribute.OBJECT_SPECIFICATION)) return DBContentType.CODE_SPEC;
            if (compilableBlockPsiElement.is(ElementTypeAttribute.OBJECT_DECLARATION)) return DBContentType.CODE_BODY;
        }
        return DBContentType.CODE;
    }

    public boolean isQuery() {
        return false;
    }

    /********************************************************
     *                    Disposable                        *
     ********************************************************/
    private boolean disposed;

    @Override
    public void dispose() {
        if (!disposed) {
            disposed = true;
            cachedExecutable = null;
            psiFileRef = null;

        }
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }
}
