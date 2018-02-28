package com.dci.intellij.dbn.language.common.element.parser;

import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.TokenTypeCategory;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.TokenElementType;
import com.dci.intellij.dbn.language.common.element.TokenPairTemplate;
import com.dci.intellij.dbn.language.common.element.impl.WrappingDefinition;
import com.dci.intellij.dbn.language.common.element.path.ParsePathNode;
import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;

public class ParserBuilder {
    private PsiBuilder builder;
    private Map<TokenPairTemplate, TokenPairRangeMonitor> tokenPairRangeMonitors;
    private String cachedTokenText;


    public ParserBuilder(PsiBuilder builder, DBLanguageDialect languageDialect) {
        this.builder = builder;
        builder.setDebugMode(true);
        tokenPairRangeMonitors = languageDialect.createTokenPairRangeMonitors(builder);
    }

    public void advanceLexer(@NotNull ParsePathNode node) {
        advanceLexer(node, true);
    }

    public void advanceLexer(@NotNull ParsePathNode node, boolean explicit) {
        TokenType tokenType = (TokenType) builder.getTokenType();
        TokenPairRangeMonitor tokenPairRangeMonitor = getTokenPairRangeMonitor(tokenType);
        if (tokenPairRangeMonitor != null) {
            tokenPairRangeMonitor.compute(node, explicit);
        }
        builder.advanceLexer();
        cachedTokenText = null;
    }

    @Nullable
    public TokenPairRangeMonitor getTokenPairRangeMonitor(TokenType tokenType) {
        if (tokenType != null) {
            TokenPairTemplate tokenPairTemplate = tokenType.getTokenPairTemplate();
            if (tokenPairTemplate != null) {
                return tokenPairRangeMonitors.get(tokenPairTemplate);
            }
        }
        return null;
    }


    public String getTokenText() {
        if (cachedTokenText == null) {
            cachedTokenText = builder.getTokenText();
        }
        return cachedTokenText;
    }

    public TokenType getTokenType() {
        return (TokenType) builder.getTokenType();
    }

    public boolean eof() {
        return builder.eof();
    }

    public int getCurrentOffset() {
        return builder.getCurrentOffset();
    }

    public TokenType lookAhead(int steps) {
        return (TokenType) builder.lookAhead(steps);
    }


    public TokenType lookBack(int steps) {
        int cursor = -1;
        int count = 0;
        TokenType tokenType = (TokenType) builder.rawLookup(cursor);
        while (tokenType != null && count <= steps) {
            TokenTypeCategory category = tokenType.getCategory();
            if (category != TokenTypeCategory.WHITESPACE && category != TokenTypeCategory.COMMENT) {
                count++;
                if (count == steps) return tokenType;
            }
            cursor--;
            tokenType = (TokenType) builder.rawLookup(cursor);
        }
        return null;
    }

    public IElementType rawLookup(int steps) {
        return builder.rawLookup(steps);
    }


    public void error(String messageText) {
        builder.error(messageText);
    }

    public ASTNode getTreeBuilt() {
        for (TokenPairRangeMonitor tokenPairRangeMonitor : tokenPairRangeMonitors.values()) {
            tokenPairRangeMonitor.cleanup(true);
        }
        return builder.getTreeBuilt();
    }

    public void setDebugMode(boolean debugMode) {
        builder.setDebugMode(debugMode);
    }

    public PsiBuilder.Marker mark(@Nullable ParsePathNode node){
        PsiBuilder.Marker marker = builder.mark();
        if (node != null) {
            WrappingDefinition wrapping = node.getElementType().getWrapping();
            if (wrapping != null) {
                TokenElementType beginElementType = wrapping.getBeginElementType();
                TokenType beginTokenType = beginElementType.getTokenType();
                while(builder.getTokenType() == beginTokenType) {
                    PsiBuilder.Marker beginTokenMarker = builder.mark();
                    advanceLexer(node, false);
                    beginTokenMarker.done((IElementType) beginElementType);
                }
            }
        }
        return marker;
    }

    public void markerRollbackTo(PsiBuilder.Marker marker, @Nullable ParsePathNode node) {
        if (marker != null) {
            marker.rollbackTo();
            for (TokenPairRangeMonitor tokenPairRangeMonitor : tokenPairRangeMonitors.values()) {
                tokenPairRangeMonitor.rollback();
            }
            cachedTokenText = null;
        }
    }

    public void markerDone(PsiBuilder.Marker marker, ElementType elementType) {
        markerDone(marker, elementType, null);
    }

    public void markerDone(PsiBuilder.Marker marker, ElementType elementType, @Nullable ParsePathNode node) {
        if (marker != null) {
            if (node != null) {
                WrappingDefinition wrapping = node.getElementType().getWrapping();
                if (wrapping != null) {
                    TokenElementType endElementType = wrapping.getEndElementType();
                    TokenType endTokenType = endElementType.getTokenType();
                    while (builder.getTokenType() == endTokenType && !isExplicitRange(endTokenType)) {
                        PsiBuilder.Marker endTokenMarker = builder.mark();
                        advanceLexer(node, false);
                        endTokenMarker.done((IElementType) endElementType);
                    }
                }
            }
            marker.done((IElementType) elementType);
        }
    }

    public void markerDrop(PsiBuilder.Marker marker) {
        if (marker != null) {
            marker.drop();
        }
    }

    public boolean isExplicitRange(TokenType tokenType) {
        TokenPairRangeMonitor tokenPairRangeMonitor = getTokenPairRangeMonitor(tokenType);
        return tokenPairRangeMonitor != null && tokenPairRangeMonitor.isExplicitRange();
    }

    public void setExplicitRange(TokenType tokenType, boolean value) {
        TokenPairRangeMonitor tokenPairRangeMonitor = getTokenPairRangeMonitor(tokenType);
        if (tokenPairRangeMonitor != null) tokenPairRangeMonitor.setExplicitRange(value);
    }
}
