package com.dci.intellij.dbn.language.common.resolve;

import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;
import com.dci.intellij.dbn.language.common.psi.NamedPsiElement;
import com.dci.intellij.dbn.language.common.psi.lookup.IdentifierLookupAdapter;
import com.dci.intellij.dbn.language.common.psi.lookup.PsiLookupAdapter;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;

public class LocalDeclarationObjectResolver extends UnderlyingObjectResolver{
    private static final LocalDeclarationObjectResolver INSTANCE = new LocalDeclarationObjectResolver();

    public static LocalDeclarationObjectResolver getInstance() {
        return INSTANCE;
    }

    private LocalDeclarationObjectResolver() {
        super("LOCAL_DECLARATION_RESOLVER");
    }

    @Override
    protected DBObject resolve(IdentifierPsiElement identifierPsiElement, int recursionCheck) {
        BasePsiElement underlyingObjectCandidate = null;

        DBObjectType objectType = identifierPsiElement.getObjectType();
        NamedPsiElement enclosingNamedPsiElement = identifierPsiElement.findEnclosingNamedPsiElement();
        if (objectType.matches(DBObjectType.DATASET)) {
            underlyingObjectCandidate = findObject(identifierPsiElement, enclosingNamedPsiElement, DBObjectType.DATASET);

        } else if (objectType.matches(DBObjectType.TYPE)) {
            underlyingObjectCandidate = findObject(identifierPsiElement, enclosingNamedPsiElement, DBObjectType.TYPE);

        } else if (objectType == DBObjectType.ANY || objectType == DBObjectType.ARGUMENT) {
            underlyingObjectCandidate = findObject(identifierPsiElement, enclosingNamedPsiElement, DBObjectType.TYPE);
            if (underlyingObjectCandidate == null) {
                underlyingObjectCandidate = findObject(identifierPsiElement, enclosingNamedPsiElement, DBObjectType.DATASET);
            }
        } else {
            underlyingObjectCandidate = findObject(identifierPsiElement, enclosingNamedPsiElement, objectType);
        }

        return underlyingObjectCandidate == null ? null : underlyingObjectCandidate.resolveUnderlyingObject() ;
    }

    private static BasePsiElement findObject(IdentifierPsiElement identifierPsiElement, NamedPsiElement enclosingNamedPsiElement, DBObjectType objectType) {
        PsiLookupAdapter lookupAdapter = new IdentifierLookupAdapter(identifierPsiElement, null, null, objectType, null);
        return lookupAdapter.findInElement(enclosingNamedPsiElement);
    }
}
