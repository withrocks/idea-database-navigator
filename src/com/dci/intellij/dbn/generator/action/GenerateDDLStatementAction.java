package com.dci.intellij.dbn.generator.action;

import java.sql.SQLException;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.generator.StatementGeneratorResult;
import com.dci.intellij.dbn.object.common.DBObject;
import com.intellij.openapi.project.Project;

public class GenerateDDLStatementAction extends GenerateStatementAction {
    private DBObject object;
    public GenerateDDLStatementAction(DBObject object) {
        super("DDL Statement");
        this.object = object;

    }

    @Nullable
    @Override
    public ConnectionHandler getConnectionHandler() {
        return object.getConnectionHandler();
    }

    @Override
    protected StatementGeneratorResult generateStatement(Project project) {
        StatementGeneratorResult result = new StatementGeneratorResult();
        try {
            String statement = object.extractDDL();
            if (StringUtil.isEmptyOrSpaces(statement)) {
                String message =
                        "Could not extract DDL statement for " + object.getQualifiedNameWithType() + ".\n" +
                        "You may not have enough rights to perform this action. Please contact your database administrator for more details.";
                result.getMessages().addErrorMessage(message);
            }

            result.setStatement(statement);
        } catch (SQLException e) {
            result.getMessages().addErrorMessage(
                    "Could not extract DDL statement for " + object.getQualifiedNameWithType() + ".\n" +
                    "Cause: " + e.getMessage());
        }
       return result;
    }
}
