package com.dci.intellij.dbn.language.common.element.lookup;

import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.LeafElementType;
import com.dci.intellij.dbn.language.common.element.TokenElementType;
import com.dci.intellij.dbn.language.common.element.path.PathNode;

public class TokenElementTypeLookupCache extends LeafElementTypeLookupCache<TokenElementType>{
    public TokenElementTypeLookupCache(TokenElementType elementType) {
        super(elementType);
    }

    public void init() {
        TokenType tokenType = getElementType().getTokenType();
        allPossibleTokens.add(tokenType);
        firstPossibleTokens.add(tokenType);
        firstRequiredTokens.add(tokenType);
    }

    @Override
    public boolean containsToken(TokenType tokenType) {
        return getElementType().getTokenType() == tokenType;
    }

    @Override
    boolean initAsFirstPossibleLeaf(LeafElementType leaf, ElementType source) {
        return false;
    }

    @Override
    boolean initAsFirstRequiredLeaf(LeafElementType leaf, ElementType source) {
        return false;
    }

    public boolean containsLandmarkToken(TokenType tokenType, PathNode node) {
       return containsToken(tokenType);
    }

    public boolean startsWithIdentifier(PathNode node) {
        return getElementType().getTokenType().isIdentifier();
    }
}