package com.dci.intellij.dbn.vfs;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.navigation.psi.NavigationPsiCache;
import com.dci.intellij.dbn.object.common.DBObject;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.SingleRootFileViewProvider;
import com.intellij.psi.impl.PsiDocumentManagerImpl;
import com.intellij.testFramework.LightVirtualFile;

public class DatabaseFileViewProvider extends SingleRootFileViewProvider {
    public DatabaseFileViewProvider(@NotNull PsiManager manager, @NotNull VirtualFile virtualFile, boolean eventSystemEnabled) {
        super(manager, virtualFile, eventSystemEnabled);
    }

    public DatabaseFileViewProvider(@NotNull PsiManager psiManager, @NotNull VirtualFile virtualFile, boolean eventSystemEnabled, @NotNull Language language) {
        super(psiManager, virtualFile, eventSystemEnabled, language);
    }

    @Override
    public boolean isPhysical() {
        return super.isPhysical();
    }

    @Override
    protected PsiFile getPsiInner(@NotNull Language language) {
        if (language instanceof DBLanguage || language instanceof DBLanguageDialect) {
            VirtualFile virtualFile = getVirtualFile();
            if (virtualFile instanceof DBObjectVirtualFile) {
                DBObjectVirtualFile objectFile = (DBObjectVirtualFile) virtualFile;
                DBObject object = objectFile.getObject();
                return NavigationPsiCache.getPsiFile(object);
            }

            Language baseLanguage = getBaseLanguage();
            PsiFile psiFile = super.getPsiInner(baseLanguage);
            if (psiFile == null) {
                DBParseableVirtualFile parseableFile = getParseableFile(virtualFile);
                if (parseableFile != null) {
                    parseableFile.initializePsiFile(this, language);
                }
            } else {
                forceCachedPsi(psiFile);
                Document document = DocumentUtil.getDocument(getVirtualFile());
                PsiDocumentManagerImpl.cachePsi(document, psiFile);
                return psiFile;
            }
        }

        return super.getPsiInner(language);
    }

    private static DBParseableVirtualFile getParseableFile(VirtualFile virtualFile) {
        if (virtualFile instanceof DBParseableVirtualFile) {
            return (DBParseableVirtualFile) virtualFile;
        }

        if (virtualFile instanceof LightVirtualFile) {
            LightVirtualFile lightVirtualFile = (LightVirtualFile) virtualFile;
            VirtualFile originalFile = lightVirtualFile.getOriginalFile();
            if (originalFile != null && !originalFile.equals(virtualFile)) {
                return getParseableFile(originalFile);
            }
        }
        return null;
    }

    @NotNull
    @Override
    public SingleRootFileViewProvider createCopy(@NotNull VirtualFile copy) {
        return new DatabaseFileViewProvider(getManager(), copy, false, getBaseLanguage());
    }

    @NotNull
    @Override
    public VirtualFile getVirtualFile() {
        VirtualFile virtualFile = super.getVirtualFile();
/*
        if (virtualFile instanceof SourceCodeFile)  {
            SourceCodeFile sourceCodeFile = (SourceCodeFile) virtualFile;
            return sourceCodeFile.getDatabaseFile();
        }
*/
        return virtualFile;
    }
}
