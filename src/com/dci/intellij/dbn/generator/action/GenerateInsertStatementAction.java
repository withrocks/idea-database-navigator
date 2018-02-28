package com.dci.intellij.dbn.generator.action;

import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.generator.StatementGenerationManager;
import com.dci.intellij.dbn.generator.StatementGeneratorResult;
import com.dci.intellij.dbn.object.DBTable;
import com.intellij.openapi.project.Project;

public class GenerateInsertStatementAction extends GenerateStatementAction {
    private DBTable table;

    public GenerateInsertStatementAction(DBTable table) {
        super("INSERT Statement");
        this.table = table;
    }

    @Override
    protected StatementGeneratorResult generateStatement(Project project) {
        StatementGenerationManager statementGenerationManager = StatementGenerationManager.getInstance(project);
        return statementGenerationManager.generateInsert(table);
    }

    @Nullable
    @Override
    public ConnectionHandler getConnectionHandler() {
        return table.getConnectionHandler();
    }
}
