package com.dci.intellij.dbn.language.sql;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;

public class SQLFile extends DBLanguagePsiFile {
    public SQLFile(FileViewProvider fileViewProvider, @NotNull SQLLanguage language) {
        super(fileViewProvider, SQLFileType.INSTANCE, language);
    }

    private SQLFile(Project project, @NotNull SQLLanguage language) {
        super(project, SQLFileType.INSTANCE, language);
    }

    public static SQLFile createEmptyFile(Project project){
        return new SQLFile(project, SQLLanguage.INSTANCE);
    }
}