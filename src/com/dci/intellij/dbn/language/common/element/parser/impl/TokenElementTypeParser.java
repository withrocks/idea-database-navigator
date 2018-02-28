package com.dci.intellij.dbn.language.common.element.parser.impl;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.language.common.ParseException;
import com.dci.intellij.dbn.language.common.SharedTokenTypeBundle;
import com.dci.intellij.dbn.language.common.SimpleTokenType;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.TokenElementType;
import com.dci.intellij.dbn.language.common.element.parser.AbstractElementTypeParser;
import com.dci.intellij.dbn.language.common.element.parser.ParseResult;
import com.dci.intellij.dbn.language.common.element.parser.ParseResultType;
import com.dci.intellij.dbn.language.common.element.parser.ParserBuilder;
import com.dci.intellij.dbn.language.common.element.parser.ParserContext;
import com.dci.intellij.dbn.language.common.element.path.ParsePathNode;
import com.intellij.lang.PsiBuilder;

public class TokenElementTypeParser extends AbstractElementTypeParser<TokenElementType> {
    public TokenElementTypeParser(TokenElementType elementType) {
        super(elementType);
    }

    public ParseResult parse(@NotNull ParsePathNode parentNode, boolean optional, int depth, ParserContext context) throws ParseException {
        ParserBuilder builder = context.getBuilder();
        logBegin(builder, optional, depth);

        TokenType tokenType = builder.getTokenType();
        TokenElementType elementType = getElementType();
        if (tokenType == elementType.getTokenType() || isDummyToken(builder.getTokenText())) {
            SharedTokenTypeBundle sharedTokenTypes = getElementBundle().getTokenTypeBundle().getSharedTokenTypes();
            SimpleTokenType leftParenthesis = sharedTokenTypes.getChrLeftParenthesis();
            SimpleTokenType dot = sharedTokenTypes.getChrDot();

            if (tokenType.isSuppressibleReservedWord()) {
                TokenType nextTokenType = builder.lookAhead(1);
                if (nextTokenType == dot && !elementType.isNextPossibleToken(dot, parentNode, context)) {
                    context.setWavedTokenType(tokenType);
                    return stepOut(null, null, context, depth, ParseResultType.NO_MATCH, 0);
                }
                if (tokenType.isFunction() && elementType.getFlavor() == null) {
                    if (nextTokenType != leftParenthesis && elementType.isNextPossibleToken(leftParenthesis, parentNode, context)) {
                        context.setWavedTokenType(tokenType);
                        return stepOut(null, null, context, depth, ParseResultType.NO_MATCH, 0);
                    }
                }
            }

            PsiBuilder.Marker marker = builder.mark(null);
            builder.advanceLexer(parentNode);
            return stepOut(marker, null, context, depth, ParseResultType.FULL_MATCH, 1);
        }
        return stepOut(null, null, context, depth, ParseResultType.NO_MATCH, 0);
    }
}
