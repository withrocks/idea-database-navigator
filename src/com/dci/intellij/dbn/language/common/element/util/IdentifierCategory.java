package com.dci.intellij.dbn.language.common.element.util;

import com.dci.intellij.dbn.common.util.EnumerationUtil;
import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;

public enum IdentifierCategory {
    DEFINITION,
    REFERENCE,
    UNKNOWN,
    ALL;

    public boolean matches(IdentifierPsiElement identifierPsiElement) {
        switch (this) {
            case DEFINITION: return identifierPsiElement.isDefinition();
            case REFERENCE: return identifierPsiElement.isReference();
            default: return true;
        }
    }

    public boolean isOneOf(IdentifierCategory... categories) {
        return EnumerationUtil.isOneOf(this, categories);
    }
}
