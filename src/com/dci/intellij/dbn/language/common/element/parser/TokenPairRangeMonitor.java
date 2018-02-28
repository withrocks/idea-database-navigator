package com.dci.intellij.dbn.language.common.element.parser;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.SimpleTokenType;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.TokenTypeBundle;
import com.dci.intellij.dbn.language.common.element.TokenPairTemplate;
import com.dci.intellij.dbn.language.common.element.path.ParsePathNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.util.containers.Stack;

public class TokenPairRangeMonitor {
    private int stackSize = 0;
    private Stack<TokenPairRangeMarker> markersStack = new Stack<TokenPairRangeMarker>();
    private PsiBuilder builder;

    private SimpleTokenType beginTokenType;
    private SimpleTokenType endTokenType;


    public TokenPairRangeMonitor(PsiBuilder builder, DBLanguageDialect languageDialect, TokenPairTemplate template) {
        this.builder = builder;

        TokenTypeBundle parserTokenTypes = languageDialect.getParserTokenTypes();
        beginTokenType = parserTokenTypes.getTokenType(template.getBeginToken());
        endTokenType = parserTokenTypes.getTokenType(template.getEndToken());
    }

    /**
     * cleanup all markers registered after the builder offset (remained dirty after a marker rollback)
     */
    public void rollback() {
        int builderOffset = builder.getCurrentOffset();
        while (markersStack.size() > 0) {
            TokenPairRangeMarker lastMarker = markersStack.peek();
            if (lastMarker.getOffset() >= builderOffset) {
                markersStack.pop();
                lastMarker.dropMarker();
                if (stackSize > 0) stackSize--;
            } else {
                break;
            }
        }
    }

    public void compute(@NotNull ParsePathNode node, boolean explicit) {
        TokenType tokenType = (TokenType) builder.getTokenType();
        if (tokenType == beginTokenType) {
            stackSize++;
            TokenPairRangeMarker marker = new TokenPairRangeMarker(node, builder, explicit);
            markersStack.push(marker);
        } else if (tokenType == endTokenType) {
            if (stackSize > 0) stackSize--;
            if (markersStack.size() > 0) {
/*
                NestedRangeMarker marker = markersStack.peek();
                ParsePathNode markerNode = marker.getParseNode();
                if (markerNode == node) {

                } else if (markerNode.isSiblingOf(node)) {

                } else if (node.isSiblingOf(markerNode)) {
                    WrappingDefinition childWrapping = node.getElementType().getWrapping();
                    WrappingDefinition parentWrapping = markerNode.getElementType().getWrapping();
                    if (childWrapping != null && childWrapping.equals(parentWrapping)) {

                    }
                }
*/
                cleanup(false);
            }
        }
    }

    public void cleanup(boolean force) {
        if (force) stackSize = 0;
        while(markersStack.size() > stackSize) {
            TokenPairRangeMarker lastMarker = markersStack.pop();
            lastMarker.dropMarker();
        }
    }

    public boolean isExplicitRange() {
        if (!markersStack.isEmpty()) {
            TokenPairRangeMarker tokenPairRangeMarker = markersStack.peek();
            return tokenPairRangeMarker.isExplicit();
        }

        return false;
    }

    public void setExplicitRange(boolean value) {
        if (!markersStack.isEmpty()) {
            TokenPairRangeMarker tokenPairRangeMarker = markersStack.peek();
            tokenPairRangeMarker.setExplicit(value);
        }
    }
}
