package com.dci.intellij.dbn.language.common.element.parser;

import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.LeafElementType;
import com.dci.intellij.dbn.language.common.element.lookup.ElementLookupContext;
import com.intellij.lang.PsiBuilder;

public class ParserContext extends ElementLookupContext {
    private long timestamp = System.currentTimeMillis();
    private ParserBuilder builder;
    private LeafElementType lastResolvedLeaf;
    private TokenType wavedTokenType;
    private int wavedTokenTypeOffset;

    public ParserContext(PsiBuilder builder, DBLanguageDialect languageDialect, double databaseVersion) {
        super(null, databaseVersion);
        this.builder = new ParserBuilder(builder, languageDialect);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public ParserBuilder getBuilder() {
        return builder;
    }

    public LeafElementType getLastResolvedLeaf() {
        return lastResolvedLeaf;
    }

    public void setLastResolvedLeaf(LeafElementType lastResolvedLeaf) {
        this.lastResolvedLeaf = lastResolvedLeaf;
    }

    public boolean isWavedTokenType(TokenType tokenType) {
        return tokenType == wavedTokenType && builder.getCurrentOffset() == wavedTokenTypeOffset;
    }

    public void setWavedTokenType(TokenType wavedTokenType) {
        this.wavedTokenType = wavedTokenType;
        this.wavedTokenTypeOffset = builder.getCurrentOffset();
    }
}
