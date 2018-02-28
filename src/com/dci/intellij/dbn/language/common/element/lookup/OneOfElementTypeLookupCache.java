package com.dci.intellij.dbn.language.common.element.lookup;

import java.util.Set;

import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.LeafElementType;
import com.dci.intellij.dbn.language.common.element.OneOfElementType;
import com.dci.intellij.dbn.language.common.element.impl.ElementTypeRef;
import com.dci.intellij.dbn.language.common.element.path.PathNode;

public class OneOfElementTypeLookupCache extends AbstractElementTypeLookupCache<OneOfElementType> {
    public OneOfElementTypeLookupCache(OneOfElementType elementType) {
        super(elementType);
    }

    @Override
    boolean initAsFirstPossibleLeaf(LeafElementType leaf, ElementType source) {
        boolean notInitialized = !firstPossibleLeafs.contains(leaf);
        return notInitialized && (isWrapperBeginLeaf(leaf) || source.getLookupCache().couldStartWithLeaf(leaf));
    }

    @Override
    boolean initAsFirstRequiredLeaf(LeafElementType leaf, ElementType source) {
        boolean notInitialized = !firstRequiredLeafs.contains(leaf);
        return notInitialized && source.getLookupCache().shouldStartWithLeaf(leaf);
    }

    public boolean containsLandmarkToken(TokenType tokenType, PathNode node) {
        for (ElementTypeRef child : getElementType().getChildren()) {
            if (child.getLookupCache().containsLandmarkToken(tokenType, node)) return true;
        }
        return false;
    }

    public boolean startsWithIdentifier(PathNode node) {
        for(ElementTypeRef child : getElementType().getChildren()){
            if (child.getLookupCache().startsWithIdentifier(node)) return true;
        }
        return false;
    }

    @Override
    public Set<LeafElementType> collectFirstPossibleLeafs(ElementLookupContext context, Set<LeafElementType> bucket) {
        ElementTypeRef[] elementTypeRefs = getElementType().getChildren();
        for (ElementTypeRef child : elementTypeRefs) {
            if (context.check(child)) {
                ElementTypeLookupCache lookupCache = child.getElementType().getLookupCache();
                bucket = lookupCache.collectFirstPossibleLeafs(context, bucket);
            }
        }
        return bucket;
    }

    public Set<TokenType> collectFirstPossibleTokens(ElementLookupContext context, Set<TokenType> bucket) {
        ElementTypeRef[] elementTypeRefs = getElementType().getChildren();
        for (ElementTypeRef child : elementTypeRefs) {
            if (context.check(child)) {
                ElementTypeLookupCache lookupCache = child.getElementType().getLookupCache();
                bucket = lookupCache.collectFirstPossibleTokens(context, bucket);
            }
        }
        return bucket;
    }
}