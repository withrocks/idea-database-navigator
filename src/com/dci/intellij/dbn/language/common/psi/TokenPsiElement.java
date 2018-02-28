package com.dci.intellij.dbn.language.common.psi;

import javax.swing.Icon;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.TokenElementType;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.lookup.PsiLookupAdapter;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;

public class TokenPsiElement extends LeafPsiElement {
    public TokenPsiElement(ASTNode astNode, TokenElementType elementType) {
        super(astNode, elementType);
    }

    public TokenElementType getElementType() {
        return (TokenElementType) super.getElementType();
    }

    @Nullable
    public BasePsiElement findPsiElement(PsiLookupAdapter lookupAdapter, int scopeCrossCount) {
        if (lookupAdapter.matches(this)) {
            return this;
        }
        return null;
    }
    @Nullable
    public Set<BasePsiElement> collectPsiElements(PsiLookupAdapter lookupAdapter, @Nullable Set<BasePsiElement> bucket, int scopeCrossCount) {return bucket;}

    public void collectExecVariablePsiElements(@NotNull Set<ExecVariablePsiElement> bucket) {}
    public void collectSubjectPsiElements(@NotNull Set<IdentifierPsiElement> bucket) {}
    public NamedPsiElement findNamedPsiElement(String id) {return null;}
    public BasePsiElement findPsiElementBySubject(ElementTypeAttribute attribute, CharSequence subjectName, DBObjectType subjectType) {return null;}


    /*********************************************************
     *                       PsiReference                    *
     *********************************************************/
    public boolean isReferenceTo(PsiElement element) {
        return true;
    }

    public boolean isSoft() {
        return false;
    }

    /*********************************************************
     *                       ItemPresentation                *
     *********************************************************/
    public String getPresentableText() {
        return getElementType().getTokenType().getValue();
    }

    @Nullable
    public String getLocationString() {
        return null;
    }

    @Nullable
    public Icon getIcon(boolean open) {
        return null;
    }

    @Nullable
    public TextAttributesKey getTextAttributesKey() {
        return null;
    }

    public boolean hasErrors() {
        return false;
    }

    @Override
    public boolean matches(BasePsiElement basePsiElement, MatchType matchType) {
        if (basePsiElement instanceof TokenPsiElement) {
            TokenPsiElement remote = (TokenPsiElement) basePsiElement;
            TokenType localTokenType = getElementType().getTokenType();
            TokenType remoteTokenType = remote.getElementType().getTokenType();
            if (localTokenType == remoteTokenType) {
                if (matchType == MatchType.SOFT) {
                    return true;
                } else {
                    if (localTokenType.isNumeric() || localTokenType.isLiteral()) {
                        return StringUtil.equals(getChars(), remote.getChars());
                    } else {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
