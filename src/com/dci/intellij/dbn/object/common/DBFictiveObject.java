package com.dci.intellij.dbn.object.common;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.IncorrectOperationException;

public class DBFictiveObject extends DBObjectImpl implements PsiReference {
    private DBObjectType objectType;
    public DBFictiveObject(DBObjectType objectType, String name) {
        super(null, name);
        this.objectType = objectType;
    }

    public boolean isValid() {
        return true;
    }

    @Override
    protected void initObject(ResultSet resultSet) throws SQLException {
    }

    public String getQualifiedNameWithType() {
        return getName();
    }

    @Nullable
    public ConnectionHandler getConnectionHandler() {
        return null;
    }

    public DBObjectType getObjectType() {
        return objectType;
    }

    @NotNull
    public List<BrowserTreeNode> buildAllPossibleTreeChildren() {
        return EMPTY_TREE_NODE_LIST;
    }

    public void navigate(boolean requestFocus) {

    }

    /*********************************************************
     *                       PsiReference                    *
     *********************************************************/
    public PsiElement getElement() {
        return null;
    }

    public TextRange getRangeInElement() {
        return new TextRange(0, getTextLength());
    }

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

    @NotNull
    public Object[] getVariants() {
        return new Object[0];
    }

    public boolean isSoft() {
        return false;
    }

}
