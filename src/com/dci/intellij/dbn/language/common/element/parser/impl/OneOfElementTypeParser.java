package com.dci.intellij.dbn.language.common.element.parser.impl;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.language.common.ParseException;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.OneOfElementType;
import com.dci.intellij.dbn.language.common.element.impl.ElementTypeRef;
import com.dci.intellij.dbn.language.common.element.parser.AbstractElementTypeParser;
import com.dci.intellij.dbn.language.common.element.parser.ParseResult;
import com.dci.intellij.dbn.language.common.element.parser.ParseResultType;
import com.dci.intellij.dbn.language.common.element.parser.ParserBuilder;
import com.dci.intellij.dbn.language.common.element.parser.ParserContext;
import com.dci.intellij.dbn.language.common.element.path.ParsePathNode;

public class OneOfElementTypeParser extends AbstractElementTypeParser<OneOfElementType> {
    public OneOfElementTypeParser(OneOfElementType elementType) {
        super(elementType);
    }

    public ParseResult parse(@NotNull ParsePathNode parentNode, boolean optional, int depth, ParserContext context) throws ParseException {
        ParserBuilder builder = context.getBuilder();
        logBegin(builder, optional, depth);
        ParsePathNode node = stepIn(parentNode, context);

        OneOfElementType elementType = getElementType();
        elementType.sort();
        TokenType tokenType = builder.getTokenType();

        if (tokenType!= null && !tokenType.isChameleon()) {
            // TODO !!!! if elementType is an identifier: then BUILD VARIANTS!!!
            for (ElementTypeRef child : elementType.getChildren()) {
                if (context.check(child) && shouldParseElement(child.getElementType(), node, context)) {
                    ParseResult result = child.getParser().parse(node, true, depth + 1, context);
                    if (result.isMatch()) {
                        return stepOut(node, context, depth, result.getType(), result.getMatchedTokens());
                    }
                }
            }
            if (!optional) {
                //updateBuilderError(builder, this);
            }

        }
        return stepOut(node, context, depth, ParseResultType.NO_MATCH, 0);
    }

}