package com.dci.intellij.dbn.language.sql.dialect.iso92;

import com.dci.intellij.dbn.language.common.ChameleonTokenType;
import com.dci.intellij.dbn.language.common.DBLanguageDialectIdentifier;
import com.dci.intellij.dbn.language.common.DBLanguageSyntaxHighlighter;
import com.dci.intellij.dbn.language.sql.dialect.SQLLanguageDialect;

import java.util.Set;

public class Iso92SQLLanguageDialect extends SQLLanguageDialect {

    public Iso92SQLLanguageDialect() {
        super(DBLanguageDialectIdentifier.ISO92_SQL);
    }

    @Override
    protected Set<ChameleonTokenType> createChameleonTokenTypes() {
        return null;
    }

    protected DBLanguageSyntaxHighlighter createSyntaxHighlighter() {
        return new Iso92SQLSyntaxHighlighter(this);
}
    protected Iso92SQLParserDefinition createParserDefinition() {
        Iso92SQLParser parser = new Iso92SQLParser(this);
        return new Iso92SQLParserDefinition(parser);
    }

}
