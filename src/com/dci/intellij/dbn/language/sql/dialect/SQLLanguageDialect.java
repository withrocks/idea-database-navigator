package com.dci.intellij.dbn.language.sql.dialect;

import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.DBLanguageDialectIdentifier;
import com.dci.intellij.dbn.language.sql.SQLFileElementType;
import com.dci.intellij.dbn.language.sql.SQLLanguage;
import com.intellij.psi.tree.IFileElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public abstract class SQLLanguageDialect extends DBLanguageDialect {
    public SQLLanguageDialect(@NonNls @NotNull DBLanguageDialectIdentifier identifier) {
        super(identifier, SQLLanguage.INSTANCE);
    }

    public IFileElementType createFileElementType() {
        return new SQLFileElementType(this);
    }

}
