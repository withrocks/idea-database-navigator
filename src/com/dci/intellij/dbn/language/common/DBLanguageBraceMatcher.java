package com.dci.intellij.dbn.language.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;

public abstract class DBLanguageBraceMatcher implements PairedBraceMatcher {
    private final BracePair[] bracePairs;
    private DBLanguage language;

    public DBLanguageBraceMatcher(DBLanguage language) {
        this.language = language;
        SharedTokenTypeBundle tt = language.getSharedTokenTypes();
        bracePairs = new BracePair[]{
            new BracePair(tt.getChrLeftParenthesis(), tt.getChrRightParenthesis(), false),
            new BracePair(tt.getTokenType("CHR_LEFT_BRACKET"), tt.getTokenType("CHR_RIGHT_BRACKET"), false)};
    }

    public BracePair[] getPairs() {
        return bracePairs;
    }

    public boolean isPairedBracesAllowedBeforeType(@NotNull IElementType iElementType, @Nullable IElementType iElementType1) {
        if (iElementType1 instanceof SimpleTokenType) {
            SimpleTokenType simpleTokenType = (SimpleTokenType) iElementType1;
            SharedTokenTypeBundle tt = language.getSharedTokenTypes();
            return simpleTokenType == tt.getWhiteSpace() ||
                    simpleTokenType == tt.getTokenType("CHR_DOT") ||
                    simpleTokenType == tt.getTokenType("CHR_COMMA") ||
                    simpleTokenType == tt.getTokenType("CHR_COLON") ||
                    simpleTokenType == tt.getTokenType("CHR_SEMICOLON");

        }
        return iElementType1 == null;
    }

    public int getCodeConstructStart(PsiFile psiFile, int i) {
        return i;
    }
}
