package com.dci.intellij.dbn.language.psql.structure;

import com.dci.intellij.dbn.language.common.structure.DBLanguageStructureViewElement;
import com.intellij.psi.PsiElement;

public class PSQLStructureViewElement extends DBLanguageStructureViewElement<PSQLStructureViewElement> {

    public PSQLStructureViewElement(PsiElement psiElement) {
        super(psiElement);
    }

    @Override
    protected PSQLStructureViewElement createChildElement(PsiElement child) {
        return new PSQLStructureViewElement(child);
    }

}
