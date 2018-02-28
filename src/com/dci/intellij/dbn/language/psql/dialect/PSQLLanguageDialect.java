package com.dci.intellij.dbn.language.psql.dialect;

import com.dci.intellij.dbn.language.common.ChameleonTokenType;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.DBLanguageDialectIdentifier;
import com.dci.intellij.dbn.language.psql.PSQLFileElementType;
import com.dci.intellij.dbn.language.psql.PSQLLanguage;
import com.intellij.psi.tree.IFileElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public abstract class PSQLLanguageDialect extends DBLanguageDialect {
    public PSQLLanguageDialect(@NonNls @NotNull DBLanguageDialectIdentifier identifier) {
        super(identifier, PSQLLanguage.INSTANCE);
    }

    @Override
    protected Set<ChameleonTokenType> createChameleonTokenTypes() {return null;}

    public IFileElementType createFileElementType() {
        return new PSQLFileElementType(this);
    }
}