package com.dci.intellij.dbn.language.common;

import java.util.StringTokenizer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.intellij.codeInsight.folding.impl.ElementSignatureProvider;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

public class DBLanguageElementSignatureProvider implements ElementSignatureProvider {
    public String getSignature(@NotNull PsiElement psiElement) {
        if (psiElement.getContainingFile() instanceof DBLanguagePsiFile) {
            TextRange textRange = psiElement.getTextRange();
            String offsets = textRange.getStartOffset() + "#" + textRange.getEndOffset();
            if (psiElement instanceof BasePsiElement) {
                BasePsiElement basePsiElement = (BasePsiElement) psiElement;
                return basePsiElement.getElementType().getId() + "#" + offsets;
            }

            if (psiElement instanceof PsiComment) {
                return "comment#" + offsets;
            }
        }
        return null;
    }

    public PsiElement restoreBySignature(@NotNull PsiFile psifile, @NotNull String signature, @Nullable StringBuilder processingInfoStorage) {
        if (psifile instanceof DBLanguagePsiFile) {
            StringTokenizer tokenizer = new StringTokenizer(signature, "#");
            String id = tokenizer.nextToken();
            int startOffset = Integer.parseInt(tokenizer.nextToken());
            int endOffset = Integer.parseInt(tokenizer.nextToken());

            PsiElement psiElement = psifile.findElementAt(startOffset);
            if (psiElement instanceof PsiComment) {
                if (id.equals("comment") && endOffset == startOffset + psiElement.getTextLength()) {
                    return psiElement;
                }
            }

            while (psiElement != null) {
                int elementStartOffset = psiElement.getTextOffset();
                int elementEndOffset = elementStartOffset + psiElement.getTextLength();
                if (elementStartOffset < startOffset || elementEndOffset > endOffset) {
                    break;
                }
                if (psiElement instanceof BasePsiElement) {
                    BasePsiElement basePsiElement = (BasePsiElement) psiElement;
                    if (basePsiElement.getElementType().getId().equals(id) &&
                            elementStartOffset == startOffset &&
                            elementEndOffset == endOffset) {
                        return basePsiElement;
                    }
                }
                psiElement = psiElement.getParent();
            }
        }
        return null;
    }

    public PsiElement restoreBySignature(PsiFile psifile, String signature) {
        return restoreBySignature(psifile, signature, null);

    }
}
