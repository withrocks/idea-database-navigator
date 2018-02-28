package com.dci.intellij.dbn.language.sql;

import com.dci.intellij.dbn.language.common.DBLanguageParser;
import com.dci.intellij.dbn.language.common.DBLanguageParserDefinition;
import com.dci.intellij.dbn.language.common.TokenTypeBundle;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;


public class SQLParserDefinition extends DBLanguageParserDefinition {

    public SQLParserDefinition() {
        this((SQLParser) getDefaultParseDefinition().getParser());
    }

    public SQLParserDefinition(SQLParser parser) {
        super(parser);
    }

    @NotNull
    public Lexer createLexer(Project project) {
        return getDefaultParseDefinition().createLexer(project);
    }

    private static DBLanguageParserDefinition getDefaultParseDefinition() {
        return SQLLanguage.INSTANCE.getMainLanguageDialect().getParserDefinition();
    }

    @NotNull
    public DBLanguageParser createParser(Project project) {
        return getParser();
    }

    public TokenTypeBundle getTokenTypes() {
        return getParser().getTokenTypes();
    }

    public PsiFile createFile(FileViewProvider viewProvider) {
        return new SQLFile(viewProvider, SQLLanguage.INSTANCE);
    }
}
