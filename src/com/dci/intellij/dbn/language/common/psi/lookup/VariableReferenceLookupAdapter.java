package com.dci.intellij.dbn.language.common.psi.lookup;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.element.util.IdentifierCategory;
import com.dci.intellij.dbn.language.common.element.util.IdentifierType;
import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;
import com.dci.intellij.dbn.object.common.DBObjectType;

public class VariableReferenceLookupAdapter extends IdentifierLookupAdapter {
    public VariableReferenceLookupAdapter(IdentifierPsiElement lookupIssuer, DBObjectType objectType, CharSequence identifierName) {
        super(lookupIssuer, IdentifierType.VARIABLE, IdentifierCategory.REFERENCE, objectType, identifierName);
    }

    public VariableReferenceLookupAdapter(IdentifierPsiElement lookupIssuer, @NotNull DBObjectType objectType, CharSequence identifierName, ElementTypeAttribute attribute) {
        super(lookupIssuer, IdentifierType.VARIABLE, IdentifierCategory.REFERENCE, objectType, identifierName, attribute);
    }
}
