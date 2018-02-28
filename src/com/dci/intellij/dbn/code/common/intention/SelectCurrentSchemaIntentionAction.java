package com.dci.intellij.dbn.code.common.intention;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.vfs.DBConsoleVirtualFile;
import com.intellij.codeInsight.intention.LowPriorityAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;

public class SelectCurrentSchemaIntentionAction extends GenericIntentionAction implements LowPriorityAction {
    @NotNull
    public String getText() {
        return "Select current schema...";
    }

    @NotNull
    public String getFamilyName() {
        return "Connection intentions";
    }

    public Icon getIcon(int flags) {
        return Icons.FILE_SCHEMA_MAPPING;
    }

    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
        VirtualFile virtualFile = psiFile.getVirtualFile();
        if (psiFile instanceof DBLanguagePsiFile && virtualFile != null && (virtualFile.isInLocalFileSystem() || virtualFile instanceof DBConsoleVirtualFile) ) {
            DBLanguagePsiFile file = (DBLanguagePsiFile) psiFile;
            return file.getActiveConnection() != null && !file.getActiveConnection().isVirtual();
        }
        return false;
    }

    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        if (psiFile instanceof DBLanguagePsiFile) {
            DBLanguagePsiFile dbLanguageFile = (DBLanguagePsiFile) psiFile;
            FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(project);
            connectionMappingManager.promptSchemaSelector(dbLanguageFile, null);
        }
    }

    public boolean startInWriteAction() {
        return false;
    }
}
