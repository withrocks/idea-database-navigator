package com.dci.intellij.dbn.code.common.style.presets.clause;

import com.dci.intellij.dbn.language.common.SharedTokenTypeBundle;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.TokenPsiElement;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.Wrap;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.CodeStyleSettings;

public class ClauseChopDownNeverPreset extends ClauseAbstractPreset {
    public ClauseChopDownNeverPreset() {
        super("do_not_chop_down", "Do not chop down");
    }

    public Wrap getWrap(BasePsiElement psiElement, CodeStyleSettings settings) {
        return WRAP_NONE;
    }

    public Spacing getSpacing(BasePsiElement psiElement, CodeStyleSettings settings) {
        PsiElement previousPsiElement = psiElement.getPrevSibling();
        if (previousPsiElement instanceof TokenPsiElement) {
            TokenPsiElement previousToken = (TokenPsiElement) previousPsiElement;
            SharedTokenTypeBundle sharedTokenTypes = psiElement.getLanguage().getSharedTokenTypes();
            TokenType tokenType = previousToken.getElementType().getTokenType();
            return tokenType ==  sharedTokenTypes.getChrLeftParenthesis() ?
                    SPACING_NO_SPACE :
                    SPACING_ONE_SPACE;

        }
        return SPACING_ONE_SPACE;
    }
}
