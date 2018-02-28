package com.dci.intellij.dbn.language.common.resolve;

import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;
import com.dci.intellij.dbn.language.common.psi.NamedPsiElement;
import com.dci.intellij.dbn.language.common.psi.lookup.ObjectReferenceLookupAdapter;
import com.dci.intellij.dbn.language.common.psi.lookup.PsiLookupAdapter;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;

public class AssignmentObjectResolver extends UnderlyingObjectResolver{
    private static final AssignmentObjectResolver INSTANCE = new AssignmentObjectResolver();

    public static AssignmentObjectResolver getInstance() {
        return INSTANCE;
    }

    private AssignmentObjectResolver() {
        super("ASSIGNMENT_RESOLVER");
    }

    @Override
    protected DBObject resolve(IdentifierPsiElement identifierPsiElement, int recursionCheck) {
        NamedPsiElement enclosingNamedPsiElement = identifierPsiElement.findEnclosingNamedPsiElement();
        PsiLookupAdapter lookupAdapter = new ObjectReferenceLookupAdapter(identifierPsiElement, DBObjectType.TYPE, null);
        BasePsiElement underlyingObjectCandidate = lookupAdapter.findInElement(enclosingNamedPsiElement);

        return underlyingObjectCandidate == null ? null : underlyingObjectCandidate.resolveUnderlyingObject() ;
    }
}
