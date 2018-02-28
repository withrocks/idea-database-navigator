package com.dci.intellij.dbn.language.common.element;

import java.util.Set;

import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.impl.ElementTypeRef;
import com.dci.intellij.dbn.language.common.element.lookup.ElementLookupContext;

public interface SequenceElementType extends ElementType {
    ElementTypeRef[] getChildren();

    public ElementTypeRef getFirstChild();

    ElementTypeRef getChild(int index);

    int getChildCount();

    boolean isExitIndex(int index);

    boolean containsLandmarkTokenFromIndex(TokenType tokenType, int index);

    Set<TokenType> getFirstPossibleTokensFromIndex(ElementLookupContext context, int index);

    boolean isPossibleTokenFromIndex(TokenType tokenType, int index);

    int indexOf(LeafElementType elementType);

    int indexOf(ElementType elementType, int fromIndex);

    int indexOf(ElementType elementType);
}
