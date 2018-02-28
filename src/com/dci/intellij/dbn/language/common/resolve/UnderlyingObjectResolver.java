package com.dci.intellij.dbn.language.common.resolve;

import java.util.Map;

import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;
import com.dci.intellij.dbn.object.common.DBObject;
import gnu.trove.THashMap;

public abstract class UnderlyingObjectResolver {
    public static Map<String, UnderlyingObjectResolver> RESOLVERS = new THashMap<String, UnderlyingObjectResolver>();
    static {
        AliasObjectResolver.getInstance();
        AssignmentObjectResolver.getInstance();
        LocalDeclarationObjectResolver.getInstance();
        SurroundingVirtualObjectResolver.getInstance();
    }

    private String id;

    public UnderlyingObjectResolver(String id) {
        this.id = id;
        RESOLVERS.put(id, this);

    }

    public static UnderlyingObjectResolver get(String id) {
        return RESOLVERS.get(id);
    }

    public final DBObject resolve(IdentifierPsiElement identifierPsiElement) {
        return resolve(identifierPsiElement, 0);
    }

    protected abstract DBObject resolve(IdentifierPsiElement identifierPsiElement, int recursionCheck);
}
