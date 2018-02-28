package com.dci.intellij.dbn.execution.explain;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.RunnableTask;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionUtil;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.execution.ExecutionManager;
import com.dci.intellij.dbn.execution.explain.result.ExplainPlanResult;
import com.dci.intellij.dbn.language.common.psi.ExecutablePsiElement;
import com.dci.intellij.dbn.object.DBSchema;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;

public class ExplainPlanManager extends AbstractProjectComponent {
    private ExplainPlanManager(Project project) {
        super(project);
        //EventManager.subscribe(project, PsiDocumentTransactionListener.TOPIC, psiDocumentTransactionListener);
    }

    public static ExplainPlanManager getInstance(Project project) {
        return project.getComponent(ExplainPlanManager.class);
    }

    @Override
    public void disposeComponent() {
        //EventManager.unsubscribe(psiDocumentTransactionListener);
        super.disposeComponent();
    }

    /*********************************************************
     *                       Execution                       *
     *********************************************************/

    public void explainPlan(final ExecutablePsiElement executable, final @Nullable RunnableTask<ExplainPlanResult> callback) {
        BackgroundTask explainTask = new BackgroundTask(getProject(), "Extracting explain plan for " + executable.getElementType().getDescription(), false, true) {
            public void execute(@NotNull ProgressIndicator progressIndicator) {
                ConnectionHandler connectionHandler = executable.getFile().getActiveConnection();
                DBSchema currentSchema = executable.getFile().getCurrentSchema();
                if (connectionHandler != null) {
                    ExplainPlanResult explainPlanResult = null;
                    Connection connection = null;
                    Statement statement = null;
                    ResultSet resultSet = null;
                    try {
                        DatabaseInterfaceProvider interfaceProvider = connectionHandler.getInterfaceProvider();
                        DatabaseMetadataInterface metadataInterface = interfaceProvider.getMetadataInterface();
                        connection = connectionHandler.getPoolConnection(currentSchema);
                        metadataInterface.clearExplainPlanData(connection);

                        DatabaseCompatibilityInterface compatibilityInterface = interfaceProvider.getCompatibilityInterface();
                        String explainPlanStatementPrefix = compatibilityInterface.getExplainPlanStatementPrefix();
                        String explainPlanQuery = explainPlanStatementPrefix + "\n" + executable.prepareStatementText();
                        statement = connection.createStatement();
                        statement.execute(explainPlanQuery);

                        resultSet = metadataInterface.loadExplainPlan(connection);
                        explainPlanResult = new ExplainPlanResult(executable, resultSet);

                    } catch (SQLException e) {
                        explainPlanResult = new ExplainPlanResult(executable, e.getMessage());
                    } finally {
                        ConnectionUtil.rollback(connection);
                        ConnectionUtil.closeResultSet(resultSet);
                        ConnectionUtil.closeStatement(statement);
                        connectionHandler.freePoolConnection(connection);
                    }

                    if (callback == null) {
                        ExecutionManager executionManager = ExecutionManager.getInstance(getProject());
                        executionManager.addExplainPlanResult(explainPlanResult);
                    } else {
                        callback.setResult(explainPlanResult);
                        callback.start();
                    }
                }
            }
        };

        FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(getProject());
        connectionMappingManager.selectConnectionAndSchema(executable.getFile(), explainTask);
    }

    /*********************************************************
     *                    ProjectComponent                   *
     *********************************************************/
    @NotNull
    @NonNls
    public String getComponentName() {
        return "DBNavigator.Project.ExplainPlanManager";
    }

}
