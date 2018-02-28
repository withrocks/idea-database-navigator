package com.dci.intellij.dbn.code.common.style.presets.iteration;

import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.IterationElementType;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.Wrap;
import com.intellij.psi.codeStyle.CodeStyleSettings;

public class IterationChopDownIfLongPreset extends IterationAbstractPreset {
    public IterationChopDownIfLongPreset() {
        super("chop_down_if_long", "Chop down if long");
    }

    public Wrap getWrap(BasePsiElement psiElement, CodeStyleSettings settings) {
        BasePsiElement parentPsiElement = getParentPsiElement(psiElement);
        IterationElementType iterationElementType = (IterationElementType) parentPsiElement.getElementType();
        ElementType elementType = psiElement.getElementType();

        boolean shouldWrap = parentPsiElement.approximateLength() > settings.RIGHT_MARGIN;
        return getWrap(elementType, iterationElementType, shouldWrap);
    }

    public Spacing getSpacing(BasePsiElement psiElement, CodeStyleSettings settings) {
        BasePsiElement parentPsiElement = getParentPsiElement(psiElement);
        IterationElementType iterationElementType = (IterationElementType) parentPsiElement.getElementType();
        ElementType elementType = psiElement.getElementType();

        boolean shouldWrap = parentPsiElement.approximateLength() > settings.RIGHT_MARGIN;
        return getSpacing(iterationElementType, elementType, shouldWrap);
    }

}