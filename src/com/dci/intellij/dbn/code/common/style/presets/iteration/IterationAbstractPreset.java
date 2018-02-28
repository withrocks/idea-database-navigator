package com.dci.intellij.dbn.code.common.style.presets.iteration;

import com.dci.intellij.dbn.code.common.style.presets.CodeStylePresetImpl;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.IterationElementType;
import com.dci.intellij.dbn.language.common.element.TokenElementType;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.Wrap;

public abstract class IterationAbstractPreset extends CodeStylePresetImpl {
    public IterationAbstractPreset(String id, String name) {
        super(id, name);
    }

    public boolean accepts(BasePsiElement psiElement) {
        return !psiElement.getElementType().is(ElementTypeAttribute.STATEMENT) &&
                getParentElementType(psiElement) instanceof IterationElementType;
    }

    protected Wrap getWrap(ElementType elementType, IterationElementType iterationElementType, boolean shouldWrap) {
        if (shouldWrap) {
            if (elementType instanceof TokenElementType) {
                TokenElementType tokenElementType = (TokenElementType) elementType;
                return iterationElementType.isSeparator(tokenElementType) ? null : WRAP_ALWAYS;
            } else {
                return WRAP_ALWAYS;
            }

        } else {
            return WRAP_NONE;
        }
    }

    protected Spacing getSpacing(IterationElementType iterationElementType, ElementType elementType, boolean shouldWrap) {
        if (elementType instanceof TokenElementType) {
            TokenElementType tokenElementType = (TokenElementType) elementType;
            if (iterationElementType.isSeparator(tokenElementType)) {
                return  tokenElementType.isCharacter() ?
                            SPACING_NO_SPACE :
                            SPACING_ONE_SPACE;
            }
        }
        return shouldWrap ? SPACING_LINE_BREAK : SPACING_ONE_SPACE;
    }
}
