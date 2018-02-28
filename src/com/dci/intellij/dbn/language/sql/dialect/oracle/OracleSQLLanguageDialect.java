package com.dci.intellij.dbn.language.sql.dialect.oracle;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.dci.intellij.dbn.language.common.ChameleonTokenType;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.DBLanguageDialectIdentifier;
import com.dci.intellij.dbn.language.common.DBLanguageSyntaxHighlighter;
import com.dci.intellij.dbn.language.common.element.ChameleonElementType;
import com.dci.intellij.dbn.language.common.element.TokenPairTemplate;
import com.dci.intellij.dbn.language.common.element.parser.TokenPairRangeMonitor;
import com.dci.intellij.dbn.language.sql.dialect.SQLLanguageDialect;
import com.intellij.lang.PsiBuilder;

public class OracleSQLLanguageDialect extends SQLLanguageDialect {
    ChameleonElementType plsqlChameleonElementType;
    public OracleSQLLanguageDialect() {
        super(DBLanguageDialectIdentifier.ORACLE_SQL);
    }

    @Override
    protected Set<ChameleonTokenType> createChameleonTokenTypes() {
        Set<ChameleonTokenType> tokenTypes = new HashSet<ChameleonTokenType>();
        DBLanguageDialect plsql = DBLanguageDialect.getLanguageDialect(DBLanguageDialectIdentifier.ORACLE_PLSQL);
        tokenTypes.add(new ChameleonTokenType(this, plsql));
        return tokenTypes;
    }

    @Override
    public ChameleonElementType getChameleonTokenType(DBLanguageDialectIdentifier dialectIdentifier) {
        if (dialectIdentifier == DBLanguageDialectIdentifier.ORACLE_PLSQL) {
            if (plsqlChameleonElementType == null) {
                DBLanguageDialect plsqlDialect = DBLanguageDialect.getLanguageDialect(DBLanguageDialectIdentifier.ORACLE_PLSQL);
                plsqlChameleonElementType = plsqlDialect.getChameleonElementType(this);
            }
            return plsqlChameleonElementType;
        }
        return super.getChameleonTokenType(dialectIdentifier);
    }

    @Override
    public Map<TokenPairTemplate, TokenPairRangeMonitor> createTokenPairRangeMonitors(PsiBuilder builder) {
        Map<TokenPairTemplate, TokenPairRangeMonitor> tokenPairRangeMonitors = super.createTokenPairRangeMonitors(builder);
        tokenPairRangeMonitors.put(TokenPairTemplate.BEGIN_END, new TokenPairRangeMonitor(builder, this, TokenPairTemplate.BEGIN_END));
        return tokenPairRangeMonitors;
    }

    protected DBLanguageSyntaxHighlighter createSyntaxHighlighter() {
        return new OracleSQLSyntaxHighlighter(this);
}

    protected OracleSQLParserDefinition createParserDefinition() {
        OracleSQLParser parser = new OracleSQLParser(this);
        return new OracleSQLParserDefinition(parser);
    }

}
