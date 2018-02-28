package com.dci.intellij.dbn.code.common.style.presets.iteration;

import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.IterationElementType;
import com.dci.intellij.dbn.language.common.element.TokenElementType;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.Wrap;
import com.intellij.psi.codeStyle.CodeStyleSettings;

public class IterationNoWrappingPreset extends IterationAbstractPreset {
    public IterationNoWrappingPreset() {
        super("no_wrapping", "No wrapping");
    }

    public Wrap getWrap(BasePsiElement psiElement, CodeStyleSettings settings) {
        return WRAP_NONE;
    }

    public Spacing getSpacing(BasePsiElement psiElement, CodeStyleSettings settings) {
        BasePsiElement parentPsiElement = getParentPsiElement(psiElement);
        IterationElementType iterationElementType = (IterationElementType) parentPsiElement.getElementType();
        ElementType elementType = psiElement.getElementType();

        if (elementType instanceof TokenElementType) {
            TokenElementType tokenElementType = (TokenElementType) elementType;
            if (iterationElementType.isSeparator(tokenElementType)) {
                return tokenElementType.isCharacter() ?
                        SPACING_NO_SPACE :
                        SPACING_ONE_SPACE;
            }
        }
        return SPACING_ONE_SPACE;
    }
}
