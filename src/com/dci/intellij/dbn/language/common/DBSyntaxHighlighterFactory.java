package com.dci.intellij.dbn.language.common;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.PlainSyntaxHighlighterFactory;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class DBSyntaxHighlighterFactory extends SyntaxHighlighterFactory {
    @NotNull
    @Override
    public SyntaxHighlighter getSyntaxHighlighter(Project project, VirtualFile virtualFile) {
        if (virtualFile != null && virtualFile.getFileType() instanceof DBLanguageFileType) {
            DBLanguageFileType fileType = (DBLanguageFileType) virtualFile.getFileType();
            DBLanguage language = (DBLanguage) fileType.getLanguage();
            if (project != null) {
                ConnectionHandler connectionHandler = FileConnectionMappingManager.getInstance(project).getActiveConnection(virtualFile);
                DBLanguageDialect languageDialect = connectionHandler == null ?
                        language.getMainLanguageDialect() :
                        connectionHandler.getLanguageDialect(language);

                return languageDialect.getSyntaxHighlighter();
            }
        }

        return PlainSyntaxHighlighterFactory.getSyntaxHighlighter(Language.ANY, project, virtualFile);
    }
}
