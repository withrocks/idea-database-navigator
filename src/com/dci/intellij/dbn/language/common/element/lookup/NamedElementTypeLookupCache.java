package com.dci.intellij.dbn.language.common.element.lookup;

import java.util.Set;

import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.LeafElementType;
import com.dci.intellij.dbn.language.common.element.NamedElementType;
import com.dci.intellij.dbn.language.common.element.path.BasicPathNode;
import com.dci.intellij.dbn.language.common.element.path.PathNode;

public class NamedElementTypeLookupCache extends SequenceElementTypeLookupCache<NamedElementType>{

    public NamedElementTypeLookupCache(NamedElementType elementType) {
        super(elementType);
    }

    protected void registerLeafInParent(LeafElementType leaf) {
        // walk the tree up for all potential parents
        NamedElementType elementType = getElementType();
        Set<ElementType> parents = elementType.getParents();
        if (parents != null) {
            for (ElementType parentElementType: parents) {
                parentElementType.getLookupCache().registerLeaf(leaf, elementType);
            }
        }
    }

    public boolean containsLandmarkToken(TokenType tokenType, PathNode node) {
        return !isRecursive(node) &&
                super.containsLandmarkToken(tokenType, createRecursionCheckPathNode(node));
    }

    public boolean startsWithIdentifier(PathNode node) {
        return !isRecursive(node) &&
                super.startsWithIdentifier(createRecursionCheckPathNode(node));
    }

    private static boolean isRecursive(PathNode node) {
        return node != null && node.isRecursive();
    }

    private PathNode createRecursionCheckPathNode(PathNode parentPathNode) {
        return new BasicPathNode(getElementType(), parentPathNode);
    }

    @Override
    public Set<LeafElementType> collectFirstPossibleLeafs(ElementLookupContext context, Set<LeafElementType> bucket) {
        NamedElementType elementType = getElementType();
        if (!context.isScanned(elementType)) {
            context.markScanned(elementType);
            return super.collectFirstPossibleLeafs(context, bucket);
        }
        return bucket;
    }

    @Override
    public Set<TokenType> collectFirstPossibleTokens(ElementLookupContext context, Set<TokenType> bucket) {
        NamedElementType elementType = getElementType();
        if (!context.isScanned(elementType)) {
            context.markScanned(elementType);
            return super.collectFirstPossibleTokens(context, bucket);
        }
        return bucket;
    }
}
