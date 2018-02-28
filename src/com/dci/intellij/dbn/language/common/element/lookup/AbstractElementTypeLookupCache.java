package com.dci.intellij.dbn.language.common.element.lookup;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.dci.intellij.dbn.language.common.SharedTokenTypeBundle;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.ElementTypeBundle;
import com.dci.intellij.dbn.language.common.element.IdentifierElementType;
import com.dci.intellij.dbn.language.common.element.IterationElementType;
import com.dci.intellij.dbn.language.common.element.LeafElementType;
import com.dci.intellij.dbn.language.common.element.SequenceElementType;
import com.dci.intellij.dbn.language.common.element.TokenElementType;
import com.dci.intellij.dbn.language.common.element.impl.ElementTypeRef;
import com.dci.intellij.dbn.language.common.element.impl.WrappingDefinition;
import gnu.trove.THashMap;
import gnu.trove.THashSet;

public abstract class AbstractElementTypeLookupCache<T extends ElementType> implements ElementTypeLookupCache<T> {
    private T elementType;

    protected Set<LeafElementType> allPossibleLeafs = new THashSet<LeafElementType>();
    protected Set<LeafElementType> firstPossibleLeafs = new THashSet<LeafElementType>();
    protected Set<LeafElementType> firstRequiredLeafs = new THashSet<LeafElementType>();
    protected Set<TokenType> allPossibleTokens = new THashSet<TokenType>();
    protected Set<TokenType> firstPossibleTokens = new THashSet<TokenType>();
    protected Set<TokenType> firstRequiredTokens = new THashSet<TokenType>();
    private Map<TokenType, Boolean> landmarkTokens;
    private Boolean startsWithIdentifier;

    private Set<TokenType> nextPossibleTokens;

    public AbstractElementTypeLookupCache(T elementType) {
        this.elementType = elementType;
        if (!elementType.isLeaf()) {
            landmarkTokens = new THashMap<TokenType, Boolean>();
        }
    }

    public void init() {}

    public T getElementType() {
        return elementType;
    }

    private ElementTypeBundle getBundle() {
        return elementType.getElementBundle();
    }


    public boolean containsToken(TokenType tokenType) {
        return allPossibleTokens != null && allPossibleTokens.contains(tokenType);
    }

    @Override
    public boolean containsLeaf(LeafElementType elementType) {
        return allPossibleLeafs != null && allPossibleLeafs.contains(elementType);
    }

    @Override
    public Set<LeafElementType> collectFirstPossibleLeafs(ElementLookupContext context) {
        return collectFirstPossibleLeafs(context.reset(), null);
    }

    @Override
    public Set<TokenType> collectFirstPossibleTokens(ElementLookupContext context) {
        return collectFirstPossibleTokens(context.reset(), null);
    }

    protected <E> Set<E> initBucket(Set<E> bucket) {
        if (bucket == null) bucket = new HashSet<E>();
        return bucket;
    }

    @Override
    public Set<TokenType> getFirstRequiredTokens() {
        return firstRequiredTokens;
    }

    public Set<TokenType> getFirstPossibleTokens() {
        return firstPossibleTokens;
    }

    public Set<LeafElementType> getFirstRequiredLeafs() {
        return firstRequiredLeafs;
    }
    @Override
    public Set<LeafElementType> getFirstPossibleLeafs() {
        return firstPossibleLeafs;
    }



    public boolean couldStartWithLeaf(LeafElementType leafElementType) {
        return firstPossibleLeafs.contains(leafElementType);
    }

    public boolean couldStartWithToken(TokenType tokenType) {
        return firstPossibleTokens.contains(tokenType);
    }

    public boolean shouldStartWithLeaf(LeafElementType leafElementType) {
        return firstRequiredLeafs.contains(leafElementType);
    }

    public void registerLeaf(LeafElementType leaf, ElementType source) {
        boolean initAllElements = initAllElements(leaf);
        boolean initAsFirstPossibleLeaf = initAsFirstPossibleLeaf(leaf, source);
        boolean initAsFirstRequiredLeaf = initAsFirstRequiredLeaf(leaf, source);

        // register first possible leafs
        ElementTypeLookupCache lookupCache = leaf.getLookupCache();
        if (initAsFirstPossibleLeaf) {
            firstPossibleLeafs.add(leaf);
            firstPossibleTokens.addAll(lookupCache.getFirstPossibleTokens());
        }

        // register first required leafs
        if (initAsFirstRequiredLeaf) {
            firstRequiredLeafs.add(leaf);
            firstRequiredTokens.addAll(lookupCache.getFirstPossibleTokens());
        }

        if (initAllElements) {
            // register all possible leafs
            allPossibleLeafs.add(leaf);

            // register all possible tokens
            if (leaf instanceof IdentifierElementType) {
                SharedTokenTypeBundle sharedTokenTypes = elementType.getLanguage().getSharedTokenTypes();
                allPossibleTokens.add(sharedTokenTypes.getIdentifier());
                allPossibleTokens.add(sharedTokenTypes.getQuotedIdentifier());
            } else {
                allPossibleTokens.add(leaf.getTokenType());
            }
        }

        if (initAsFirstPossibleLeaf || initAsFirstRequiredLeaf || initAllElements) {
            // walk the tree up
            registerLeafInParent(leaf);
        }
    }

    abstract boolean initAsFirstPossibleLeaf(LeafElementType leaf, ElementType source);
    abstract boolean initAsFirstRequiredLeaf(LeafElementType leaf, ElementType source);
    private boolean initAllElements(LeafElementType leafElementType) {
        return leafElementType != elementType && !allPossibleLeafs.contains(leafElementType);
    }

    protected void registerLeafInParent(LeafElementType leaf) {
        ElementType parent = elementType.getParent();
        if (parent != null) {
            parent.getLookupCache().registerLeaf(leaf, elementType);
        }
    }

    public synchronized boolean containsLandmarkToken(TokenType tokenType) {
        if (elementType.isLeaf()) return containsToken(tokenType);

        Boolean value = landmarkTokens.get(tokenType);
        if (value == null) {
            value = containsLandmarkToken(tokenType, null);
            landmarkTokens.put(tokenType, value);
        }
        return value;
    }


    public synchronized boolean startsWithIdentifier() {
        if (startsWithIdentifier == null) {
            startsWithIdentifier =  startsWithIdentifier(null);
        }
        return startsWithIdentifier;
    }

    public boolean containsIdentifiers() {
        return containsToken(getBundle().getTokenTypeBundle().getIdentifier());
    }

    /**
     * This method returns all possible tokens (optional or not) which may follow current element.
     *
     * NOTE: to be used only for limited scope, since the tree walk-up
     * is done only until first named element is hit.
     * (named elements do not have parents)
     */
    public synchronized Set<TokenType> getNextPossibleTokens() {
        if (nextPossibleTokens == null) {
            nextPossibleTokens = new THashSet<TokenType>();
            ElementType elementType = this.elementType;
            ElementType parentElementType = elementType.getParent();
            while (parentElementType != null) {
                if (parentElementType instanceof SequenceElementType) {
                    SequenceElementType sequenceElementType = (SequenceElementType) parentElementType;
                    int elementsCount = sequenceElementType.getChildCount();
                    int index = sequenceElementType.indexOf(elementType, 0) + 1;

                    if (index < elementsCount) {
                        ElementTypeRef child = sequenceElementType.getChild(index);
                        while (child != null) {
                            nextPossibleTokens.addAll(child.getLookupCache().getFirstPossibleTokens());
                            if (!child.isOptional()) {
                                parentElementType = null;
                                break;
                            }
                            child = child.getNext();
                        }
                    }
                } else if (parentElementType instanceof IterationElementType) {
                    IterationElementType iteration = (IterationElementType) parentElementType;
                    TokenElementType[] separatorTokens = iteration.getSeparatorTokens();
                    if (separatorTokens != null) {
                        for (TokenElementType separatorToken : separatorTokens) {
                            nextPossibleTokens.add(separatorToken.getTokenType());
                        }
                    }
                }
                if (parentElementType != null) {
                    elementType = parentElementType;
                    parentElementType = elementType.getParent();
                }
            }
        }
        return nextPossibleTokens;
    }

    protected boolean isWrapperBeginLeaf(LeafElementType leaf) {
        WrappingDefinition wrapping = elementType.getWrapping();
        if (wrapping != null) {
            if (wrapping.getBeginElementType() == leaf) {
                return true;
            }
        }
        return false;
    }
}
