package com.dci.intellij.dbn.language.common.psi.lookup;

import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.language.common.element.IdentifierElementType;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.element.util.IdentifierCategory;
import com.dci.intellij.dbn.language.common.element.util.IdentifierType;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;
import com.dci.intellij.dbn.language.common.psi.LeafPsiElement;
import com.dci.intellij.dbn.object.common.DBObjectType;

public class IdentifierLookupAdapter extends PsiLookupAdapter {
    private IdentifierType identifierType;
    private IdentifierCategory identifierCategory;
    private DBObjectType objectType;
    private CharSequence identifierName;
    private ElementTypeAttribute attribute;
    private LeafPsiElement lookupIssuer;

    public IdentifierLookupAdapter(LeafPsiElement lookupIssuer, @Nullable IdentifierType identifierType, @Nullable IdentifierCategory identifierCategory, @Nullable DBObjectType objectType, CharSequence identifierName) {
        this(lookupIssuer, identifierType, identifierCategory, objectType, identifierName, null);
    }

    public IdentifierLookupAdapter(LeafPsiElement lookupIssuer, @Nullable IdentifierType identifierType, @Nullable IdentifierCategory identifierCategory, @Nullable DBObjectType objectType, CharSequence identifierName, ElementTypeAttribute attribute) {
        this.lookupIssuer = lookupIssuer;
        this.identifierType = identifierType;
        this.objectType = objectType;
        this.identifierName = identifierName;
        this.identifierCategory = identifierCategory;
        this.attribute = attribute;
    }

    public boolean matches(BasePsiElement basePsiElement) {
        if (basePsiElement instanceof IdentifierPsiElement && basePsiElement != lookupIssuer) {
            IdentifierPsiElement identifierPsiElement = (IdentifierPsiElement) basePsiElement;
            return
                matchesType(identifierPsiElement) &&
                matchesObjectType(identifierPsiElement) &&
                matchesCategory(identifierPsiElement) &&
                matchesAttribute(identifierPsiElement) &&
                matchesName(identifierPsiElement);
        }
        return false;
    }

    private boolean matchesType(IdentifierPsiElement identifierPsiElement) {
        return identifierType == null ||identifierType == identifierPsiElement.getElementType().getIdentifierType();
    }

    public boolean matchesObjectType(IdentifierPsiElement identifierPsiElement) {
        return objectType == null || identifierPsiElement.isObjectOfType(objectType);
    }

    public boolean matchesName(IdentifierPsiElement identifierPsiElement) {
        return identifierName == null || identifierPsiElement.textMatches(identifierName);

    }

    private boolean matchesCategory(IdentifierPsiElement identifierPsiElement) {
        if (identifierCategory == null) return true;
        IdentifierElementType elementType = identifierPsiElement.getElementType();
        IdentifierCategory category = elementType.getIdentifierCategory();
        switch (identifierCategory) {
            case ALL: return true;
            case DEFINITION: return category == IdentifierCategory.DEFINITION || identifierPsiElement.isReferenceable();
            case REFERENCE: return category == IdentifierCategory.REFERENCE;
        }
        return false;
    }

    private boolean matchesAttribute(IdentifierPsiElement identifierPsiElement) {
        return attribute == null || identifierPsiElement.getElementType().is(attribute);
    }

    @Override
    public boolean accepts(BasePsiElement element) {
        return true;
    }

    public IdentifierType getIdentifierType() {
        return identifierType;
    }

    public IdentifierCategory getIdentifierCategory() {
        return identifierCategory;
    }

    public DBObjectType getObjectType() {
        return objectType;
    }

    public CharSequence getIdentifierName() {
        return identifierName;
    }

    public ElementTypeAttribute getAttribute() {
        return attribute;
    }

    public void setAttribute(ElementTypeAttribute attribute) {
        this.attribute = attribute;
    }

    public String toString() {
        return "IdentifierLookupAdapter{" +
                "identifierType=" + identifierType +
                ", identifierRole=" + identifierCategory +
                ", objectType=" + objectType +
                ", identifierName='" + identifierName + '\'' +
                ", attribute=" + attribute +
                '}';
    }
}
