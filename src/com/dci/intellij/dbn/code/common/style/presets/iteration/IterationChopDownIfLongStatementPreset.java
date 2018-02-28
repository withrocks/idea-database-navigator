package com.dci.intellij.dbn.code.common.style.presets.iteration;

import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.IterationElementType;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.NamedPsiElement;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.Wrap;
import com.intellij.psi.codeStyle.CodeStyleSettings;

public class IterationChopDownIfLongStatementPreset extends IterationAbstractPreset {
    public IterationChopDownIfLongStatementPreset() {
        super("chop_down_if_statement_long", "Chop down if statement long");
    }

    public Wrap getWrap(BasePsiElement psiElement, CodeStyleSettings settings) {
        BasePsiElement parentPsiElement = getParentPsiElement(psiElement);
        IterationElementType iterationElementType = (IterationElementType) parentPsiElement.getElementType();
        ElementType elementType = psiElement.getElementType();

        NamedPsiElement namedPsiElement = (NamedPsiElement) parentPsiElement.findEnclosingPsiElement(ElementTypeAttribute.EXECUTABLE);
        boolean shouldWrap = namedPsiElement.approximateLength() > settings.RIGHT_MARGIN;
        return getWrap(elementType, iterationElementType, shouldWrap);
    }

    public Spacing getSpacing(BasePsiElement psiElement, CodeStyleSettings settings) {
        BasePsiElement parentPsiElement = getParentPsiElement(psiElement);
        IterationElementType iterationElementType = (IterationElementType) parentPsiElement.getElementType();
        ElementType elementType = psiElement.getElementType();

        NamedPsiElement namedPsiElement = (NamedPsiElement) parentPsiElement.findEnclosingPsiElement(ElementTypeAttribute.EXECUTABLE);
        boolean shouldWrap = namedPsiElement.approximateLength() > settings.RIGHT_MARGIN;
        return getSpacing(iterationElementType, elementType, shouldWrap);
    }
}