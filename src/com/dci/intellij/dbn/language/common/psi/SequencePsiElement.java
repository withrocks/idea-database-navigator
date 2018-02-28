package com.dci.intellij.dbn.language.common.psi;

import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.code.common.style.formatting.FormattingAttributes;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.language.common.element.BlockElementType;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.IterationElementType;
import com.dci.intellij.dbn.language.common.element.NamedElementType;
import com.dci.intellij.dbn.language.common.element.OneOfElementType;
import com.dci.intellij.dbn.language.common.element.SequenceElementType;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.lookup.PsiLookupAdapter;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.psi.PsiElement;
import gnu.trove.THashSet;

public class SequencePsiElement extends BasePsiElement {
    public SequencePsiElement(ASTNode astNode, ElementType elementType) {
        super(astNode, elementType);
    }

    @Override
    public FormattingAttributes getFormattingAttributes() {
        return super.getFormattingAttributes();
    }

    public int approximateLength() {
        int length = 0;
        PsiElement child = getFirstChild();
        while (child != null) {
            if (child instanceof BasePsiElement) {
                BasePsiElement basePsiElement = (BasePsiElement) child;
                length = length + basePsiElement.approximateLength();
            }
            child = child.getNextSibling();
        }
        return length;
    }

    /*********************************************************
     *                   Lookup routines                     *
     *********************************************************/

    @Nullable
    public BasePsiElement findPsiElement(
            PsiLookupAdapter lookupAdapter,
            int scopeCrossCount) {

        PsiElement child = getFirstChild();
        while (child != null) {
            ProgressIndicatorProvider.checkCanceled();
            if (child instanceof BasePsiElement) {
                BasePsiElement basePsiElement = (BasePsiElement) child;
                if (lookupAdapter.accepts(basePsiElement)) {
                    boolean isScopeBoundary = basePsiElement.isScopeBoundary();
                    if (!isScopeBoundary || scopeCrossCount > 0) {
                        int childScopeCrossCount = isScopeBoundary ? scopeCrossCount-1 : scopeCrossCount;
                        BasePsiElement result = basePsiElement.findPsiElement(lookupAdapter, childScopeCrossCount);
                        if (result != null) return result;
                    }
                }
            }
            child = child.getNextSibling();
        }
        return null;
    }

    @Nullable
    public Set<BasePsiElement> collectPsiElements(
            PsiLookupAdapter lookupAdapter,
            @Nullable Set<BasePsiElement> bucket,
            int scopeCrossCount) {

        if (lookupAdapter.matches(this)) {
            if (bucket == null) bucket = new THashSet<BasePsiElement>();
            bucket.add(this);

        }
        PsiElement child = getFirstChild();
        while (child != null) {
            if (child instanceof BasePsiElement) {
                BasePsiElement basePsiElement = (BasePsiElement) child;

                if (lookupAdapter.accepts(basePsiElement) || bucket == null) {
                    boolean isScopeBoundary = basePsiElement.isScopeBoundary();
                    if (!isScopeBoundary || scopeCrossCount > 0) {
                        int childScopeCrossCount = isScopeBoundary ? scopeCrossCount-1 : scopeCrossCount;
                        bucket = basePsiElement.collectPsiElements(lookupAdapter, bucket, childScopeCrossCount);
                    }
                }
            }
            child = child.getNextSibling();
        }
        return bucket;
    }

    public void collectExecVariablePsiElements(@NotNull Set<ExecVariablePsiElement> bucket) {
        PsiElement child = getFirstChild();
        while (child != null) {
            if (child instanceof BasePsiElement) {
                BasePsiElement basePsiElement = (BasePsiElement) child;
                basePsiElement.collectExecVariablePsiElements(bucket);
            }
            child = child.getNextSibling();
        }
    }

    public void collectSubjectPsiElements(@NotNull Set<IdentifierPsiElement> bucket) {
        PsiElement child = getFirstChild();
        while (child != null) {
            if (child instanceof BasePsiElement) {
                BasePsiElement basePsiElement = (BasePsiElement) child;
                basePsiElement.collectSubjectPsiElements(bucket);
            }
            child = child.getNextSibling();
        }
    }

    public void collectVirtualObjectPsiElements(Set<BasePsiElement> bucket, DBObjectType objectType) {
        //if (getElementType().getLookupCache().containsVirtualObject(objectType)) {
            if (getElementType().isVirtualObject()) {
                DBObjectType virtualObjectType = getElementType().getVirtualObjectType();
                if (objectType == virtualObjectType) {
                    bucket.add(this);
                }
            }
            PsiElement child = getFirstChild();
            while (child != null) {
                if (child instanceof BasePsiElement) {
                    BasePsiElement basePsiElement = (BasePsiElement) child;
                    basePsiElement.collectVirtualObjectPsiElements(bucket, objectType);
                }
                child = child.getNextSibling();
            }
        //}
    }

    public NamedPsiElement findNamedPsiElement(String id) {
        PsiElement child = getFirstChild();
        while (child != null) {
            if (child instanceof SequencePsiElement) {
                SequencePsiElement bundlePsiElement = (SequencePsiElement) child;
                if (bundlePsiElement instanceof NamedPsiElement) {
                    NamedPsiElement namedPsiElement = (NamedPsiElement) bundlePsiElement;
                    if (namedPsiElement.getElementType().getId().equals(id)) {
                        return namedPsiElement;
                    }
                }

                NamedPsiElement namedPsiElement = bundlePsiElement.findNamedPsiElement(id);
                if (namedPsiElement != null) {
                    return namedPsiElement;
                }
            }
            child = child.getNextSibling();
        }
        return null;
    }

    @Override
    public BasePsiElement findFirstPsiElement(ElementTypeAttribute attribute) {
        if (this.getElementType().is(attribute)) {
            return this;
        }

        PsiElement child = getFirstChild();
        while (child != null) {
            if (child instanceof BasePsiElement) {
                BasePsiElement basePsiElement = (BasePsiElement) child;
                BasePsiElement firstElement = basePsiElement.findFirstPsiElement(attribute);
                if (firstElement != null) {
                    return firstElement;
                }
            }
            child = child.getNextSibling();
        }
        return null;
    }

    @Override
    public BasePsiElement findFirstPsiElement(Class<? extends ElementType> clazz) {
        if (clazz.isAssignableFrom(this.getElementType().getClass())) {
            return this;
        }

        PsiElement child = getFirstChild();
        while (child != null) {
            if (child instanceof BasePsiElement) {
                BasePsiElement basePsiElement = (BasePsiElement) child;
                BasePsiElement firstElement = basePsiElement.findFirstPsiElement(clazz);
                if (firstElement != null) {
                    return firstElement;
                }
            }
            child = child.getNextSibling();
        }
        return null;
    }

    public BasePsiElement findFirstLeafPsiElement() {
        PsiElement firstChild = getFirstChild();
        while (firstChild != null) {
            if (firstChild instanceof BasePsiElement) {
                BasePsiElement basePsiElement = (BasePsiElement) firstChild;
                return basePsiElement.findFirstLeafPsiElement();
            }
            firstChild = firstChild.getNextSibling();
        }
        return null;
    }

    public BasePsiElement findPsiElementBySubject(ElementTypeAttribute attribute, CharSequence subjectName, DBObjectType subjectType) {
        if (getElementType().is(attribute)) {
            BasePsiElement subjectPsiElement = findFirstPsiElement(ElementTypeAttribute.SUBJECT);
            if (subjectPsiElement instanceof IdentifierPsiElement) {
                IdentifierPsiElement identifierPsiElement = (IdentifierPsiElement) subjectPsiElement;
                if (identifierPsiElement.getObjectType() == subjectType &&
                        StringUtil.equalsIgnoreCase(subjectName, identifierPsiElement.getChars())) {
                    return this;
                }
            }
        }
        PsiElement child = getFirstChild();
        while (child != null) {
            if (child instanceof BasePsiElement) {
                BasePsiElement basePsiElement = (BasePsiElement) child;
                BasePsiElement childPsiElement = basePsiElement.findPsiElementBySubject(attribute, subjectName, subjectType);
                if (childPsiElement != null) {
                    return childPsiElement;
                }
            }
            child = child.getNextSibling();
        }
        return null;
    }

    @Override
    public BasePsiElement findPsiElementByAttribute(ElementTypeAttribute attribute) {
        if (getElementType().is(attribute)) {
            return this;
        }
        PsiElement child = getFirstChild();
        while (child != null) {
            if (child instanceof BasePsiElement) {
                BasePsiElement basePsiElement = (BasePsiElement) child;
                BasePsiElement childPsiElement = basePsiElement.findPsiElementByAttribute(attribute);
                if (childPsiElement != null) {
                    return childPsiElement;
                }
            }
            child = child.getNextSibling();

        }
        return null;
    }

    public boolean containsPsiElement(BasePsiElement childPsiElement) {
        if (this == childPsiElement) {
            return true;
        }

        PsiElement child = getFirstChild();
        while (child != null) {
            if (child instanceof BasePsiElement) {
                BasePsiElement basePsiElement = (BasePsiElement) child;
                boolean containsPsiElement = basePsiElement.containsPsiElement(childPsiElement);
                if (containsPsiElement) {
                    return true;
                }
            }
            child = child.getNextSibling();
        }
        return false;
    }

    /*********************************************************
     *                    Miscellaneous                      *
     *********************************************************/
     public boolean hasErrors() {
         PsiElement child = getFirstChild();

         while (child != null) {
            if (child instanceof BasePsiElement) {
                BasePsiElement basePsiElement = (BasePsiElement) child;
                if (basePsiElement.hasErrors()) {
                    return true;
                }
            }
             child = child.getNextSibling();
         }

/*         if (true) return false;

         PsiElement[] psiElements = getChildren();
         if (getElementType() instanceof SequenceElementType) {
            int offset = 0;
            SequenceElementType sequenceElementType = (SequenceElementType) getElementType();
            ElementTypeRef[] children = sequenceElementType.getChildren();

            for (int i=0; i<children.length; i++) {
                while (offset < psiElements.length &&
                        (psiElements[offset] instanceof PsiWhiteSpace ||
                         psiElements[offset] instanceof PsiErrorElement)) offset++;

                PsiElement psiElement = offset == psiElements.length ? null : psiElements[offset];
                if (psiElement!= null && psiElement instanceof BasePsiElement && children[i].getElementType() == ((BasePsiElement)psiElement).getElementType()) {
                    offset++;
                    if (offset == psiElements.length) {
                        boolean isLast = i == children.length-1;
                        return !isLast && !sequenceElementType.isOptionalFromIndex(i+1);
                    }
                } else {
                    if (!children[i].isOptional() && !(psiElement instanceof PsiWhiteSpace) && !(psiElement instanceof PsiComment)) {
                        return true;
                    }
                }
            }
        } else if (getElementType() instanceof IterationElementType) {
            IterationElementType iterationElementType = (IterationElementType) getElementType();
            PsiElement psiElement = getLastChild();
            if (psiElement == null) {
                return true;
            } else if (psiElement instanceof BasePsiElement){
                BasePsiElement basePsiElement = (BasePsiElement) psiElement;
                return basePsiElement.getElementType() != iterationElementType.getIteratedElementType();
            } else {
                return psiElement instanceof PsiErrorElement;
            }
        }*/
        return false;
    }

    public boolean isSequence(){
        return getElementType() instanceof SequenceElementType;
    }

    public boolean isBlock(){
        return getElementType() instanceof BlockElementType;
    }

    public boolean isIteration(){
        return getElementType() instanceof IterationElementType;
    }

    public boolean isOneOf() {
        return getElementType() instanceof OneOfElementType;
    }

    public boolean isNamedSequence() {
        return getElementType() instanceof NamedElementType;
    }

    public boolean isFirstChild(PsiElement psiElement){
         return psiElement == getFirstChild();
    }

    @Override
    public boolean matches(BasePsiElement basePsiElement, MatchType matchType) {
        PsiElement localChild = getFirstChild();
        PsiElement remoteChild = basePsiElement == null ? null : basePsiElement.getFirstChild();

        while(localChild != null && remoteChild != null) {
            if (localChild instanceof BasePsiElement && remoteChild instanceof BasePsiElement) {
                BasePsiElement localPsiElement = (BasePsiElement) localChild;
                BasePsiElement remotePsiElement = (BasePsiElement) remoteChild;
                if (!localPsiElement.matches(remotePsiElement, matchType)) {
                    return false;
                }
                localChild = PsiUtil.getNextSibling(localChild);
                remoteChild = PsiUtil.getNextSibling(remoteChild);
            } else {
                return false;
            }
        }
        return localChild == null && remoteChild == null;
    }


}
