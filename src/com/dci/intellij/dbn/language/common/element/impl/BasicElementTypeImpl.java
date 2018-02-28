package com.dci.intellij.dbn.language.common.element.impl;

import com.dci.intellij.dbn.language.common.element.BasicElementType;
import com.dci.intellij.dbn.language.common.element.ElementTypeBundle;
import com.dci.intellij.dbn.language.common.element.lookup.BasicElementTypeLookupCache;
import com.dci.intellij.dbn.language.common.element.parser.impl.BasicElementTypeParser;
import com.dci.intellij.dbn.language.common.psi.UnknownPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;

public class BasicElementTypeImpl extends AbstractElementType implements BasicElementType {

    public BasicElementTypeImpl(ElementTypeBundle bundle, String id, String description) {
        super(bundle, null, id, description);
    }
    public BasicElementTypeImpl(ElementTypeBundle bundle) {
        this(bundle, "UNKNOWN", "Unidentified element type.");
    }

    @Override
    public BasicElementTypeLookupCache createLookupCache() {
        return new BasicElementTypeLookupCache(this);
    }

    @Override
    public BasicElementTypeParser createParser() {
        return new BasicElementTypeParser(this);
    }

    public boolean isLeaf() {
        return true;
    }

    public String getDebugName() {
        return getId();
    }

    public PsiElement createPsiElement(ASTNode astNode) {
        return new UnknownPsiElement(astNode, this);
    }

}
