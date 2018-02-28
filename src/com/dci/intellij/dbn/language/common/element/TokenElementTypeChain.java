package com.dci.intellij.dbn.language.common.element;

import java.util.ArrayList;
import java.util.List;

public class TokenElementTypeChain {
    private List<TokenElementType> elementTypes = new ArrayList<TokenElementType>();
    private double version ;

    private TokenElementTypeChain(){};

    public TokenElementTypeChain(TokenElementType first) {
        elementTypes.add(first);
    }

    public void append(TokenElementType tokenElementType) {
        elementTypes.add(tokenElementType);
    }

    public List<TokenElementType> getElementTypes() {
        return elementTypes;
    }

    public double getVersion() {
        return version;
    }

    public TokenElementTypeChain createVariant(TokenElementType tokenElementType) {
        TokenElementTypeChain variant = new TokenElementTypeChain();
        variant.elementTypes.addAll(elementTypes);
        variant.elementTypes.add(tokenElementType);
        return variant;
    }

    @Override
    public String toString() {
        return elementTypes.toString();
    }
}
