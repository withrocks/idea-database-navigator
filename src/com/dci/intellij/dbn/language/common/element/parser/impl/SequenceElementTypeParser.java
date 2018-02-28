package com.dci.intellij.dbn.language.common.element.parser.impl;

import java.util.Set;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.language.common.ParseException;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.IdentifierElementType;
import com.dci.intellij.dbn.language.common.element.IterationElementType;
import com.dci.intellij.dbn.language.common.element.SequenceElementType;
import com.dci.intellij.dbn.language.common.element.impl.ElementTypeRef;
import com.dci.intellij.dbn.language.common.element.parser.AbstractElementTypeParser;
import com.dci.intellij.dbn.language.common.element.parser.ParseResult;
import com.dci.intellij.dbn.language.common.element.parser.ParseResultType;
import com.dci.intellij.dbn.language.common.element.parser.ParserBuilder;
import com.dci.intellij.dbn.language.common.element.parser.ParserContext;
import com.dci.intellij.dbn.language.common.element.path.ParsePathNode;
import com.dci.intellij.dbn.language.common.element.path.PathNode;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.element.util.ParseBuilderErrorHandler;
import com.intellij.lang.PsiBuilder;

public class SequenceElementTypeParser<ET extends SequenceElementType> extends AbstractElementTypeParser<ET> {
    public SequenceElementTypeParser(ET elementType) {
        super(elementType);
    }

    public ParseResult parse(@NotNull ParsePathNode parentNode, boolean optional, int depth, ParserContext context) throws ParseException {
        ParserBuilder builder = context.getBuilder();
        logBegin(builder, optional, depth);
        ParsePathNode node = stepIn(parentNode, context);

        SequenceElementType elementType = getElementType();
        int matches = 0;
        int matchedTokens = 0;

        TokenType tokenType = builder.getTokenType();

        if (tokenType != null && !tokenType.isChameleon() && shouldParseElement(elementType, node, context)) {
            ElementTypeRef[] children = elementType.getChildren();
            while (node.getCursorPosition() < children.length) {
                int index = node.getCursorPosition();
                ElementTypeRef child = children[index];

                // is end of document
                if (tokenType == null || tokenType.isChameleon()) {
                    ParseResultType resultType =
                            child.isOptional() &&(child.isLast() || child.isOptionalFromHere()) ? ParseResultType.FULL_MATCH :
                            !child.isFirst() && !elementType.isExitIndex(index) ? ParseResultType.PARTIAL_MATCH : ParseResultType.NO_MATCH;
                    return stepOut(node, context, depth, resultType, matchedTokens);
                }

                if (context.check(child)) {
                    ParseResult result = ParseResult.createNoMatchResult();
                    // current token can still be part of the iterated element.
                    //if (elementTypes[i].containsToken(tokenType)) {
                    if (shouldParseElement(child.getElementType(), node, context)) {

                        //node = node.createVariant(builder.getCurrentOffset(), i);
                        result = child.getParser().parse(node, child.isOptional(), depth + 1, context);

                        if (result.isMatch()) {
                            matchedTokens = matchedTokens + result.getMatchedTokens();
                            tokenType = builder.getTokenType();
                            matches++;
                        }
                    }

                    // not matched and not optional
                    if (result.isNoMatch() && !child.isOptional()) {
                        boolean isWeakMatch = matches < 2 && matchedTokens < 3 && index > 1 && ignoreFirstMatch();

                        if (child.isFirst()|| elementType.isExitIndex(index) || isWeakMatch || matches == 0) {
                            //if (isFirst(i) || isExitIndex(i)) {
                            return stepOut(node, context, depth, ParseResultType.NO_MATCH, matchedTokens);
                        }

                        index = advanceLexerToNextLandmark(node, context);

                        if (index <= 0) {
                            // no landmarks found or landmark in parent found
                            return stepOut(node, context, depth, ParseResultType.PARTIAL_MATCH, matchedTokens);
                        } else {
                            // local landmarks found

                            tokenType = builder.getTokenType();
                            node.setCursorPosition(index);
                            continue;
                        }
                    }
                }


                // if is last element
                if (child.isLast()) {
                    //matches == 0 reaches this stage only if all sequence elements are optional
                    ParseResultType resultType = matches == 0 ? ParseResultType.NO_MATCH : ParseResultType.FULL_MATCH;
                    return stepOut(node, context, depth, resultType, matchedTokens);
                }
                node.incrementIndex(builder.getCurrentOffset());
            }
        }

        return stepOut(node, context, depth, ParseResultType.NO_MATCH, matchedTokens);
    }

    private boolean ignoreFirstMatch() {
        ElementTypeRef firstChild = getElementType().getChild(0);
        if (firstChild.getElementType() instanceof IdentifierElementType) {
            IdentifierElementType identifierElementType = (IdentifierElementType) firstChild.getElementType();
            return !identifierElementType.isDefinition();
        }
        return false;
    }

    private int advanceLexerToNextLandmark(ParsePathNode node, ParserContext context) throws ParseException {
        int siblingPosition = node.getCursorPosition();
        ParserBuilder builder = context.getBuilder();
        PsiBuilder.Marker marker = builder.mark(null);
        SequenceElementType elementType = getElementType();
        Set<TokenType> possibleTokens = elementType.getFirstPossibleTokensFromIndex(context, siblingPosition);
        ParseBuilderErrorHandler.updateBuilderError(possibleTokens, context);

        TokenType tokenType = builder.getTokenType();
        siblingPosition++;
        while (tokenType != null) {
            int newIndex = getLandmarkIndex(tokenType, siblingPosition, node);

            // no landmark hit -> spool the builder
            if (newIndex == 0) {
                builder.advanceLexer(node);
                tokenType = builder.getTokenType();
            } else {
                //builder.markerDone(marker, getElementBundle().getUnknownElementType());
                marker.error("Unrecognized statement");
                return newIndex;
            }
        }
        //builder.markerDone(marker, getElementBundle().getUnknownElementType());
        marker.error("Unrecognized statement 1");
        return 0;
    }

    protected int getLandmarkIndex(TokenType tokenType, int index, ParsePathNode node) {
        if (tokenType.isParserLandmark()) {
            PathNode statementPathNode = node.getPathNode(ElementTypeAttribute.STATEMENT);
            if (statementPathNode != null && statementPathNode.getElementType().getLookupCache().couldStartWithToken(tokenType)) {
                return -1;
            }
            ElementTypeRef[] children = getElementType().getChildren();
            for (int i=index; i< children.length; i++) {
                // check children landmarks
                if (children[i].getLookupCache().couldStartWithToken(tokenType)) {
                    return i;
                }
            }

            ParsePathNode parseNode = node;
            while (parseNode != null) {
                ElementType elementType = parseNode.getElementType();
                if (elementType instanceof SequenceElementType) {
                    SequenceElementType sequenceElementType = (SequenceElementType) elementType;
                    if ( sequenceElementType.containsLandmarkTokenFromIndex(tokenType, parseNode.getCursorPosition() + 1)) {
                        return -1;
                    }
                } else  if (elementType instanceof IterationElementType) {
                    IterationElementType iterationElementType = (IterationElementType) elementType;
                    if (iterationElementType.isSeparator(tokenType)) {
                        return -1;
                    }
                }
                parseNode = parseNode.getParent();
            }
        }
        return 0;
    }


    protected ParsePathNode advanceToLandmark_New(ParserBuilder builder, ParsePathNode node) {
        TokenType tokenType = builder.getTokenType();
        while (tokenType != null && !tokenType.isParserLandmark()) {
            builder.advanceLexer(node);
            tokenType = builder.getTokenType();
        }

        // scan current sequence
        ElementTypeRef[] children = getElementType().getChildren();
        int siblingIndex = node.getCursorPosition();
        while (siblingIndex < children.length) {
            int builderOffset = builder.getCurrentOffset();
            siblingIndex = node.incrementIndex(builderOffset);
            // check children landmarks
            if (children[siblingIndex].getLookupCache().couldStartWithToken(tokenType)) {
                return node;
            }
        }

        ParsePathNode parentNode = node.getParent();
        while (parentNode != null) {
            ElementType elementType = parentNode.getElementType();
            if (elementType instanceof SequenceElementType) {
                parentNode = advanceToLandmark_New(builder, node);

            } else if (elementType instanceof IterationElementType) {
                IterationElementType iterationElementType = (IterationElementType) elementType;
                if (iterationElementType.isSeparator(tokenType)) {
                    return parentNode;
                }
            }
            parentNode = parentNode.getParent();
        }

        return null;
    }


}
