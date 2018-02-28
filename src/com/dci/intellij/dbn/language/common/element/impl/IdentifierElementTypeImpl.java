package com.dci.intellij.dbn.language.common.element.impl;

import org.jdom.Element;

import com.dci.intellij.dbn.code.common.style.formatting.FormattingDefinition;
import com.dci.intellij.dbn.code.common.style.formatting.SpacingDefinition;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.ElementTypeBundle;
import com.dci.intellij.dbn.language.common.element.IdentifierElementType;
import com.dci.intellij.dbn.language.common.element.LeafElementType;
import com.dci.intellij.dbn.language.common.element.lookup.IdentifierElementTypeLookupCache;
import com.dci.intellij.dbn.language.common.element.parser.impl.IdentifierElementTypeParser;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeDefinition;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeDefinitionException;
import com.dci.intellij.dbn.language.common.element.util.IdentifierCategory;
import com.dci.intellij.dbn.language.common.element.util.IdentifierType;
import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;
import com.dci.intellij.dbn.language.common.resolve.UnderlyingObjectResolver;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;


public class IdentifierElementTypeImpl extends LeafElementTypeImpl implements IdentifierElementType {
    public static final FormattingDefinition FORMATTING = new FormattingDefinition(null, null, SpacingDefinition.ONE_SPACE, null);

    private IdentifierType identifierType;
    private IdentifierCategory identifierCategory;
    private DBObjectType objectType;
    private String underlyingObjectResolverId;
    private boolean referenceable; // is referenceable ()
    private boolean localReference; // is local reference


    public IdentifierElementTypeImpl(ElementTypeBundle bundle, ElementType parent, String id, Element def) throws ElementTypeDefinitionException {
        super(bundle, parent, id, def);
        setTokenType(bundle.getTokenTypeBundle().getIdentifier());
    }

    public IdentifierElementTypeLookupCache createLookupCache() {
        return new IdentifierElementTypeLookupCache(this);
    }

    public IdentifierElementTypeParser createParser() {
        return new IdentifierElementTypeParser(this);
    }

    protected void loadDefinition(Element def) throws ElementTypeDefinitionException {
        super.loadDefinition(def);
        String objectTypeName = ElementTypeBundle.determineMandatoryAttribute(def, "type", "Incomplete definition " + getId() + ". ");
        objectType = ElementTypeBundle.resolveObjectType(objectTypeName); 

        String type = def.getName();
        identifierType =
            ElementTypeDefinition.OBJECT_DEF.is(type) || ElementTypeDefinition.OBJECT_REF.is(type) ? IdentifierType.OBJECT :
            ElementTypeDefinition.ALIAS_DEF.is(type) || ElementTypeDefinition.ALIAS_REF.is(type) ? IdentifierType.ALIAS :
            ElementTypeDefinition.VARIABLE_DEF.is(type) || ElementTypeDefinition.VARIABLE_REF.is(type) ? IdentifierType.VARIABLE : IdentifierType.UNKNOWN;

        identifierCategory =
                    ElementTypeDefinition.OBJECT_REF.is(type) ||
                    ElementTypeDefinition.ALIAS_REF.is(type) ||
                    ElementTypeDefinition.VARIABLE_REF.is(type) ? IdentifierCategory.REFERENCE :
                    ElementTypeDefinition.OBJECT_DEF.is(type) ||
                    ElementTypeDefinition.ALIAS_DEF.is(type) ||
                    ElementTypeDefinition.VARIABLE_DEF.is(type) ? IdentifierCategory.DEFINITION : IdentifierCategory.UNKNOWN;

        referenceable = Boolean.parseBoolean(def.getAttributeValue("referenceable"));
        localReference = Boolean.parseBoolean(def.getAttributeValue("local"));

        underlyingObjectResolverId = def.getAttributeValue("underlying-object-resolver");

        if (isDefinition()) {
            setDefaultFormatting(FORMATTING);
        }

/*
        setDefaultFormatting();
        if (is(ElementTypeAttribute.STATEMENT)) {
            formatting = FormattingDefinitionFactory.mergeDefinitions(formatting, FormattingDefinition.STATEMENT_ATTRIBUTES_INDENTED);
        }
*/

    }

    public PsiElement createPsiElement(ASTNode astNode) {
        return new IdentifierPsiElement(astNode, this);
    }

    public String getDebugName() {
        String prefix =
                isObject() ? (isReference() ? "object-ref " : "object-def ") :
                isAlias() ? (isReference() ? "alias-ref " : "alias-def ") :
                isVariable() ? (isReference() ? "variable-ref " : "variable-def ") : "unknown";
        return prefix + getObjectTypeName() + " (" + getId() + ")";
    }

    public String toString() {
        return getObjectTypeName() + " (" + getId() + ")";
    }

    /*********************************************************
     *                Identifier type acessors               *
     *********************************************************/

    public IdentifierType getIdentifierType() {
        return identifierType;
    }

    public boolean isObject() {
        return identifierType == IdentifierType.OBJECT;
    }

    public boolean isAlias() {
        return identifierType == IdentifierType.ALIAS;
    }
    
    public boolean isVariable() {
        return identifierType == IdentifierType.VARIABLE;
    }

    public IdentifierCategory getIdentifierCategory() {
        return identifierCategory;
    }

    public boolean isReference() {
        return identifierCategory == IdentifierCategory.REFERENCE;
    }

    @Override
    public boolean isReferenceable() {
        return referenceable;
    }

    public boolean isLocalReference() {
        return localReference;
    }

    public boolean isDefinition() {
        return identifierCategory == IdentifierCategory.DEFINITION;
    }

    public DBObjectType getObjectType() {
        return objectType;
    }


    public String getObjectTypeName() {
        return objectType.getName();
    }

    public String getQualifiedObjectTypeName() {
        return getObjectTypeName() + " " + identifierType.name().toLowerCase();

    }

    public boolean isObjectOfType(DBObjectType type) {
        return objectType.matches(type);
    }

    @Override
    public boolean isSubject() {
        return is(ElementTypeAttribute.SUBJECT);
    }

    public boolean isSameAs(LeafElementType elementType) {
        if (elementType instanceof IdentifierElementType) {
            IdentifierElementType identifierElementType = (IdentifierElementType) elementType;
            return  identifierElementType.getObjectType().matches(objectType) &&
                    identifierElementType.getIdentifierType() == identifierType &&
                    identifierElementType.getIdentifierCategory() == identifierCategory;
        }
        return false;
    }

    public boolean isIdentifier() {
        return true;
    }

    @Override
    public UnderlyingObjectResolver getUnderlyingObjectResolver() {
        return underlyingObjectResolverId == null ? null : UnderlyingObjectResolver.get(underlyingObjectResolverId);
    }
}
