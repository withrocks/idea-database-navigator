package com.dci.intellij.dbn.language.common.element.parser.impl;

import java.util.Set;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.language.common.ParseException;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.BasicElementType;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.IterationElementType;
import com.dci.intellij.dbn.language.common.element.SequenceElementType;
import com.dci.intellij.dbn.language.common.element.TokenElementType;
import com.dci.intellij.dbn.language.common.element.lookup.ElementTypeLookupCache;
import com.dci.intellij.dbn.language.common.element.parser.AbstractElementTypeParser;
import com.dci.intellij.dbn.language.common.element.parser.ParseResult;
import com.dci.intellij.dbn.language.common.element.parser.ParseResultType;
import com.dci.intellij.dbn.language.common.element.parser.ParserBuilder;
import com.dci.intellij.dbn.language.common.element.parser.ParserContext;
import com.dci.intellij.dbn.language.common.element.path.ParsePathNode;
import com.dci.intellij.dbn.language.common.element.util.ParseBuilderErrorHandler;
import com.intellij.lang.PsiBuilder;

public class IterationElementTypeParser extends AbstractElementTypeParser<IterationElementType> {
    public IterationElementTypeParser(IterationElementType elementType) {
        super(elementType);
    }

    public ParseResult parse(@NotNull ParsePathNode parentNode, boolean optional, int depth, ParserContext context) throws ParseException {
        ParserBuilder builder = context.getBuilder();
        logBegin(builder, optional, depth);
        ParsePathNode node = stepIn(parentNode, context);

        IterationElementType elementType = getElementType();
        ElementType iteratedElementType = elementType.getIteratedElementType();
        TokenElementType[] separatorTokens = elementType.getSeparatorTokens();

        int iterations = 0;
        int matchedTokens = 0;
        //TokenType tokenType = (TokenType) builder.getTokenType();
        // check if the token objectType can be part of this iteration
        //if (isDummyToken(builder.getTokenText()) || isSuppressibleReservedWord(tokenType, node) || iteratedElementType.containsToken(tokenType)) {
            ParseResult result = iteratedElementType.getParser().parse(node, optional, depth + 1, context);


            // check first iteration element
            if (result.isMatch()) {
                if (node.isRecursive(node.getStartOffset())) {
                    ParseResultType resultType = matchesMinIterations(iterations) ? ParseResultType.FULL_MATCH : ParseResultType.NO_MATCH;
                    return stepOut(node, context, depth, resultType, matchedTokens);
                }
                while (true) {
                    iterations++;
                    // check separator
                    // if not matched just step out
                    if (separatorTokens != null) {
                        for (TokenElementType separatorToken : separatorTokens) {
                            result = separatorToken.getParser().parse(node, false, depth + 1, context);
                            matchedTokens = matchedTokens + result.getMatchedTokens();
                            if (result.isMatch()) break;
                        }

                        if (result.isNoMatch()) {
                            // if NO_MATCH, no additional separator found, hence then iteration should exit with MATCH
                            ParseResultType resultType =
                                    matchesMinIterations(iterations) ?
                                        matchesIterations(iterations) ?
                                            ParseResultType.FULL_MATCH :
                                            ParseResultType.PARTIAL_MATCH :
                                    ParseResultType.NO_MATCH;
                            return stepOut(node, context, depth, resultType, matchedTokens);
                        } else {
                            node.setCurrentOffset(builder.getCurrentOffset());
                        }
                    }

                    // check consecutive iterated element
                    // if not matched, step out with error

                    result = iteratedElementType.getParser().parse(node, true, depth + 1, context);

                    if (result.isNoMatch()) {
                        // missing separators permit ending the iteration as valid at any time
                        if (separatorTokens == null) {
                            ParseResultType resultType =
                                    matchesMinIterations(iterations) ?
                                        matchesIterations(iterations) ?
                                            ParseResultType.FULL_MATCH :
                                            ParseResultType.PARTIAL_MATCH :
                                    ParseResultType.NO_MATCH;
                            return stepOut(node, context, depth, resultType, matchedTokens);
                        } else {
                            if (matchesMinIterations(iterations)) {
                                boolean exit = advanceLexerToNextLandmark(node, false, context);
                                if (exit){
                                    return stepOut(node, context, depth, ParseResultType.PARTIAL_MATCH, matchedTokens);
                                }
                            } else {
                                return stepOut(node, context, depth, ParseResultType.NO_MATCH, matchedTokens);
                            }
                        }
                    } else {
                        matchedTokens = matchedTokens + result.getMatchedTokens();
                    }
                }
            }
        //}
        if (!optional) {
            //updateBuilderError(builder, this);
        }
        return stepOut(node, context, depth, ParseResultType.NO_MATCH, matchedTokens);
    }

    private boolean advanceLexerToNextLandmark(ParsePathNode parentNode, boolean lenient, ParserContext context) throws ParseException {
        ParserBuilder builder = context.getBuilder();
        IterationElementType elementType = getElementType();

        PsiBuilder.Marker marker = builder.mark(null);
        ElementType iteratedElementType = elementType.getIteratedElementType();
        TokenElementType[] separatorTokens = elementType.getSeparatorTokens();

        if (!lenient) {
            ElementTypeLookupCache lookupCache = iteratedElementType.getLookupCache();
            Set<TokenType> expectedTokens = lookupCache.collectFirstPossibleTokens(context.reset());
            ParseBuilderErrorHandler.updateBuilderError(expectedTokens, context);
        }
        boolean advanced = false;
        BasicElementType unknownElementType = getElementBundle().getUnknownElementType();
        while (!builder.eof()) {
            TokenType tokenType = builder.getTokenType();
            if (tokenType == null || tokenType.isChameleon())  break;

            if (tokenType.isParserLandmark()) {
                if (separatorTokens != null) {
                    for (TokenElementType separatorToken : separatorTokens) {
                        if (separatorToken.getLookupCache().containsLandmarkToken(tokenType)) {
                            builder.markerDone(marker, unknownElementType);
                            return false;
                        }
                    }
                }

                ParsePathNode parseNode = parentNode;
                while (parseNode != null) {
                    if (parseNode.getElementType() instanceof SequenceElementType) {
                        SequenceElementType sequenceElementType = (SequenceElementType) parseNode.getElementType();
                        int index = parseNode.getCursorPosition();
                        if (!iteratedElementType.getLookupCache().containsToken(tokenType) && sequenceElementType.containsLandmarkTokenFromIndex(tokenType, index + 1)) {
                            if (advanced || !lenient) {
                                builder.markerDone(marker, unknownElementType);
                            } else {
                                builder.markerRollbackTo(marker, null);
                            }
                            return true;
                        }

                    }
                    parseNode = parseNode.getParent();
                }
            }
            builder.advanceLexer(parentNode);
            advanced = true;
        }
        if (advanced || !lenient)
            builder.markerDone(marker, unknownElementType); else
            builder.markerRollbackTo(marker, null);
        return true;
    }

    private boolean matchesMinIterations(int iterations) {
        Integer minIterations = getElementType().getMinIterations();
        return minIterations == null || minIterations <= iterations;
    }

    private boolean matchesIterations(int iterations) {
        int[]elementsCountVariants = getElementType().getElementsCountVariants();
        if (elementsCountVariants != null) {
            for (int elementsCountVariant: elementsCountVariants) {
                if (elementsCountVariant == iterations) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }


}
