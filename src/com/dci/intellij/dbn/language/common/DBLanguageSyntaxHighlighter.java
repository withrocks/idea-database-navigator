package com.dci.intellij.dbn.language.common;

import java.util.HashMap;
import java.util.Map;
import org.jdom.Document;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.util.CommonUtil;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;

public abstract class DBLanguageSyntaxHighlighter extends SyntaxHighlighterBase {
    protected Map colors = new HashMap();
    private Map backgrounds = new HashMap();

    private DBLanguageDialect languageDialect;
    private TokenTypeBundle tokenTypes;

    public DBLanguageSyntaxHighlighter(DBLanguageDialect languageDialect, String tokenTypesFile) {
        Document document = CommonUtil.loadXmlFile(getClass(), tokenTypesFile);
        tokenTypes = new TokenTypeBundle(languageDialect, document);
        this.languageDialect = languageDialect;
    }

    public DBLanguageDialect getLanguageDialect() {
        return languageDialect;
    }

    @NotNull
    protected abstract Lexer createLexer();

    @NotNull
    public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
        return pack(getAttributeKeys(tokenType, backgrounds), getAttributeKeys(tokenType, colors));
    }

    private static TextAttributesKey getAttributeKeys(IElementType tokenType, Map map) {
        return (TextAttributesKey) map.get(tokenType);
    }

    public TokenTypeBundle getTokenTypes() {
        return tokenTypes;
    }

    @NotNull
    public Lexer getHighlightingLexer() {
        return createLexer();
    }
}
