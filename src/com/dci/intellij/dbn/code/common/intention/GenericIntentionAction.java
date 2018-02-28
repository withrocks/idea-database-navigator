package com.dci.intellij.dbn.code.common.intention;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiFile;

public abstract class GenericIntentionAction implements IntentionAction, Iconable {

    @NotNull
    public String getFamilyName() {
        return "DBNavigator intentions";
    }

    @Nullable
    protected ConnectionHandler getConnectionHandler(PsiFile psiFile) {
        if (psiFile instanceof DBLanguagePsiFile) {
            DBLanguagePsiFile dbLanguagePsiFile = (DBLanguagePsiFile) psiFile;
            return dbLanguagePsiFile.getActiveConnection();
        }
        return null;
    }
}
