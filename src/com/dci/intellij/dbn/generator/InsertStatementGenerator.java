package com.dci.intellij.dbn.generator;

import java.util.Iterator;

import com.dci.intellij.dbn.code.common.style.DBLCodeStyleManager;
import com.dci.intellij.dbn.code.common.style.options.CodeStyleCaseOption;
import com.dci.intellij.dbn.code.common.style.options.CodeStyleCaseSettings;
import com.dci.intellij.dbn.language.sql.SQLLanguage;
import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBTable;
import com.intellij.openapi.project.Project;

public class InsertStatementGenerator extends StatementGenerator {
    private DBTable table;

    public InsertStatementGenerator(DBTable table) {
        this.table = table;
    }

    @Override
    public StatementGeneratorResult generateStatement(Project project) {
        CodeStyleCaseSettings styleCaseSettings = DBLCodeStyleManager.getInstance(project).getCodeStyleCaseSettings(SQLLanguage.INSTANCE);
        CodeStyleCaseOption kco = styleCaseSettings.getKeywordCaseOption();
        CodeStyleCaseOption oco = styleCaseSettings.getObjectCaseOption();

        StatementGeneratorResult result = new StatementGeneratorResult();

        StringBuilder statement = new StringBuilder();

        statement.append(kco.format("insert into "));
        statement.append(oco.format(table.getName()));
        statement.append(" (\n");

        Iterator<DBColumn> columnIterator = table.getColumns().iterator();
        while (columnIterator.hasNext()) {
            DBColumn column = columnIterator.next();
            statement.append("    ");
            statement.append(oco.format(column.getName()));
            if (columnIterator.hasNext()) {
                statement.append(",\n");
            } else {
                statement.append(")\n");
            }
        }
        statement.append(kco.format("values (\n"));

        columnIterator = table.getColumns().iterator();
        while (columnIterator.hasNext()) {
            DBColumn column = columnIterator.next();
            statement.append("    :");
            statement.append(column.getName().toLowerCase());
            if (columnIterator.hasNext()) {
                statement.append(",\n");
            } else {
                statement.append(")\n");
            }
        }
        statement.append(";");

        result.setStatement(statement.toString());
        return result;
    }
}
