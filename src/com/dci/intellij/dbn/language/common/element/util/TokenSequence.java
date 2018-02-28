package com.dci.intellij.dbn.language.common.element.util;

import com.dci.intellij.dbn.language.common.element.TokenElementType;

import java.util.ArrayList;
import java.util.List;

public class TokenSequence {
    private List<TokenElementType> elements = new ArrayList<TokenElementType>();

    public void createVariant(TokenElementType additionalElement) {
        TokenSequence sequence = new TokenSequence();
        sequence.elements.addAll(elements);
        sequence.elements.add(additionalElement);
    }
}
