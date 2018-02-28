package com.dci.intellij.dbn.language.common.element.lookup;

import java.util.Set;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.LeafElementType;

public abstract class LeafElementTypeLookupCache<T extends LeafElementType> extends AbstractElementTypeLookupCache<T> {
    public LeafElementTypeLookupCache(T leafElementType) {
        super(leafElementType);
        T elementType = getElementType();
        allPossibleLeafs.add(elementType);
        firstPossibleLeafs.add(elementType);
        firstRequiredLeafs.add(elementType);
    }

    @Override
    @Deprecated
    public boolean couldStartWithLeaf(LeafElementType leafElementType) {
        return getElementType() == leafElementType;
    }

    @Override
    public boolean couldStartWithToken(TokenType tokenType) {
        return getElementType().getTokenType() == tokenType;
    }

    @Override
    public Set<LeafElementType> collectFirstPossibleLeafs(ElementLookupContext context, @Nullable Set<LeafElementType> bucket) {
        bucket = initBucket(bucket);
        bucket.add(getElementType());
        return bucket;
    }

    @Override
    public Set<TokenType> collectFirstPossibleTokens(ElementLookupContext context, @Nullable Set<TokenType> bucket) {
        bucket = initBucket(bucket);
        bucket.add(getElementType().getTokenType());
        return bucket;
    }
}