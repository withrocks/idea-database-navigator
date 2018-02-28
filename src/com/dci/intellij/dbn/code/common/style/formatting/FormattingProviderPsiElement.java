package com.dci.intellij.dbn.code.common.style.formatting;

import com.intellij.psi.PsiElement;

public interface FormattingProviderPsiElement extends PsiElement {
    FormattingAttributes getFormattingAttributes();

    FormattingAttributes getFormattingAttributesRecursive(boolean left);
}
