package com.dci.intellij.dbn.language.common.element.impl;

import java.util.List;
import org.jdom.Element;

import com.dci.intellij.dbn.code.common.style.formatting.FormattingDefinition;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.ElementTypeBundle;
import com.dci.intellij.dbn.language.common.element.SequenceElementType;
import com.dci.intellij.dbn.language.common.element.TokenElementType;
import com.dci.intellij.dbn.language.common.element.TokenPairTemplate;
import com.dci.intellij.dbn.language.common.element.WrapperElementType;
import com.dci.intellij.dbn.language.common.element.lookup.WrapperElementTypeLookupCache;
import com.dci.intellij.dbn.language.common.element.parser.impl.WrapperElementTypeParser;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeDefinitionException;
import com.dci.intellij.dbn.language.common.psi.SequencePsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;

public class WrapperElementTypeImpl extends AbstractElementType implements WrapperElementType {
    private WrappingDefinition wrappingDefinition;
    private ElementType wrappedElement;
    private boolean wrappedElementOptional;

    public WrapperElementTypeImpl(ElementTypeBundle bundle, ElementType parent, String id, Element def) throws ElementTypeDefinitionException {
        super(bundle, parent, id, def);
    }

    @Override
    protected void loadDefinition(Element def) throws ElementTypeDefinitionException {
        super.loadDefinition(def);
        ElementTypeBundle bundle = getElementBundle();
        String templateId = def.getAttributeValue("template");

        TokenElementType beginTokenElement;
        TokenElementType endTokenElement;
        if (StringUtil.isEmpty(templateId)) {
            String startTokenId = def.getAttributeValue("begin-token");
            String endTokenId = def.getAttributeValue("end-token");

            beginTokenElement = new TokenElementTypeImpl(bundle, this, startTokenId, "begin-token");
            endTokenElement = new TokenElementTypeImpl(bundle, this, endTokenId, "end-token");
        } else {
            TokenPairTemplate template = TokenPairTemplate.valueOf(templateId);
            beginTokenElement = new TokenElementTypeImpl(bundle, this, template.getBeginToken(), "begin-token");
            endTokenElement = new TokenElementTypeImpl(bundle, this, template.getEndToken(), "end-token");

            if (template.isBlock()) {
                beginTokenElement.setDefaultFormatting(FormattingDefinition.LINE_BREAK_AFTER);
                endTokenElement.setDefaultFormatting(FormattingDefinition.LINE_BREAK_BEFORE);
                setDefaultFormatting(FormattingDefinition.LINE_BREAK_BEFORE);
            }
        }

        wrappingDefinition = new WrappingDefinition(beginTokenElement, endTokenElement);


        List children = def.getChildren();
        if (children.size() != 1) {
            throw new ElementTypeDefinitionException(
                    "Invalid wrapper definition. " +
                    "Element should contain exact one child of type 'one-of', 'sequence', 'element', 'token'");
        }
        Element child = (Element) children.get(0);
        String type = child.getName();
        wrappedElement = bundle.resolveElementDefinition(child, type, this);
        wrappedElementOptional = Boolean.parseBoolean(child.getAttributeValue("optional"));

        //getLookupCache().registerFirstLeaf(beginTokenElement, isOptional);
    }

    @Override
    public WrapperElementTypeLookupCache createLookupCache() {
        return new WrapperElementTypeLookupCache(this);
    }

    @Override
    public WrapperElementTypeParser createParser() {
        return new WrapperElementTypeParser(this);
    }

    public boolean isLeaf() {
        return false;
    }

    public TokenElementType getBeginTokenElement() {
        return wrappingDefinition.getBeginElementType();
    }

    public TokenElementType getEndTokenElement() {
        return wrappingDefinition.getEndElementType();
    }

    public ElementType getWrappedElement() {
        return wrappedElement;
    }

    @Override
    public boolean isWrappedElementOptional() {
        return wrappedElementOptional;
    }

    @Override
    public boolean isStrong() {
        if (getBeginTokenElement().getTokenType().isReservedWord()) {
            return true;
        }
        if (getParent() instanceof SequenceElementType) {
            SequenceElementType sequenceElementType = (SequenceElementType) getParent();
            int index = sequenceElementType.indexOf(this);

            ElementTypeRef child = sequenceElementType.getChild(index);
            if (child.isOptional()) {
                return false;
            }

            return index > 0 && !child.isOptionalToHere();
        }
        return false;
    }

    public String getDebugName() {
        return "wrapper (" + getId() + ")";
    }
    public PsiElement createPsiElement(ASTNode astNode) {
        return new SequencePsiElement(astNode, this);
    }
}
