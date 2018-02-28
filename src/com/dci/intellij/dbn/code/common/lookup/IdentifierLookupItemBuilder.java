package com.dci.intellij.dbn.code.common.lookup;

import javax.swing.Icon;

import com.dci.intellij.dbn.code.common.completion.CodeCompletionContext;
import com.dci.intellij.dbn.code.common.completion.CodeCompletionLookupConsumer;
import com.dci.intellij.dbn.language.common.element.util.IdentifierType;
import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;
import com.dci.intellij.dbn.object.common.DBObjectType;

public class IdentifierLookupItemBuilder extends LookupItemBuilder {
    private IdentifierPsiElement identifierPsiElement;
    public IdentifierLookupItemBuilder(IdentifierPsiElement identifierPsiElement) {
        this.identifierPsiElement = identifierPsiElement;
    }

    public String getTextHint() {
        IdentifierType identifierType = identifierPsiElement.getElementType().getIdentifierType();
        DBObjectType objectType = identifierPsiElement.getElementType().getObjectType();
        String objectTypeName = objectType == DBObjectType.ANY ? "object" : objectType.getName();
        String identifierTypeName =
                identifierType == IdentifierType.ALIAS  ? " alias" :
                identifierType == IdentifierType.VARIABLE ? " variable" :
                        "";
        return objectTypeName + identifierTypeName + (identifierPsiElement.isDefinition() ? " def" : " ref");
    }

    @Override
    public CodeCompletionLookupItem createLookupItem(Object source, CodeCompletionLookupConsumer consumer) {
        return super.createLookupItem(source, consumer);
    }

    public boolean isBold() {
        return false;
    }

    @Override
    public CharSequence getText(CodeCompletionContext completionContext) {
        return identifierPsiElement.getChars();
    }

    public Icon getIcon() {
        return identifierPsiElement.getObjectType().getIcon();
    }

    @Override
    public void dispose() {
        identifierPsiElement = null;
    }
}