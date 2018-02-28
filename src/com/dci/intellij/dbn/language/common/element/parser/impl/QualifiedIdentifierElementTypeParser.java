package com.dci.intellij.dbn.language.common.element.parser.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.language.common.ParseException;
import com.dci.intellij.dbn.language.common.SharedTokenTypeBundle;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.LeafElementType;
import com.dci.intellij.dbn.language.common.element.QualifiedIdentifierElementType;
import com.dci.intellij.dbn.language.common.element.TokenElementType;
import com.dci.intellij.dbn.language.common.element.impl.QualifiedIdentifierVariant;
import com.dci.intellij.dbn.language.common.element.lookup.ElementTypeLookupCache;
import com.dci.intellij.dbn.language.common.element.parser.AbstractElementTypeParser;
import com.dci.intellij.dbn.language.common.element.parser.ParseResult;
import com.dci.intellij.dbn.language.common.element.parser.ParseResultType;
import com.dci.intellij.dbn.language.common.element.parser.ParserBuilder;
import com.dci.intellij.dbn.language.common.element.parser.ParserContext;
import com.dci.intellij.dbn.language.common.element.path.ParsePathNode;
import com.dci.intellij.dbn.language.common.element.util.ParseBuilderErrorHandler;
import gnu.trove.THashSet;

public class QualifiedIdentifierElementTypeParser extends AbstractElementTypeParser<QualifiedIdentifierElementType> {
    public QualifiedIdentifierElementTypeParser(QualifiedIdentifierElementType elementType) {
        super(elementType);
    }

    public ParseResult parse(@NotNull ParsePathNode parentNode, boolean optional, int depth, ParserContext context) throws ParseException {
        ParserBuilder builder = context.getBuilder();
        logBegin(builder, optional, depth);
        ParsePathNode node = stepIn(parentNode, context);

        TokenElementType separatorToken = getElementType().getSeparatorToken();
        int matchedTokens = 0;

        QualifiedIdentifierVariant variant = getMostProbableParseVariant(builder, node);
        if (variant != null) {
            LeafElementType[] elementTypes = variant.getLeafs();

            for (LeafElementType elementType : elementTypes) {
                ParseResult result = elementType.getParser().parse(node, true, depth + 1, context);
                if (result.isNoMatch()) break;  else matchedTokens = matchedTokens + result.getMatchedTokens();

                if (elementType != elementTypes[elementTypes.length -1])  {
                    result = separatorToken.getParser().parse(node, true, depth + 1, context);
                    if (result.isNoMatch()) break; else matchedTokens = matchedTokens + result.getMatchedTokens();
                }
                node.incrementIndex(builder.getCurrentOffset());
            }

            if (matchedTokens > 0) {
                if (variant.isIncomplete()) {
                    Set<TokenType> expected = new THashSet<TokenType>();
                    expected.add(separatorToken.getTokenType());
                    ParseBuilderErrorHandler.updateBuilderError(expected, context);
                    return stepOut(node, context, depth, ParseResultType.PARTIAL_MATCH, matchedTokens);
                } else {
                    return stepOut(node, context, depth, ParseResultType.FULL_MATCH, matchedTokens);
                }
            }
        }

        return stepOut(node, context, depth, ParseResultType.NO_MATCH, matchedTokens);
    }

    private QualifiedIdentifierVariant getMostProbableParseVariant(ParserBuilder builder, ParsePathNode node) {
        QualifiedIdentifierElementType elementType = getElementType();
        TokenType separatorToken = elementType.getSeparatorToken().getTokenType();
        ElementTypeLookupCache lookupCache = elementType.getLookupCache();
        SharedTokenTypeBundle sharedTokenTypes = getElementType().getLanguage().getSharedTokenTypes();
        TokenType identifier = sharedTokenTypes.getIdentifier();


        List<TokenType> chan = new ArrayList<TokenType>();
        int offset = 0;
        boolean wasSeparator = true;
        TokenType tokenType = builder.lookAhead(offset);
        while (tokenType != null) {
            if (tokenType == separatorToken) {
                if (wasSeparator) chan.add(identifier);
                wasSeparator = true;
            } else {
                if (wasSeparator) {
                    if (tokenType.isIdentifier() || lookupCache.containsToken(tokenType))
                        chan.add(tokenType); else
                        chan.add(identifier);
                } else {
                   break;
                }
                wasSeparator = false;
            }
            offset++;
            tokenType = builder.lookAhead(offset);
            if (tokenType == null && wasSeparator) chan.add(identifier);
        }

        QualifiedIdentifierVariant mostProbableVariant = null;

        for (LeafElementType[] elementTypes : getElementType().getVariants()) {
            if (elementTypes.length <= chan.size()) {
                int matchedTokens = 0;
                for (int i=0; i<elementTypes.length; i++) {
                    if (elementTypes[i].getTokenType().matches(chan.get(i))) {
                        matchedTokens++;
                    }
                }
                if (mostProbableVariant == null || mostProbableVariant.getMatchedTokens() < matchedTokens) {
                    mostProbableVariant = mostProbableVariant == null ?
                            new QualifiedIdentifierVariant(elementTypes, matchedTokens) :
                            mostProbableVariant.replace(elementTypes, matchedTokens);
                }

            }
        }

        return mostProbableVariant;
    }

/*    private QualifiedIdentifierVariant getMostProbableParseVariant_(ParserBuilder builder, ParsePathNode node) {

        TokenElementType separatorToken = getElementType().getSeparatorToken();

        QualifiedIdentifierVariant mostProbableVariant = null;
        int initialCursorPosition = node.getCursorPosition();

        try {
            for (LeafElementType[] variants : getElementType().getVariants()) {
                int offset = 0;
                //PsiBuilder.Marker marker = builder.mark();
                int matchedTokens = 0;
                for (int i=0; i< variants.length; i++) {
                    TokenType tokenType = builder.lookAhead(offset);
                    // if no mach -> consider as partial if not first element

                    node.setCursorPosition(i);
                    if (match(variants[i], tokenType, node, true)) {
                        matchedTokens++;
                        offset++;


                        tokenType = builder.lookAhead(offset);
                        boolean isSeparator = tokenType == separatorToken.getTokenType();
                        boolean isFullMatch = matchedTokens == variants.length;
                        boolean isLastElement = i == variants.length - 1;

                        if (isLastElement) {
                            QualifiedIdentifierVariant variant = new QualifiedIdentifierVariant(variants, matchedTokens);
                            if ((isFullMatch && !isSeparator) || variant.containsNonIdentifierTokens()) {
                                //markerRollbackTo(marker);
                                return variant;
                            }

                            if (mostProbableVariant == null || matchedTokens > mostProbableVariant.getMatchedTokens()) {
                                mostProbableVariant = variant;
                            }
                            break;
                        } else {
                            if (!isSeparator) {  // is not separator token
                                QualifiedIdentifierVariant variant = new QualifiedIdentifierVariant(variants, matchedTokens);
                                if (mostProbableVariant == null || mostProbableVariant.getMatchedTokens() < variant.getMatchedTokens()) {
                                    mostProbableVariant = variant;
                                }
                                break;
                            }
                        }
                        offset++;
                    } else {
                        if (matchedTokens > 0)  {
                            QualifiedIdentifierVariant variant = new QualifiedIdentifierVariant(variants, matchedTokens);
                            if (variant.containsNonIdentifierTokens()) {
                                //markerRollbackTo(marker);
                                return variant;
                            }
                            if (mostProbableVariant == null || mostProbableVariant.getMatchedTokens() < variant.getMatchedTokens()) {
                                mostProbableVariant = variant;
                            }
                        }
                        break;
                    }
                }
            }
        }
        finally {
            node.setCursorPosition(initialCursorPosition);
        }

        return mostProbableVariant;
    }

    private boolean match(LeafElementType leafElementType, TokenType tokenType, ParsePathNode node, boolean leniant) {
        if (leafElementType instanceof IdentifierElementType) {
            return tokenType != null && (tokenType.isIdentifier() || (leniant && isSuppressibleReservedWord(tokenType, node, context)));
        } else if (leafElementType instanceof TokenElementType) {
            TokenElementType tokenElementType = (TokenElementType) leafElementType;
            return tokenElementType.getTokenType() == tokenType;
        }
        return false;
    }*/
}