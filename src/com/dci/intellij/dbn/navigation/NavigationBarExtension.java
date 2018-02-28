package com.dci.intellij.dbn.navigation;

import java.util.Collection;
import java.util.Collections;

import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.navigation.psi.DBConnectionPsiDirectory;
import com.dci.intellij.dbn.navigation.psi.DBObjectPsiDirectory;
import com.dci.intellij.dbn.navigation.psi.DBObjectPsiFile;
import com.dci.intellij.dbn.navigation.psi.NavigationPsiCache;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.vfs.DBVirtualFile;
import com.intellij.ide.navigationToolbar.NavBarModelExtension;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;

public class NavigationBarExtension implements NavBarModelExtension {
    public String getPresentableText(Object object) {
        if (object instanceof DBObject) {
            DBObject dbObject = (DBObject) object;
            return dbObject.getName();
        }
        return null;
    }

    public PsiElement getParent(PsiElement psiElement) {
        if (psiElement instanceof DBObjectPsiFile || psiElement instanceof DBObjectPsiDirectory || psiElement instanceof DBConnectionPsiDirectory) {
            return psiElement.getParent();
        }
        return null;
    }

    public PsiElement adjustElement(PsiElement psiElement) {
        if (psiElement instanceof DBLanguagePsiFile) {
            DBLanguagePsiFile databaseFile = (DBLanguagePsiFile) psiElement;
            VirtualFile virtualFile = databaseFile.getVirtualFile();
            if (virtualFile instanceof DBVirtualFile) {
                DBObject object = databaseFile.getUnderlyingObject();
                if (object != null) {
                    return NavigationPsiCache.getPsiFile(object);
                }
            }
        }
        return psiElement;
    }

    public Collection<VirtualFile> additionalRoots(Project project) {
        return Collections.emptyList();
    }
}
