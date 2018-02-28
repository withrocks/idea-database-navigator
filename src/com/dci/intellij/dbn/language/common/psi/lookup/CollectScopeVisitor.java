package com.dci.intellij.dbn.language.common.psi.lookup;

import com.dci.intellij.dbn.language.common.psi.BasePsiElement;

import java.util.Set;

public abstract class CollectScopeVisitor extends PsiScopeVisitor<Set<BasePsiElement>>{
    private Set<BasePsiElement> bucket;

    protected final boolean visitScope(BasePsiElement scope) {
        bucket = performCollect(scope);
        return false;
    }

    public void setResult(Set<BasePsiElement> bucket) {
        this.bucket = bucket;
    }

    public Set<BasePsiElement> getResult() {
        return bucket;
    }

    protected abstract Set<BasePsiElement> performCollect(BasePsiElement scope);
}
