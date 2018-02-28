package com.dci.intellij.dbn.language.common.element.lookup;

import java.util.Set;

import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.LeafElementType;
import com.dci.intellij.dbn.language.common.element.SequenceElementType;
import com.dci.intellij.dbn.language.common.element.impl.ElementTypeRef;
import com.dci.intellij.dbn.language.common.element.path.PathNode;

public class SequenceElementTypeLookupCache<T extends SequenceElementType> extends AbstractElementTypeLookupCache<T> {

    public SequenceElementTypeLookupCache(T elementType) {
        super(elementType);
    }

    @Override
    boolean initAsFirstPossibleLeaf(LeafElementType leaf, ElementType source) {
        boolean notInitialized = !firstPossibleLeafs.contains(leaf);
        return notInitialized && (
                isWrapperBeginLeaf(leaf) ||
                    (couldStartWithElement(source) &&
                     source.getLookupCache().couldStartWithLeaf(leaf)));
    }

    @Override
    boolean initAsFirstRequiredLeaf(LeafElementType leaf, ElementType source) {
        boolean notInitialized = !firstRequiredLeafs.contains(leaf);
        return notInitialized &&
                shouldStartWithElement(source) &&
                source.getLookupCache().shouldStartWithLeaf(leaf);
    }

    private boolean couldStartWithElement(ElementType elementType) {
        ElementTypeRef[] children = getElementType().getChildren();
        for (ElementTypeRef child : children) {
            if (child.isOptional()) {
                if (elementType == child.getElementType()) return true;
            } else {
                return child.getElementType() == elementType;
            }
        }
        return false;
    }

    private boolean shouldStartWithElement(ElementType elementType) {
        ElementTypeRef[] children = getElementType().getChildren();
        for (ElementTypeRef child : children) {
            if (!child.isOptional()) {
                return child.getElementType() == elementType;
            }
        }
        return false;
    }


    public boolean containsLandmarkToken(TokenType tokenType, PathNode node) {
        //check only first landmarks within first mandatory element
        ElementTypeRef[] children = getElementType().getChildren();
        for (ElementTypeRef child : children) {
            if (child.getLookupCache().containsLandmarkToken(tokenType, node)) return true;
            if (!child.isOptional()) return false;  // skip if found non optional element
        }
        return false;
    }

    public boolean startsWithIdentifier(PathNode node) {
        ElementTypeRef[] children = getElementType().getChildren();
        for (ElementTypeRef child : children) {
            if (child.getLookupCache().startsWithIdentifier(node)) {
                return true;
            }

            if (!child.isOptional()) {
                return false;
            }
        }
        return false;
    }

    @Override
    public Set<LeafElementType> collectFirstPossibleLeafs(ElementLookupContext context, Set<LeafElementType> bucket) {
        bucket = initBucket(bucket);

        T elementType = getElementType();
        ElementTypeRef[] children = elementType.getChildren();
        for (ElementTypeRef child : children) {
            if (context.check(child)) {
                ElementTypeLookupCache lookupCache = child.getElementType().getLookupCache();
                lookupCache.collectFirstPossibleLeafs(context, bucket);
            }
            if (!child.isOptional()) break;
        }
        return bucket;
    }

    @Override
    public Set<TokenType> collectFirstPossibleTokens(ElementLookupContext context, Set<TokenType> bucket) {
        bucket = initBucket(bucket);

        T elementType = getElementType();
        ElementTypeRef[] children = elementType.getChildren();
        for (ElementTypeRef child : children) {
            if (context.check(child)) {
                ElementTypeLookupCache lookupCache = child.getElementType().getLookupCache();
                lookupCache.collectFirstPossibleTokens(context, bucket);
            }
            if (!child.isOptional()) break;
        }
        return bucket;
    }
}

