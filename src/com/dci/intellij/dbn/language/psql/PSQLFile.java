package com.dci.intellij.dbn.language.psql;

import java.util.Set;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.dci.intellij.dbn.language.common.psi.lookup.IdentifierDefinitionLookupAdapter;
import com.dci.intellij.dbn.language.common.psi.lookup.LookupAdapterCache;
import com.dci.intellij.dbn.language.common.psi.lookup.ObjectDefinitionLookupAdapter;
import com.dci.intellij.dbn.language.common.psi.lookup.PsiLookupAdapter;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import gnu.trove.THashSet;

public class PSQLFile extends DBLanguagePsiFile {

    public PSQLFile(FileViewProvider fileViewProvider, @NotNull PSQLLanguage language) {
        super(fileViewProvider, PSQLFileType.INSTANCE, language);
    }

    public BasePsiElement lookupObjectSpecification(DBObjectType objectType, CharSequence objectName) {
        PsiElement child = getFirstChild();
        while (child != null) {
            if (child instanceof BasePsiElement) {
                BasePsiElement basePsiElement = (BasePsiElement) child;
                PsiLookupAdapter lookupAdapter = new ObjectDefinitionLookupAdapter(null, objectType, objectName, ElementTypeAttribute.SUBJECT);
                BasePsiElement specObject = lookupAdapter.findInScope(basePsiElement);
                if (specObject != null) {
                    return specObject.findEnclosingPsiElement(ElementTypeAttribute.OBJECT_SPECIFICATION);
                }
            }
            child = child.getNextSibling();
        }
        return null;
    }

    public BasePsiElement lookupObjectDeclaration(DBObjectType objectType, CharSequence objectName) {
        PsiElement child = getFirstChild();
        while (child != null) {
            if (child instanceof BasePsiElement) {
                BasePsiElement basePsiElement = (BasePsiElement) child;
                PsiLookupAdapter lookupAdapter = new ObjectDefinitionLookupAdapter(null, objectType, objectName, ElementTypeAttribute.SUBJECT);
                BasePsiElement specObject = lookupAdapter.findInScope(basePsiElement);
                if (specObject != null) {
                    return specObject.findEnclosingPsiElement(ElementTypeAttribute.OBJECT_DECLARATION);
                }
            }
            child = child.getNextSibling();
        }
        return null;
    }

    public Set<BasePsiElement> lookupVariableDefinition(int offset) {
        BasePsiElement scope = PsiUtil.lookupElementAtOffset(this, ElementTypeAttribute.SCOPE_DEMARCATION, offset);
        Set<BasePsiElement> variableDefinitions = new THashSet<BasePsiElement>();
        while (scope != null) {
            PsiLookupAdapter lookupAdapter = new IdentifierDefinitionLookupAdapter(null, DBObjectType.ARGUMENT, null);
            variableDefinitions = scope.collectPsiElements(lookupAdapter, variableDefinitions, 0);

            lookupAdapter = LookupAdapterCache.VARIABLE_DEFINITION.get(DBObjectType.ANY);
            variableDefinitions = scope.collectPsiElements(lookupAdapter, variableDefinitions, 0);

            PsiElement parent = scope.getParent();
            if (parent instanceof BasePsiElement) {
                BasePsiElement basePsiElement = (BasePsiElement) parent;
                scope = basePsiElement.findEnclosingPsiElement(ElementTypeAttribute.SCOPE_DEMARCATION);
                if (scope == null) scope = basePsiElement.findEnclosingPsiElement(ElementTypeAttribute.SCOPE_ISOLATION);
            } else {
                scope = null;
            }
        }
        return variableDefinitions;
    }
}
