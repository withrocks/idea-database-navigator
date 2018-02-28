package com.dci.intellij.dbn.language.common.psi;

import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.LeafElementType;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.lookup.ObjectLookupAdapter;
import com.dci.intellij.dbn.language.common.psi.lookup.PsiLookupAdapter;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.ObjectTypeFilter;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.IncorrectOperationException;
import gnu.trove.THashSet;

public abstract class LeafPsiElement extends BasePsiElement implements PsiReference {

    public LeafPsiElement(ASTNode astNode, ElementType elementType) {
        super(astNode, elementType);
    }

    public int approximateLength() {
        return getTextLength() + 1;
    }

    @Override
    public LeafElementType getElementType() {
        return (LeafElementType) super.getElementType();
    }

    @Override
    public PsiReference getReference() {
        return this;
    }

    public CharSequence getChars() {
        return getNode().getFirstChildNode().getChars();
    }

    /*********************************************************
     *                       PsiReference                    *
     *********************************************************/

    public PsiElement getElement() {
        return this;
    }

    @Nullable
    public PsiElement resolve() {
        return null;
    }

    @NotNull
    public String getCanonicalText() {
        return null;
    }

    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
        return null;
    }

    public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
        return null;
    }

    public boolean isReferenceTo(PsiElement element) {
        return false;
    }

    public TextRange getRangeInElement() {                                        
        return new TextRange(0, getTextLength());
    }

    public boolean isSoft() {
        return true;
    }

    @NotNull
    public Object[] getVariants() {
        return PsiElement.EMPTY_ARRAY;
    }

    public static Set<DBObject> identifyPotentialParentObjects(DBObjectType objectType, @Nullable ObjectTypeFilter filter, BasePsiElement sourceScope, LeafPsiElement lookupIssuer) {
        ConnectionHandler connectionHandler = sourceScope.getActiveConnection();
        Set<DBObject> parentObjects = null;
        Set<DBObjectType> parentTypes = objectType.getGenericParents();
        if (parentTypes.size() > 0) {
            if (objectType.isSchemaObject() && connectionHandler != null && !connectionHandler.isVirtual()) {
                DBObjectBundle objectBundle = connectionHandler.getObjectBundle();

                if (filter == null || filter.acceptsCurrentSchemaObject(objectType)) {
                    DBSchema currentSchema = sourceScope.getCurrentSchema();
                    parentObjects = addObjectToSet(parentObjects, currentSchema);
                }

                if (filter == null || filter.acceptsPublicSchemaObject(objectType)) {
                    DBSchema publicSchema = objectBundle.getPublicSchema();
                    parentObjects = addObjectToSet(parentObjects, publicSchema);
                }
            }

            Set<BasePsiElement> parentObjectPsiElements = null;
            for (DBObjectType parentObjectType : parentTypes) {
                PsiLookupAdapter lookupAdapter = new ObjectLookupAdapter(lookupIssuer, parentObjectType, null);
                parentObjectPsiElements = !objectType.isSchemaObject() && parentObjectType.isSchemaObject() ?
                        lookupAdapter.collectInScope(sourceScope, parentObjectPsiElements) :
                        lookupAdapter.collectInParentScopeOf(sourceScope, parentObjectPsiElements);
            }

            if (parentObjectPsiElements != null) {
                for (BasePsiElement parentObjectPsiElement : parentObjectPsiElements) {
                    if (!parentObjectPsiElement.containsPsiElement(sourceScope)) {
                        DBObject parentObject = parentObjectPsiElement.resolveUnderlyingObject();
                        parentObjects = addObjectToSet(parentObjects, parentObject);
                    }
                }
            }
        }

        DBObject fileObject = sourceScope.getFile().getUnderlyingObject();
        if (fileObject != null && fileObject.getObjectType().isParentOf(objectType)) {
            parentObjects = addObjectToSet(parentObjects, fileObject);
        }

        return parentObjects;
    }

    private static Set<DBObject> addObjectToSet(Set<DBObject> objects, DBObject object) {
        if (object != null && !object.isDisposed()) {
            if (objects == null) objects = new THashSet<DBObject>();
            objects.add(object);
        }
        return objects;
    }

    @Override
    public BasePsiElement findPsiElementByAttribute(ElementTypeAttribute attribute) {
        return getElementType().is(attribute) ? this : null;
    }

    public BasePsiElement findFirstPsiElement(ElementTypeAttribute attribute) {
        if (this.getElementType().is(attribute)) {
            return this;
        }
        return null;
    }

    @Override
    public BasePsiElement findFirstPsiElement(Class<? extends ElementType> clazz) {
        if (this.getElementType().getClass().isAssignableFrom(clazz)) {
            return this;
        }
        return null;
    }

    public BasePsiElement findFirstLeafPsiElement() {
        return this;
    }
}
