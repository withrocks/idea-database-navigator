package com.dci.intellij.dbn.code.common.style.presets.clause;

import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.NamedPsiElement;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.Wrap;
import com.intellij.psi.codeStyle.CodeStyleSettings;

public class ClauseChopDownIfLongStatementPreset extends ClauseAbstractPreset {
    public ClauseChopDownIfLongStatementPreset() {
        super("chop_down_if_statement_long", "Chop down if statement long");
    }

    public Wrap getWrap(BasePsiElement psiElement, CodeStyleSettings settings) {
        BasePsiElement parentPsiElement = getParentPsiElement(psiElement);
        NamedPsiElement namedPsiElement = (NamedPsiElement) parentPsiElement.findEnclosingPsiElement(ElementTypeAttribute.EXECUTABLE);
        boolean shouldWrap = namedPsiElement.approximateLength() > settings.RIGHT_MARGIN;
        return shouldWrap ? WRAP_ALWAYS : WRAP_NONE;

    }

    public Spacing getSpacing(BasePsiElement psiElement, CodeStyleSettings settings) {
        BasePsiElement parentPsiElement = getParentPsiElement(psiElement);
        NamedPsiElement namedPsiElement = (NamedPsiElement) parentPsiElement.findEnclosingPsiElement(ElementTypeAttribute.EXECUTABLE);
        boolean shouldWrap = namedPsiElement.approximateLength() > settings.RIGHT_MARGIN;
        return getSpacing(psiElement, shouldWrap);
    }

}