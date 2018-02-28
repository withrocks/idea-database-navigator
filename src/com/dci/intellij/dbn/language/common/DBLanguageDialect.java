package com.dci.intellij.dbn.language.common;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.language.common.element.ChameleonElementType;
import com.dci.intellij.dbn.language.common.element.TokenPairTemplate;
import com.dci.intellij.dbn.language.common.element.parser.TokenPairRangeMonitor;
import com.intellij.lang.LanguageDialect;
import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IFileElementType;

public abstract class DBLanguageDialect extends LanguageDialect implements DBFileElementTypeProvider {
    private DBLanguageDialectIdentifier identifier;
    private DBLanguageSyntaxHighlighter syntaxHighlighter;
    private DBLanguageParserDefinition parserDefinition;
    private IFileElementType fileElementType;
    private Set<ChameleonTokenType> chameleonTokens;
    private ChameleonElementType chameleonElementType;
    private static Map<DBLanguageDialectIdentifier, DBLanguageDialect> register = new EnumMap<DBLanguageDialectIdentifier, DBLanguageDialect>(DBLanguageDialectIdentifier.class);

    public DBLanguageDialect(@NonNls @NotNull DBLanguageDialectIdentifier identifier, @NotNull DBLanguage baseLanguage) {
        super(identifier.getValue(), baseLanguage);
        this.identifier = identifier;
        register.put(identifier, this);
    }

    protected abstract Set<ChameleonTokenType> createChameleonTokenTypes();
    protected abstract DBLanguageSyntaxHighlighter createSyntaxHighlighter() ;
    protected abstract DBLanguageParserDefinition createParserDefinition();
    protected abstract IFileElementType createFileElementType();
    public ChameleonElementType getChameleonTokenType(DBLanguageDialectIdentifier dialectIdentifier) {
        throw new IllegalArgumentException("Language " + getID() + " does not support chameleons of type " + dialectIdentifier.getValue() );
    }

    public static DBLanguageDialect getLanguageDialect(DBLanguageDialectIdentifier identifier) {
        return register.get(identifier);
    }

    public DBLanguageDialectIdentifier getIdentifier() {
        return identifier;
    }

    public boolean isReservedWord(String identifier) {
        return getParserTokenTypes().isReservedWord(identifier);
    }

    @NotNull
    public DBLanguage getBaseLanguage() {
        return (DBLanguage) super.getBaseLanguage();
    }

    public SharedTokenTypeBundle getSharedTokenTypes() {
        return getBaseLanguage().getSharedTokenTypes();
    }

    public synchronized DBLanguageSyntaxHighlighter getSyntaxHighlighter() {
        if (syntaxHighlighter == null) {
            syntaxHighlighter = createSyntaxHighlighter();
        }
        return syntaxHighlighter;
    }

    @NotNull
    public synchronized DBLanguageParserDefinition getParserDefinition() {
        if (parserDefinition == null) {
            parserDefinition = createParserDefinition();
        }
        return parserDefinition;
    }

    public synchronized IFileElementType getFileElementType() {
        if (fileElementType == null) {
            fileElementType = createFileElementType();
        }
        return fileElementType;
    }

    public TokenTypeBundle getParserTokenTypes() {
        return getParserDefinition().getParser().getTokenTypes();
    }

    public TokenTypeBundle getHighlighterTokenTypes() {
        return getSyntaxHighlighter().getTokenTypes();
    }

    public TokenType getInjectedLanguageToken(DBLanguageDialectIdentifier dialectIdentifier) {
        if (chameleonTokens == null) {
            chameleonTokens = createChameleonTokenTypes();
            if (chameleonTokens == null) chameleonTokens = new HashSet<ChameleonTokenType>();
        }
        for (ChameleonTokenType chameleonToken : chameleonTokens) {
            if (chameleonToken.getInjectedLanguage().identifier == dialectIdentifier) {
                return chameleonToken;
            }
        }
        return null;
    }

    public synchronized ChameleonElementType getChameleonElementType(DBLanguageDialect parentLanguage) {
        if (chameleonElementType == null) {
            chameleonElementType = new ChameleonElementType(this, parentLanguage);
        }
        return chameleonElementType;
    }

    public Map<TokenPairTemplate,TokenPairRangeMonitor> createTokenPairRangeMonitors(PsiBuilder builder){
        Map<TokenPairTemplate,TokenPairRangeMonitor> tokenPairRangeMonitors = new EnumMap<TokenPairTemplate, TokenPairRangeMonitor>(TokenPairTemplate.class);
        tokenPairRangeMonitors.put(TokenPairTemplate.PARENTHESES, new TokenPairRangeMonitor(builder, this, TokenPairTemplate.PARENTHESES));
        return tokenPairRangeMonitors;
    }
}
