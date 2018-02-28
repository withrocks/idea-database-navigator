package com.dci.intellij.dbn.code.common.lookup;

import javax.swing.Icon;

import com.dci.intellij.dbn.code.common.completion.CodeCompletionContext;
import com.dci.intellij.dbn.code.common.completion.CodeCompletionLookupConsumer;

public class BasicLookupItemBuilder extends LookupItemBuilder {
    private CharSequence text;
    String hint;
    Icon icon;

    public BasicLookupItemBuilder(CharSequence text, String hint, Icon icon) {
        this.text = text;
        this.hint = hint;
        this.icon = icon;
    }

    public String getTextHint() {
        return hint;
    }

    @Override
    public CodeCompletionLookupItem createLookupItem(Object source, CodeCompletionLookupConsumer consumer) {
        return super.createLookupItem(source, consumer);
    }

    public boolean isBold() {
        return false;
    }

    @Override
    public CharSequence getText(CodeCompletionContext completionContext) {
        return text;
    }

    public Icon getIcon() {
        return icon;
    }

    @Override
    public void dispose() {
    }
}