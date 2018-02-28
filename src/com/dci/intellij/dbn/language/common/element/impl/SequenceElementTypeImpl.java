package com.dci.intellij.dbn.language.common.element.impl;

import java.util.List;
import java.util.Set;
import org.jdom.Element;

import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.ElementTypeBundle;
import com.dci.intellij.dbn.language.common.element.LeafElementType;
import com.dci.intellij.dbn.language.common.element.SequenceElementType;
import com.dci.intellij.dbn.language.common.element.TokenElementType;
import com.dci.intellij.dbn.language.common.element.lookup.ElementLookupContext;
import com.dci.intellij.dbn.language.common.element.lookup.ElementTypeLookupCache;
import com.dci.intellij.dbn.language.common.element.lookup.SequenceElementTypeLookupCache;
import com.dci.intellij.dbn.language.common.element.parser.BranchCheck;
import com.dci.intellij.dbn.language.common.element.parser.impl.SequenceElementTypeParser;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeDefinitionException;
import com.dci.intellij.dbn.language.common.psi.SequencePsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import gnu.trove.THashSet;

public class SequenceElementTypeImpl extends AbstractElementType implements SequenceElementType {
    protected ElementTypeRef[] children;
    private int exitIndex;

    public ElementTypeRef[] getChildren() {
        return children;
    }

    public ElementTypeRef getFirstChild() {return children[0];}

    @Override
    public ElementTypeRef getChild(int index) {
        return children[index];
    }

    public SequenceElementTypeImpl(ElementTypeBundle bundle, ElementType parent, String id) {
        super(bundle, parent, id, (String) null);
    }

    public SequenceElementTypeImpl(ElementTypeBundle bundle, ElementType parent, String id, Element def) throws ElementTypeDefinitionException {
        super(bundle, parent, id, def);
    }

    @Override
    public SequenceElementTypeLookupCache createLookupCache() {
        return new SequenceElementTypeLookupCache<SequenceElementType>(this);
    }

    @Override
    public SequenceElementTypeParser createParser() {
        return new SequenceElementTypeParser<SequenceElementType>(this);
    }

    protected void loadDefinition(Element def) throws ElementTypeDefinitionException {
        super.loadDefinition(def);
        List children = def.getChildren();
        this.children = new ElementTypeRef[children.size()];

        ElementTypeRef previous = null;
        for (int i = 0; i < children.size(); i++) {
            Element child = (Element) children.get(i);
            String type = child.getName();
            ElementType elementType = getElementBundle().resolveElementDefinition(child, type, this);
            boolean optional = Boolean.parseBoolean(child.getAttributeValue("optional"));
            double version = Double.parseDouble(CommonUtil.nvl(child.getAttributeValue("version"), "0"));

            Set<BranchCheck> branchChecks = parseBranchChecks(child.getAttributeValue("branch-check"));
            this.children[i] = new ElementTypeRef(previous, this, elementType, optional, version, branchChecks);
            previous = this.children[i];

            if (child.getAttributeValue("exit") != null) exitIndex = i;
        }
    }

    public boolean isLeaf() {
        return false;
    }

    public int getChildCount() {
        return children.length;
    }

    public PsiElement createPsiElement(ASTNode astNode) {
        return new SequencePsiElement(astNode, this);
    }

    public boolean isExitIndex(int index) {
        return index <= exitIndex;
    }

    public String getDebugName() {
        return "sequence (" + getId() + ")";
    }

    /*********************************************************
     *                Cached lookup helpers                  *
     *********************************************************/
    public boolean containsLandmarkTokenFromIndex(TokenType tokenType, int index) {
        for (int i = index; i < children.length; i++) {
            if (children[i].getLookupCache().containsLandmarkToken(tokenType)) return true;
        }
        return false;
    }

    public Set<TokenType> getFirstPossibleTokensFromIndex(ElementLookupContext context, int index) {
        if (children[index].isOptional()) {
            Set<TokenType> tokenTypes = new THashSet<TokenType>();
            for (int i=index; i< children.length; i++) {
                ElementTypeLookupCache lookupCache = children[i].getLookupCache();
                lookupCache.collectFirstPossibleTokens(context.reset(), tokenTypes);
                if (!children[i].isOptional()) break;
            }
            return tokenTypes;
        } else {
            ElementTypeLookupCache lookupCache = children[index].getLookupCache();
            return lookupCache.collectFirstPossibleTokens(context.reset());
        }
    }

    public boolean isPossibleTokenFromIndex(TokenType tokenType, int index) {
        if (index < children.length) {
            for (int i= index; i< children.length; i++) {
                if (children[i].getLookupCache().couldStartWithToken(tokenType)){
                    return true;
                }
                if (!children[i].isOptional()) {
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public int indexOf(LeafElementType leafElementType) {
        WrappingDefinition wrapping = getWrapping();
        if (wrapping != null && leafElementType instanceof TokenElementType) {
            TokenElementType tokenElementType = (TokenElementType) leafElementType;
            if (wrapping.getEndElementType().getTokenType() == tokenElementType.getTokenType()) {
                return children.length-1;
            }
        }
        ElementTypeRef child = children[0];
        while (child != null) {
            ElementType childElementType = child.getElementType();
            if (childElementType == leafElementType || childElementType.getLookupCache().containsLeaf(leafElementType)) {
                return child.getIndex();
            }
            child = child.getNext();
        }

        return -1;
    }

    public int indexOf(ElementType elementType, int fromIndex) {
        WrappingDefinition wrapping = getWrapping();
        if (wrapping != null && elementType instanceof TokenElementType) {
            TokenElementType tokenElementType = (TokenElementType) elementType;
            if (wrapping.getEndElementType().getTokenType() == tokenElementType.getTokenType()) {
                return children.length-1;
            }
        }

        if (fromIndex < children.length) {
            ElementTypeRef child = children[fromIndex];
            while (child != null) {
                if (child.getElementType() == elementType) {
                    return child.getIndex();
                }
                child = child.getNext();
            }
        }
        return -1;
    }

    public int indexOf(ElementType elementType) {
        return indexOf(elementType, 0);
    }
}
