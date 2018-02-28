package com.dci.intellij.dbn.language.psql.dialect.postgres;

import com.dci.intellij.dbn.language.psql.PSQLParser;
import com.dci.intellij.dbn.language.psql.dialect.PSQLLanguageDialect;

public class PostgresPSQLParser extends PSQLParser {
    public PostgresPSQLParser(PSQLLanguageDialect languageDialect) {
        super(languageDialect, "postgres_psql_parser_tokens.xml", "postgres_psql_parser_elements.xml", "psql_block");
    }
}