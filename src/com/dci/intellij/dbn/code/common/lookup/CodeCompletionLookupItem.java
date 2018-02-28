package com.dci.intellij.dbn.code.common.lookup;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.code.common.completion.BasicInsertHandler;
import com.dci.intellij.dbn.code.common.completion.CodeCompletionContext;
import com.dci.intellij.dbn.code.common.completion.options.sorting.CodeCompletionSortingSettings;
import com.dci.intellij.dbn.common.util.NamingUtil;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.lookup.LookupItem;


public class CodeCompletionLookupItem extends LookupItem {
    public CodeCompletionLookupItem(LookupItemBuilder lookupItemBuilder, @NotNull String text, CodeCompletionContext completionContext) {
        super(lookupItemBuilder, NamingUtil.unquote(text));
        setIcon(lookupItemBuilder.getIcon());
        if (lookupItemBuilder.isBold()) setBold();
        setAttribute(LookupItem.TYPE_TEXT_ATTR, lookupItemBuilder.getTextHint());
        addLookupStrings(text.toUpperCase(), text.toLowerCase());
        setPresentableText(NamingUtil.unquote(text));
        CodeCompletionSortingSettings sortingSettings = completionContext.getCodeCompletionSettings().getSortingSettings();
        if (sortingSettings.isEnabled()) {
            setPriority(sortingSettings.getSortingIndexFor(lookupItemBuilder));
        }
    }

    public CodeCompletionLookupItem(Object source, Icon icon, @NotNull String text, String description, boolean bold, double sortPriority) {
        this(source, icon, text, description, bold);
        setPriority(sortPriority);
    }


    public CodeCompletionLookupItem(Object source, Icon icon, @NotNull String text, String description, boolean bold) {
        super(source, text);
        addLookupStrings(text.toUpperCase(), text.toLowerCase());
        setIcon(icon);
        if (bold) setBold();
        setAttribute(LookupItem.TYPE_TEXT_ATTR, description);
        setPresentableText(NamingUtil.unquote(text));
        setInsertHandler(BasicInsertHandler.INSTANCE);
    }

    @NotNull
    @Override
    public Object getObject() {
        return super.getObject();
    }

    @Override
    public InsertHandler getInsertHandler() {
        return super.getInsertHandler();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof CodeCompletionLookupItem) {
            CodeCompletionLookupItem lookupItem = (CodeCompletionLookupItem) o;
            return lookupItem.getLookupString().equals(getLookupString());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return getLookupString().hashCode();
    }

    @Override
    public boolean isValid() {
        return true;
    }
}
