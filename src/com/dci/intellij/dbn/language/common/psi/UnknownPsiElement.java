package com.dci.intellij.dbn.language.common.psi;

import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.code.common.style.formatting.FormattingAttributes;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.lookup.PsiLookupAdapter;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.lang.ASTNode;

public class UnknownPsiElement extends BasePsiElement {
    public UnknownPsiElement(ASTNode astNode, ElementType elementType) {
        super(astNode, elementType);
    }

    @Override
    public FormattingAttributes getFormattingAttributes() {
        return FormattingAttributes.NO_ATTRIBUTES;
    }

    public int approximateLength() {
        return getTextLength();
    }

    @Nullable
    @Override public BasePsiElement findPsiElement(PsiLookupAdapter lookupAdapter, int scopeCrossCount) {return null;}
    @Nullable
    @Override public Set<BasePsiElement> collectPsiElements(PsiLookupAdapter lookupAdapter, @Nullable Set<BasePsiElement> bucket, int scopeCrossCount) {return bucket;}


    @Override public void collectExecVariablePsiElements(@NotNull Set<ExecVariablePsiElement> bucket) {}
    @Override public void collectSubjectPsiElements(@NotNull Set<IdentifierPsiElement> bucket) {}
    @Override public NamedPsiElement findNamedPsiElement(String id) {return null;}
    @Override public BasePsiElement findFirstPsiElement(ElementTypeAttribute attribute) {return null;}
    @Override public BasePsiElement findFirstPsiElement(Class<? extends ElementType> clazz) { return null; }

    @Override public BasePsiElement findFirstLeafPsiElement() {return null;}
    @Override public BasePsiElement findPsiElementByAttribute(ElementTypeAttribute attribute) {return null;}

    @Override public BasePsiElement findPsiElementBySubject(ElementTypeAttribute attribute, CharSequence subjectName, DBObjectType subjectType) {return null;}

    @Override public boolean hasErrors() {
        return true;
    }

    @Override
    public boolean matches(BasePsiElement remote, MatchType matchType) {
        if (matchType == MatchType.SOFT) {
            return remote instanceof UnknownPsiElement;
        } else {
            return getTextLength() == remote.getTextLength() && StringUtil.equals(getText(), remote.getText());
        }
    }

    @Override
    public String toString() {
        return getElementType().getDebugName();

    }
}
