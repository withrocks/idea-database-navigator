package com.dci.intellij.dbn.language.psql.dialect.postgres;

import com.dci.intellij.dbn.language.psql.PSQLParserDefinition;
import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;


public class PostgresPSQLParserDefinition extends PSQLParserDefinition {

    public PostgresPSQLParserDefinition(PostgresPSQLParser parser) {
        super(parser);
    }

    @NotNull
    public Lexer createLexer(Project project) {
        return new FlexAdapter(new PostgresPSQLParserFlexLexer(getTokenTypes()));
    }


}
