package com.dci.intellij.dbn.language.common.element.lookup;

import java.util.Set;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.LeafElementType;
import com.dci.intellij.dbn.language.common.element.QualifiedIdentifierElementType;
import com.dci.intellij.dbn.language.common.element.path.PathNode;

public class QualifiedIdentifierElementTypeLookupCache extends AbstractElementTypeLookupCache<QualifiedIdentifierElementType> {
    public QualifiedIdentifierElementTypeLookupCache(QualifiedIdentifierElementType elementType) {
        super(elementType);
    }

    @Override
    boolean initAsFirstPossibleLeaf(LeafElementType leaf, ElementType source) {
        for (LeafElementType[] variant : getElementType().getVariants()) {
            if (variant[0] == source) return true;
        }
        return false;
    }

    @Override
    boolean initAsFirstRequiredLeaf(LeafElementType leaf, ElementType source) {
        for (LeafElementType[] variant : getElementType().getVariants()) {
            if (variant[0] == source && !variant[0].isOptional()) return true;
        }
        return false;
    }

    public boolean containsLandmarkToken(TokenType tokenType, PathNode node) {
        for (LeafElementType[] elementTypes : getElementType().getVariants()) {
            for (LeafElementType elementType : elementTypes) {
                if (elementType.getLookupCache().containsLandmarkToken(tokenType, node)) return true;
            }
        }
        return false;
    }

    public boolean startsWithIdentifier(PathNode node) {
        for (LeafElementType[] elementTypes : getElementType().getVariants()) {
            if (elementTypes[0].getLookupCache().startsWithIdentifier(node)) return true;
        }
        return false;
    }

    @Override
    public Set<LeafElementType> collectFirstPossibleLeafs(ElementLookupContext context, @Nullable Set<LeafElementType> bucket) {
        bucket = initBucket(bucket);
        for (LeafElementType[] elementTypes : getElementType().getVariants()) {
            // variants already consider optional leafs
            bucket.add(elementTypes[0]);
        }

        return bucket;
    }

    @Override
    public Set<TokenType> collectFirstPossibleTokens(ElementLookupContext context, @Nullable Set<TokenType> bucket) {
        bucket = initBucket(bucket);
        for (LeafElementType[] elementTypes : getElementType().getVariants()) {
            // variants already consider optional leafs
            bucket.add(elementTypes[0].getTokenType());
        }

        return bucket;
    }
}