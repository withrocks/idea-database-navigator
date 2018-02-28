package com.dci.intellij.dbn.language.common.psi.lookup;

import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.intellij.psi.PsiElement;

public abstract class PsiScopeVisitor<T> {
    public final T visit(BasePsiElement element) {
        BasePsiElement scope = element.getEnclosingScopePsiElement();
        while (scope!= null) {
            boolean breakTreeWalk = visitScope(scope);
            if (breakTreeWalk || scope.isScopeIsolation()) break;

            // LOOKUP
            PsiElement parent = scope.getParent();
            if (parent instanceof BasePsiElement) {
                BasePsiElement basePsiElement = (BasePsiElement) parent;
                scope = basePsiElement.getEnclosingScopePsiElement();

            } else {
                scope = null;
            }
        }

        return getResult();
    }

    public abstract void setResult(T result);

    public abstract T getResult();

    protected abstract boolean visitScope(BasePsiElement scope);
}
