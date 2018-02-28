package com.dci.intellij.dbn.language.psql;

import com.dci.intellij.dbn.language.common.DBLanguageBraceMatcher;

public class PSQLLanguageBraceMatcher extends DBLanguageBraceMatcher {
    public PSQLLanguageBraceMatcher() {
        super(PSQLLanguage.INSTANCE);
    }
}