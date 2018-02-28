package com.dci.intellij.dbn.language.sql;

import com.dci.intellij.dbn.code.sql.style.options.SQLCodeStyleSettings;
import com.dci.intellij.dbn.code.sql.style.options.SQLCustomCodeStyleSettings;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.sql.dialect.SQLLanguageDialect;
import com.dci.intellij.dbn.language.sql.dialect.iso92.Iso92SQLLanguageDialect;
import com.dci.intellij.dbn.language.sql.dialect.mysql.MysqlSQLLanguageDialect;
import com.dci.intellij.dbn.language.sql.dialect.oracle.OracleSQLLanguageDialect;
import com.dci.intellij.dbn.language.sql.dialect.postgres.PostgresSQLLanguageDialect;
import com.intellij.openapi.project.Project;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.tree.IFileElementType;

public class SQLLanguage extends DBLanguage<SQLLanguageDialect> {
    public static final SQLLanguage INSTANCE = new SQLLanguage();

    protected SQLLanguageDialect[] createLanguageDialects() {
        SQLLanguageDialect oracleSQLLanguageDialect = new OracleSQLLanguageDialect();
        SQLLanguageDialect mysqlSQLLanguageDialect = new MysqlSQLLanguageDialect();
        SQLLanguageDialect postgresSQLLanguageDialect = new PostgresSQLLanguageDialect();
        SQLLanguageDialect iso92SQLLanguageDialect = new Iso92SQLLanguageDialect();
        return new SQLLanguageDialect[]{
                oracleSQLLanguageDialect,
                mysqlSQLLanguageDialect,
                postgresSQLLanguageDialect,
                iso92SQLLanguageDialect};
    }

    public SQLLanguageDialect getMainLanguageDialect() {
        return getAvailableLanguageDialects()[0];
    }

    @Override
    protected IFileElementType createFileElementType(DBLanguage<SQLLanguageDialect> language) {
        return new SQLFileElementType(this);
    }

    private SQLLanguage() {
        super("DBN-SQL", "text/sql");
    }


    public SQLCodeStyleSettings getCodeStyleSettings(Project project) {
        CodeStyleSettings codeStyleSettings = CodeStyleSettingsManager.getSettings(project);
        SQLCustomCodeStyleSettings customCodeStyleSettings = codeStyleSettings.getCustomSettings(SQLCustomCodeStyleSettings.class);
        return customCodeStyleSettings.getCodeStyleSettings();
    }
}
