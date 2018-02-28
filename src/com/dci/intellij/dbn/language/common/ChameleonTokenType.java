package com.dci.intellij.dbn.language.common;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.ElementTypeBundle;
import com.dci.intellij.dbn.language.common.element.LeafElementType;
import com.dci.intellij.dbn.language.common.element.impl.WrappingDefinition;
import com.dci.intellij.dbn.language.common.element.lookup.ElementTypeLookupCache;
import com.dci.intellij.dbn.language.common.element.parser.Branch;
import com.dci.intellij.dbn.language.common.element.parser.ElementTypeParser;
import com.dci.intellij.dbn.language.common.element.path.PathNode;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttributesBundle;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;

public class ChameleonTokenType extends SimpleTokenType implements ElementType {
    private DBLanguageDialect injectedLanguage;
    public ChameleonTokenType(@Nullable DBLanguageDialect hostLanguage, DBLanguageDialect injectedLanguage) {
        super(injectedLanguage.getID() + " block", hostLanguage);
        this.injectedLanguage = injectedLanguage;
    }

    @NotNull
    @Override
    public DBLanguage getLanguage() {
        return getLanguageDialect().getBaseLanguage();
    }

    @Override
    public DBLanguageDialect getLanguageDialect() {
        return (DBLanguageDialect) super.getLanguage();
    }

    public DBLanguageDialect getInjectedLanguage() {
        return injectedLanguage;
    }

    public String getDebugName() {
        return toString();
    }

    public Icon getIcon() {
        return null;
    }

    public ElementType getParent() {
        return null;
    }

    public ElementTypeLookupCache getLookupCache() {
        return null;
    }

    public ElementTypeParser getParser() {
        return null;
    }

    public boolean is(ElementTypeAttribute attribute) {
        return false;
    }

    public boolean isLeaf() {
        return false;
    }

    public boolean isVirtualObject() {
        return false;
    }

    public DBObjectType getVirtualObjectType() {
        return null;
    }

    public PsiElement createPsiElement(ASTNode astNode) {
        return new ASTWrapperPsiElement(astNode);
    }

    public ElementTypeBundle getElementBundle() {
        return null;
    }

    @Override
    public ElementTypeAttributesBundle getAttributes() {
        return null;
    }

    @Override
    public WrappingDefinition getWrapping() {
        return null;
    }

    @Override
    public boolean isWrappingBegin(LeafElementType elementType) {
        return false;
    }

    @Override
    public boolean isWrappingEnd(LeafElementType elementType) {
        return false;
    }

    @Nullable
    @Override
    public Branch getBranch() {
        return null;
    }

    @Override
    public int getIndexInParent(PathNode pathNode) {
        return 0;
    }
}
