package com.dci.intellij.dbn.generator;

import java.util.List;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.object.DBTable;
import com.dci.intellij.dbn.object.common.DBObject;
import com.intellij.openapi.project.Project;

public class StatementGenerationManager extends AbstractProjectComponent {

    private StatementGenerationManager(Project project) {
        super(project);
    }

    public static StatementGenerationManager getInstance(Project project) {
        return project.getComponent(StatementGenerationManager.class);
    }

    public StatementGeneratorResult generateSelectStatement(List<DBObject> objects, boolean enforceAliasUsage) {
        SelectStatementGenerator generator = new SelectStatementGenerator(objects, enforceAliasUsage);
        return generator.generateStatement(getProject());
    }

    public StatementGeneratorResult generateInsert(DBTable table) {
        InsertStatementGenerator generator = new InsertStatementGenerator(table);
        return generator.generateStatement(getProject());
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "DBNavigator.Project.StatementGenerationManager";
    }
}
