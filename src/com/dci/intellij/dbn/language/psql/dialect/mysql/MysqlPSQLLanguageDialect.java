package com.dci.intellij.dbn.language.psql.dialect.mysql;

import com.dci.intellij.dbn.language.common.DBLanguageDialectIdentifier;
import com.dci.intellij.dbn.language.common.DBLanguageSyntaxHighlighter;
import com.dci.intellij.dbn.language.psql.dialect.PSQLLanguageDialect;
import com.dci.intellij.dbn.language.psql.dialect.oracle.OraclePLSQLSyntaxHighlighter;

public class MysqlPSQLLanguageDialect extends PSQLLanguageDialect {
    public MysqlPSQLLanguageDialect() {
        super(DBLanguageDialectIdentifier.MYSQL_PSQL);
    }

    protected DBLanguageSyntaxHighlighter createSyntaxHighlighter() {
        return new OraclePLSQLSyntaxHighlighter(this);
}

    protected MysqlPSQLParserDefinition createParserDefinition() {
        MysqlPSQLParser parser = new MysqlPSQLParser(this);
        return new MysqlPSQLParserDefinition(parser);
    }
}
