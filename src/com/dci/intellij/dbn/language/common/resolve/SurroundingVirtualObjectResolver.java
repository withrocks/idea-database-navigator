package com.dci.intellij.dbn.language.common.resolve;

import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;

public class SurroundingVirtualObjectResolver extends UnderlyingObjectResolver{
    private static final SurroundingVirtualObjectResolver INSTANCE = new SurroundingVirtualObjectResolver();

    public static SurroundingVirtualObjectResolver getInstance() {
        return INSTANCE;
    }

    private SurroundingVirtualObjectResolver() {
        super("VIRTUAL_OBJECT_RESOLVER");
    }

    @Override
    protected DBObject resolve(IdentifierPsiElement identifierPsiElement, int recursionCheck) {
        DBObjectType objectType = identifierPsiElement.getObjectType();
        if (objectType != DBObjectType.DATASET) {
            BasePsiElement virtualObjectPsiElement = identifierPsiElement.findEnclosingVirtualObjectPsiElement(objectType);
            if (virtualObjectPsiElement != null) {
                return virtualObjectPsiElement.resolveUnderlyingObject();
            }
        }

        return null;
    }
}
