package com.dci.intellij.dbn.language.common.psi.lookup;

import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.element.util.IdentifierCategory;
import com.dci.intellij.dbn.language.common.element.util.IdentifierType;
import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;
import com.dci.intellij.dbn.object.common.DBObjectType;
import org.jetbrains.annotations.NotNull;

public class VariableDefinitionLookupAdapter extends IdentifierLookupAdapter {
    public VariableDefinitionLookupAdapter(IdentifierPsiElement lookupIssuer, DBObjectType objectType, CharSequence identifierName) {
        super(lookupIssuer, IdentifierType.VARIABLE, IdentifierCategory.DEFINITION, objectType, identifierName);
    }

    public VariableDefinitionLookupAdapter(IdentifierPsiElement lookupIssuer, @NotNull DBObjectType objectType, CharSequence identifierName, ElementTypeAttribute attribute) {
        super(lookupIssuer, IdentifierType.VARIABLE, IdentifierCategory.DEFINITION, objectType, identifierName, attribute);
    }
}
