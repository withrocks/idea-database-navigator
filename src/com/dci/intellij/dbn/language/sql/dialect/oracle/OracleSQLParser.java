package com.dci.intellij.dbn.language.sql.dialect.oracle;

import com.dci.intellij.dbn.language.sql.SQLParser;
import com.dci.intellij.dbn.language.sql.dialect.SQLLanguageDialect;

public class OracleSQLParser extends SQLParser {
    public OracleSQLParser(SQLLanguageDialect languageDialect) {
        super(languageDialect, "oracle_sql_parser_tokens.xml", "oracle_sql_parser_elements.xml", "sql_block");
    }
}
