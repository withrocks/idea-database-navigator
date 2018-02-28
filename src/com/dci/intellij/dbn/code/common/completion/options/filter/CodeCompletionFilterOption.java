package com.dci.intellij.dbn.code.common.completion.options.filter;

import javax.swing.*;
import org.jdom.Element;

import com.dci.intellij.dbn.code.common.completion.options.filter.ui.CheckedTreeNodeProvider;
import com.dci.intellij.dbn.code.common.completion.options.filter.ui.CodeCompletionFilterTreeNode;
import com.dci.intellij.dbn.common.options.PersistentConfiguration;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.language.common.TokenTypeCategory;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.ui.CheckedTreeNode;

public class CodeCompletionFilterOption implements CheckedTreeNodeProvider, PersistentConfiguration{
    private CodeCompletionFilterSettings filterSettings;
    private DBObjectType objectType;
    private TokenTypeCategory tokenTypeCategory = TokenTypeCategory.UNKNOWN;
    private boolean selected;

    public CodeCompletionFilterOption(CodeCompletionFilterSettings filterSettings) {
        this.filterSettings = filterSettings;
    }

    public CodeCompletionFilterSettings getFilterSettings() {
        return filterSettings;
    }

    public DBObjectType getObjectType() {
        return objectType;
    }

    public TokenTypeCategory getTokenTypeCategory() {
        return tokenTypeCategory;
    }

    public String getName() {
        return objectType == null ?
                tokenTypeCategory.getName() :
                objectType.getName().toUpperCase();
    }

    public Icon getIcon() {
        return objectType == null ? null : objectType.getIcon();
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public void readConfiguration(Element element) {
        if (element != null) {
            String filterElementType = element.getAttributeValue("type");
            if (filterElementType.equals("OBJECT")) {
                String objectTypeName = element.getAttributeValue("id");
                objectType = DBObjectType.getObjectType(objectTypeName);
            } else {
                String tokenTypeName = element.getAttributeValue("id");
                tokenTypeCategory = TokenTypeCategory.getCategory(tokenTypeName);
            }
            selected = SettingsUtil.getBooleanAttribute(element, "selected", selected);
        }

    }

    public void writeConfiguration(Element element) {
        if (objectType != null) {
            element.setAttribute("type", "OBJECT");
            element.setAttribute("id", objectType.getName());

        } else {
            element.setAttribute("type", "RESERVED_WORD");
            element.setAttribute("id", tokenTypeCategory.getName());
        }

        SettingsUtil.setBooleanAttribute(element, "selected", selected);
    }

    public CheckedTreeNode createCheckedTreeNode() {
        return new CodeCompletionFilterTreeNode(this, selected);
    }

    public boolean equals(Object o) {
        CodeCompletionFilterOption option = (CodeCompletionFilterOption) o;
        return
            option.objectType == objectType &&
            option.tokenTypeCategory == tokenTypeCategory;
    }
}
