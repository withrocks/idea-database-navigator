package com.dci.intellij.dbn.code.common.style.presets.statement;

import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.Wrap;
import com.intellij.psi.codeStyle.CodeStyleSettings;

public class StatementLineBreakAtLeastPreset extends StatementAbstractPreset {
    public StatementLineBreakAtLeastPreset() {
        super("one_line_break_at_least", "One line break at least");
    }

    public Wrap getWrap(BasePsiElement psiElement, CodeStyleSettings settings) {
        return WRAP_ALWAYS;
    }

    public Spacing getSpacing(BasePsiElement psiElement, CodeStyleSettings settings) {
        return null;
    }
}