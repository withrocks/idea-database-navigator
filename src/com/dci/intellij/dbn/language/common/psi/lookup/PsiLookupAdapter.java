package com.dci.intellij.dbn.language.common.psi.lookup;

import java.util.Set;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.language.common.psi.BasePsiElement;

public abstract class PsiLookupAdapter {
    public abstract boolean matches(BasePsiElement element);

    public abstract boolean accepts(BasePsiElement element);

    public final BasePsiElement findInParentScopeOf(final BasePsiElement source) {
        //System.out.println(this);
        LookupScopeVisitor finder = new LookupScopeVisitor() {
            protected BasePsiElement performLookup(BasePsiElement scope) {
                BasePsiElement result = scope.findPsiElement(PsiLookupAdapter.this, 10);
                return result == null || result == source ? null : result;
            }
        };
        return finder.visit(source);
    }

    public final BasePsiElement findInScope(BasePsiElement scope) {
        return scope.findPsiElement(this, 100);
    }

    public final BasePsiElement findInElement(BasePsiElement element) {
        return element.findPsiElement(this, 100);
    }


    public final Set<BasePsiElement> collectInParentScopeOf(BasePsiElement source) {
        return collectInParentScopeOf(source, null);
    }


    public final Set<BasePsiElement> collectInParentScopeOf(BasePsiElement source, Set<BasePsiElement> bucket) {
        CollectScopeVisitor collector = new CollectScopeVisitor() {
            protected Set<BasePsiElement> performCollect(BasePsiElement scope) {
                return scope.collectPsiElements(PsiLookupAdapter.this, getResult(), 1);
            }

        };
        collector.setResult(bucket);
        return collector.visit(source);
    }

    public final Set<BasePsiElement> collectInScope(BasePsiElement scope, Set<BasePsiElement> bucket) {
        if (!scope.isScopeBoundary()) scope = scope.getEnclosingScopePsiElement();
        return scope.collectPsiElements(this, bucket, 100);
    }

    @Nullable
    public final Set<BasePsiElement> collectInElement(BasePsiElement element, Set<BasePsiElement> bucket) {
        return element.collectPsiElements(this, bucket, 100);
    }
}
