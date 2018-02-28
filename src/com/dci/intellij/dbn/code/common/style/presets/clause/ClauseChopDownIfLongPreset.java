package com.dci.intellij.dbn.code.common.style.presets.clause;

import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.Wrap;
import com.intellij.psi.codeStyle.CodeStyleSettings;

public class ClauseChopDownIfLongPreset extends ClauseAbstractPreset {
    public ClauseChopDownIfLongPreset() {
        super("chop_down_if_long", "Chop down if long");
    }

    public Wrap getWrap(BasePsiElement psiElement, CodeStyleSettings settings) {
        boolean shouldWrap = psiElement.approximateLength() > settings.RIGHT_MARGIN;
        return shouldWrap ? WRAP_ALWAYS : WRAP_NONE;

    }

    public Spacing getSpacing(BasePsiElement psiElement, CodeStyleSettings settings) {
        boolean shouldChopDown = psiElement.approximateLength() > settings.RIGHT_MARGIN;
        return getSpacing(psiElement, shouldChopDown);
    }
}