package com.dci.intellij.dbn.language.common.psi;

import java.lang.ref.WeakReference;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.psi.PsiElement;

public class PsiResolveResult {
    private ConnectionHandlerRef activeConnection;
    private DBObjectRef<DBSchema> currentSchema;
    private WeakReference<IdentifierPsiElement> element;
    private WeakReference<BasePsiElement> parent;
    private WeakReference<PsiElement> referencedElement;
    private CharSequence text;
    private boolean isNew;
    private boolean isResolving;
    private boolean isConnectionValid;
    private boolean isConnectionActive;
    private long lastResolveInvocation = 0;
    private int executableTextLength;
    private int resolveTrials = 0;
    private int overallResolveTrials = 0;

    PsiResolveResult(IdentifierPsiElement element) {
        this.activeConnection = ConnectionHandlerRef.from(element.getActiveConnection());
        this.element = new WeakReference<IdentifierPsiElement>(element);
        this.isNew = true;
    }

    public void preResolve(IdentifierPsiElement psiElement) {
        this.isResolving = true;
        ConnectionHandler connectionHandler = psiElement.getActiveConnection();
        this.isConnectionValid = connectionHandler != null && !connectionHandler.isVirtual() && connectionHandler.getConnectionStatus().isValid();
        this.isConnectionActive = connectionHandler != null && !connectionHandler.isVirtual() && connectionHandler.canConnect();
        this.referencedElement = null;
        this.parent = null;
        this.text = psiElement.getUnquotedText();
        this.activeConnection = ConnectionHandlerRef.from(connectionHandler);
        this.currentSchema = DBObjectRef.from(psiElement.getCurrentSchema());
        this.executableTextLength = psiElement.getEnclosingScopePsiElement().getTextLength();
    }

    public void postResolve() {
        this.isNew = false;
        PsiElement referencedElement = this.referencedElement == null ? null : this.referencedElement.get();
        this.resolveTrials = referencedElement == null ? resolveTrials + 1 : 0;
        this.overallResolveTrials = referencedElement == null ? overallResolveTrials + 1 : 0;
        this.isResolving = false;
    }

    public boolean isResolving() {
        return isResolving;
    }

    boolean isDirty() {
        //if (isResolving) return false;
        if (isNew) return true;

        if (resolveTrials > 3 && lastResolveInvocation < System.currentTimeMillis() - 3000) {
            lastResolveInvocation = System.currentTimeMillis();
            resolveTrials = 0;
            return true;
        }

        if (connectionChanged()) {
            return true;
        }

        IdentifierPsiElement element = this.element.get();
        ConnectionHandler activeConnection = element == null ? null : element.getActiveConnection();
        if (activeConnection == null || activeConnection.isVirtual()) {
            if (currentSchema != null) return true;
        } else {
            if (connectionBecameActive(activeConnection) || connectionBecameValid(activeConnection) || currentSchemaChanged()) {
                return true;
            }
        }

        PsiElement referencedElement = this.referencedElement == null ? null : this.referencedElement.get();
        if (referencedElement == null &&
                resolveTrials > 3 &&
                !elementTextChanged() &&
                !enclosingExecutableChanged()) {
            return false;
        }

        if (referencedElement == null || !referencedElement.isValid() ||
                (element != null && !element.textMatches(referencedElement.getText()))) {
            return true;
        }

        BasePsiElement parent = getParent();
        if (parent != null) {
            if (!parent.isValid()) {
                return true;
            } else if (referencedElement instanceof DBObject) {
                DBObject object = (DBObject) referencedElement;
                if (object.getParentObject() != parent.resolveUnderlyingObject()) {
                    return true;
                }
            }
        } else {
            if (element != null && element.isPrecededByDot()) {
                return true;
            }
        }
        return false;
    }

    private BasePsiElement getParent() {
        return parent == null ? null : parent.get();
    }

    private boolean elementTextChanged() {
        IdentifierPsiElement element = this.element.get();
        return element!= null && !element.textMatches(text);
    }

    private boolean connectionChanged() {
        IdentifierPsiElement element = this.element.get();
        return element != null && getActiveConnection() != element.getActiveConnection();
    }

    private boolean currentSchemaChanged() {
        IdentifierPsiElement element = this.element.get();
        return element != null && !CommonUtil.safeEqual(DBObjectRef.get(currentSchema), element.getCurrentSchema());
    }

    private boolean connectionBecameValid(ConnectionHandler connectionHandler) {
        return !isConnectionValid && connectionHandler!= null && !connectionHandler.isVirtual() && connectionHandler.getConnectionStatus().isValid();
    }

    private boolean connectionBecameActive(ConnectionHandler connectionHandler) {
        return !isConnectionActive && connectionHandler!= null && !connectionHandler.isVirtual() && connectionHandler.canConnect();
    }

    private boolean enclosingExecutableChanged() {
        IdentifierPsiElement element = this.element.get();
        return element != null && executableTextLength != element.getEnclosingScopePsiElement().getTextLength();
    }

    public DBObjectType getObjectType() {
        PsiElement referencedElement = getReferencedElement();
        if (referencedElement instanceof DBObject) {
            DBObject object = (DBObject) referencedElement;
            return object.getObjectType();
        }
        if (referencedElement instanceof IdentifierPsiElement) {
            IdentifierPsiElement identifierPsiElement = (IdentifierPsiElement) referencedElement;
            return identifierPsiElement.getObjectType();
        }

        if (referencedElement instanceof BasePsiElement) {
            BasePsiElement basePsiElement = (BasePsiElement) referencedElement;
            DBObject object = basePsiElement.resolveUnderlyingObject();
            if (object != null) {
                return object.getObjectType();
            }
        }

        return null;
    }

    /*********************************************************
     *                   Getters/Setters                     *
     *********************************************************/

    public CharSequence getText() {
        return text;
    }

    public PsiElement getReferencedElement() {
        return this.referencedElement == null ? null : this.referencedElement.get();
    }

    public ConnectionHandler getActiveConnection() {
        return ConnectionHandlerRef.get(activeConnection);
    }

    public void setParent(@Nullable BasePsiElement parent) {
        this.parent = new WeakReference<BasePsiElement>(parent);
    }

    public void setReferencedElement(PsiElement referencedElement) {
        this.referencedElement = referencedElement == null ? null : new WeakReference<PsiElement>(referencedElement);
    }

    public int getOverallResolveTrials() {
        return overallResolveTrials;
    }
}
