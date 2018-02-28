package com.dci.intellij.dbn.language.psql.template;

import com.intellij.codeInsight.template.impl.DefaultLiveTemplatesProvider;
import org.jetbrains.annotations.Nullable;

public class PSQLTemplateProvider implements DefaultLiveTemplatesProvider {

    public static final String[] TEMPLATES = new String[]{"com/dci/intellij/dbn/language/psql/template/default"};

    @Override
    public String[] getDefaultLiveTemplateFiles() {
        return TEMPLATES;
    }

    @Nullable
    @Override
    public String[] getHiddenLiveTemplateFiles() {
        return null;
    }
}
