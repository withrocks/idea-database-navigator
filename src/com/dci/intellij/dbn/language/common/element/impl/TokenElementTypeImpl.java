package com.dci.intellij.dbn.language.common.element.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.jdom.Element;

import com.dci.intellij.dbn.code.common.lookup.LookupItemBuilderProvider;
import com.dci.intellij.dbn.code.common.lookup.TokenLookupItemBuilder;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.TokenTypeCategory;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.ElementTypeBundle;
import com.dci.intellij.dbn.language.common.element.IterationElementType;
import com.dci.intellij.dbn.language.common.element.LeafElementType;
import com.dci.intellij.dbn.language.common.element.QualifiedIdentifierElementType;
import com.dci.intellij.dbn.language.common.element.TokenElementType;
import com.dci.intellij.dbn.language.common.element.TokenElementTypeChain;
import com.dci.intellij.dbn.language.common.element.WrapperElementType;
import com.dci.intellij.dbn.language.common.element.lookup.ElementLookupContext;
import com.dci.intellij.dbn.language.common.element.lookup.ElementTypeLookupCache;
import com.dci.intellij.dbn.language.common.element.lookup.TokenElementTypeLookupCache;
import com.dci.intellij.dbn.language.common.element.parser.ParserContext;
import com.dci.intellij.dbn.language.common.element.parser.impl.TokenElementTypeParser;
import com.dci.intellij.dbn.language.common.element.path.BasicPathNode;
import com.dci.intellij.dbn.language.common.element.path.PathNode;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeDefinitionException;
import com.dci.intellij.dbn.language.common.psi.TokenPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;

public class TokenElementTypeImpl extends LeafElementTypeImpl implements LookupItemBuilderProvider, TokenElementType {
    private TokenLookupItemBuilder lookupItemBuilder = new TokenLookupItemBuilder(this);
    private TokenTypeCategory flavor;
    private List<TokenElementTypeChain> possibleTokenChains;

    public TokenElementTypeImpl(ElementTypeBundle bundle, ElementType parent, String id, Element def) throws ElementTypeDefinitionException {
        super(bundle, parent, id, def);
        String typeId = def.getAttributeValue("type-id");
        TokenType tokenType = bundle.getTokenTypeBundle().getTokenType(typeId);
        setTokenType(tokenType);
        setDefaultFormatting(tokenType.getFormatting());

        String flavorName = def.getAttributeValue("flavor");
        if (StringUtil.isNotEmpty(flavorName)) {
            flavor = TokenTypeCategory.getCategory(flavorName);
        }

        setDescription(tokenType.getValue() + " " + getTokenTypeCategory());
    }

    public TokenElementTypeImpl(ElementTypeBundle bundle, ElementType parent, String typeId, String id) throws ElementTypeDefinitionException {
        super(bundle, parent, id, (String)null);
        TokenType tokenType = bundle.getTokenTypeBundle().getTokenType(typeId);
        setTokenType(tokenType);
        setDescription(tokenType.getValue() + " " + getTokenTypeCategory());

        setDefaultFormatting(tokenType.getFormatting());
    }

    public TokenElementTypeLookupCache createLookupCache() {
        return new TokenElementTypeLookupCache(this);
    }

    public TokenElementTypeParser createParser() {
        return new TokenElementTypeParser(this);
    }

    public String getDebugName() {
        return "token (" + getId() + " - " + getTokenType().getId() + ")";
    }

    public Set<LeafElementType> getNextPossibleLeafs(PathNode pathNode, ElementLookupContext context) {
        ElementType parent = getParent();
        if (isIterationSeparator()) {
            if (parent instanceof IterationElementType) {
                IterationElementType iterationElementType = (IterationElementType) parent;
                ElementTypeLookupCache lookupCache = iterationElementType.getIteratedElementType().getLookupCache();
                return lookupCache.collectFirstPossibleLeafs(context.reset());
            } else if (parent instanceof QualifiedIdentifierElementType){
                return super.getNextPossibleLeafs(pathNode, context);
            }
        }
        if (parent instanceof WrapperElementType) {
            WrapperElementType wrapperElementType = (WrapperElementType) parent;
            if (this.equals(wrapperElementType.getBeginTokenElement())) {
                ElementTypeLookupCache lookupCache = wrapperElementType.getWrappedElement().getLookupCache();
                return lookupCache.collectFirstPossibleLeafs(context.reset());
            }
        }

        return super.getNextPossibleLeafs(pathNode, context);
    }

    public Set<LeafElementType> getNextRequiredLeafs(PathNode pathNode, ParserContext context) {
        if (isIterationSeparator()) {
            if (getParent() instanceof IterationElementType) {
                IterationElementType iterationElementType = (IterationElementType) getParent();
                return iterationElementType.getIteratedElementType().getLookupCache().getFirstRequiredLeafs();
            } else if (getParent() instanceof QualifiedIdentifierElementType){
                return super.getNextRequiredLeafs(pathNode, context);
            }
        }
        return super.getNextRequiredLeafs(pathNode, context);
    }

    public boolean isIterationSeparator() {
        return getId().equals(SEPARATOR);
    }

    public boolean isLeaf() {
        return true;
    }

    public boolean isIdentifier() {
        return false;
    }

    public boolean isSameAs(LeafElementType elementType) {
        if (elementType instanceof TokenElementType) {
            TokenElementType token = (TokenElementType) elementType;
            return token.getTokenType() == getTokenType();
        }
        return false;
    }


    public PsiElement createPsiElement(ASTNode astNode) {
        return new TokenPsiElement(astNode, this);
    }

    public String toString() {
        return getTokenType().getId() + " (" + getId() + ")";
    }

    public boolean isCharacter() {
        return getTokenType().isCharacter();
    }

    public TokenLookupItemBuilder getLookupItemBuilder(DBLanguage language) {
        return lookupItemBuilder;
    }

    @Override
    public TokenTypeCategory getFlavor() {
        return flavor;
    }

    @Override
    public TokenTypeCategory getTokenTypeCategory() {
        return flavor == null ? getTokenType().getCategory() : flavor;
    }

    @Override
    public List<TokenElementTypeChain> getPossibleTokenChains() {
        if (possibleTokenChains == null) {
            possibleTokenChains = new ArrayList<TokenElementTypeChain>();
            TokenElementTypeChain stump = new TokenElementTypeChain(this);
            buildPossibleChains(this, stump);
            System.out.printf("");
        }
        return possibleTokenChains;
    }

    private void buildPossibleChains(TokenElementType tokenElementType, TokenElementTypeChain stump) {
        PathNode pathNode = BasicPathNode.buildPathUp(tokenElementType);
        Set<LeafElementType> nextPossibleLeafs = getNextPossibleLeafs(pathNode, new ElementLookupContext());
        if (nextPossibleLeafs != null) {
            for (LeafElementType nextPossibleLeaf : nextPossibleLeafs) {
                if (nextPossibleLeaf instanceof TokenElementType) {
                    TokenElementType nextTokenElementType = (TokenElementType) nextPossibleLeaf;
                    if (nextTokenElementType.getTokenType().isKeyword()) {
                        TokenElementTypeChain tokenElementTypeChain = stump.createVariant(nextTokenElementType);
                        if (possibleTokenChains == null) possibleTokenChains = new ArrayList<TokenElementTypeChain>();
                        possibleTokenChains.add(tokenElementTypeChain);
                        if (tokenElementTypeChain.getElementTypes().size()<3) {
                            buildPossibleChains(nextTokenElementType, tokenElementTypeChain);
                        }
                    }
                }
            }
        }
    }
}
