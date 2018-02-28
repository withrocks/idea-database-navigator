package com.dci.intellij.dbn.generator.action;

import java.util.List;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.generator.StatementGenerationManager;
import com.dci.intellij.dbn.generator.StatementGeneratorResult;
import com.dci.intellij.dbn.object.common.DBObject;
import com.intellij.openapi.project.Project;

public class GenerateSelectStatementAction extends GenerateStatementAction {
    private List<DBObject> selectedObjects;

    public GenerateSelectStatementAction(List<DBObject> selectedObjects) {
        super("SELECT Statement");
        this.selectedObjects = selectedObjects;
    }

    @Override
    protected StatementGeneratorResult generateStatement(Project project) {
        StatementGenerationManager statementGenerationManager = StatementGenerationManager.getInstance(project);
        return statementGenerationManager.generateSelectStatement(selectedObjects, true);
    }

    @Nullable
    @Override
    public ConnectionHandler getConnectionHandler() {
        if (selectedObjects.size() > 0) {
            return selectedObjects.get(0).getConnectionHandler();
        }
        return null;
    }
}
