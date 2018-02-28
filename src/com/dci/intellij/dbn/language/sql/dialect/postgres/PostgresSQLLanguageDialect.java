package com.dci.intellij.dbn.language.sql.dialect.postgres;

import java.util.Set;

import com.dci.intellij.dbn.language.common.ChameleonTokenType;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.DBLanguageDialectIdentifier;
import com.dci.intellij.dbn.language.common.DBLanguageSyntaxHighlighter;
import com.dci.intellij.dbn.language.common.element.ChameleonElementType;
import com.dci.intellij.dbn.language.sql.dialect.SQLLanguageDialect;

public class PostgresSQLLanguageDialect extends SQLLanguageDialect {
    ChameleonElementType pgsqlChameleonElementType;
    public PostgresSQLLanguageDialect() {
        super(DBLanguageDialectIdentifier.POSTGRES_SQL);
    }

    @Override
    protected Set<ChameleonTokenType> createChameleonTokenTypes() {
        return null;
    }

    protected DBLanguageSyntaxHighlighter createSyntaxHighlighter() {
        return new PostgresSQLSyntaxHighlighter(this);
}
    protected PostgresSQLParserDefinition createParserDefinition() {
        PostgresSQLParser parser = new PostgresSQLParser(this);
        return new PostgresSQLParserDefinition(parser);
    }

    @Override
    public ChameleonElementType getChameleonTokenType(DBLanguageDialectIdentifier dialectIdentifier) {
        if (dialectIdentifier == DBLanguageDialectIdentifier.POSTGRES_PSQL) {
            if (pgsqlChameleonElementType == null) {
                DBLanguageDialect plsqlDialect = DBLanguageDialect.getLanguageDialect(DBLanguageDialectIdentifier.POSTGRES_PSQL);
                pgsqlChameleonElementType = plsqlDialect.getChameleonElementType(this);
            }
            return pgsqlChameleonElementType;
        }
        return super.getChameleonTokenType(dialectIdentifier);
    }
}