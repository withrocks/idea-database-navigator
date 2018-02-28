package com.dci.intellij.dbn.language.common.psi.lookup;

import com.dci.intellij.dbn.language.common.psi.BasePsiElement;

public abstract class LookupScopeVisitor extends PsiScopeVisitor<BasePsiElement> {
    private BasePsiElement result;

    protected final boolean visitScope(BasePsiElement scope) {
        result = performLookup(scope);
        return result != null;
    }

    public BasePsiElement getResult() {
        return result;
    }

    @Override
    public void setResult(BasePsiElement result) {
        this.result = result;
    }

    protected abstract BasePsiElement performLookup(BasePsiElement scope);
}
