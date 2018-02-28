package com.dci.intellij.dbn.code.common.intention;

import java.lang.ref.WeakReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.element.util.IdentifierCategory;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.dci.intellij.dbn.language.common.psi.lookup.ObjectLookupAdapter;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;

public abstract class AbstractMethodExecutionIntentionAction extends GenericIntentionAction {
    private WeakReference<DBMethod> lastChecked;
    public static final ObjectLookupAdapter METHOD_LOOKUP_ADAPTER = new ObjectLookupAdapter(null, IdentifierCategory.DEFINITION, DBObjectType.METHOD);

    @NotNull
    public final String getText() {
        DBMethod method = getMethod();
        if (method != null) {
            DBObjectType objectType = method.getObjectType();
            if (objectType.matches(DBObjectType.PROCEDURE)) objectType = DBObjectType.PROCEDURE;
            if (objectType.matches(DBObjectType.FUNCTION)) objectType = DBObjectType.FUNCTION;
            return getActionName() + ' ' + objectType.getName() + ' ' + method.getName();
        }
        return getActionName() + " method";
    }

    protected abstract String getActionName();

    @Nullable
    protected DBMethod resolveMethod(Editor editor, PsiFile psiFile) {
        if (psiFile instanceof DBLanguagePsiFile) {
            DBLanguagePsiFile dbLanguagePsiFile = (DBLanguagePsiFile) psiFile;
            DBObject underlyingObject = dbLanguagePsiFile.getUnderlyingObject();

            if (underlyingObject != null) {
                if (underlyingObject instanceof DBMethod) {
                    DBMethod method = (DBMethod) underlyingObject;
                    lastChecked = new WeakReference<DBMethod>(method);
                    return method;
                }

                if (underlyingObject.getObjectType().isParentOf(DBObjectType.METHOD) && editor != null) {
                    BasePsiElement psiElement = PsiUtil.lookupLeafAtOffset(psiFile, editor.getCaretModel().getOffset());
                    if (psiElement != null) {
                        BasePsiElement methodPsiElement = METHOD_LOOKUP_ADAPTER.findInParentScopeOf(psiElement);
                        if (methodPsiElement instanceof IdentifierPsiElement) {
                            IdentifierPsiElement identifierPsiElement = (IdentifierPsiElement) methodPsiElement;
                            DBObject object = identifierPsiElement.resolveUnderlyingObject();
                            if (object instanceof DBMethod) {
                                DBMethod method = (DBMethod) object;
                                lastChecked = new WeakReference<DBMethod>(method);
                                return method;
                            }

                        }
                    }
                }
            }
        }
        lastChecked = null;
        return null;
    }

    @Nullable
    protected DBMethod getMethod() {
        return lastChecked == null ? null : lastChecked.get();
    }

    @NotNull
    public String getFamilyName() {
        return "Method execution intentions";
    }
}
