package com.dci.intellij.dbn.code.common.completion.options.filter.ui;

import com.dci.intellij.dbn.code.common.completion.options.filter.CodeCompletionFilterSettings;
import com.dci.intellij.dbn.code.common.completion.options.filter.CodeCompletionFilterOption;
import com.dci.intellij.dbn.code.common.completion.options.filter.CodeCompletionFilterOptionBundle;
import com.intellij.ui.CheckboxTree;
import com.intellij.ui.CheckedTreeNode;
import com.intellij.ui.SimpleTextAttributes;

import javax.swing.JTree;
import javax.swing.Icon;

public class CodeCompletionFilterTreeCellRenderer extends CheckboxTree.CheckboxTreeCellRenderer { //implements TreeCellEditor {
    public static final CodeCompletionFilterTreeCellRenderer CELL_RENDERER = new CodeCompletionFilterTreeCellRenderer();

    public void customizeCellRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        CheckedTreeNode node = (CheckedTreeNode) value;
        Object userObject = node.getUserObject();

        if (userObject instanceof CodeCompletionFilterOptionBundle) {
            CodeCompletionFilterOptionBundle optionBundle = (CodeCompletionFilterOptionBundle) userObject;
            getTextRenderer().append(optionBundle.getName(), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
        }
        else if(userObject instanceof CodeCompletionFilterOption) {
            CodeCompletionFilterOption option = (CodeCompletionFilterOption) userObject;
            Icon icon = option.getIcon();
            getTextRenderer().append(option.getName(), icon == null ?
                    SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES :
                    SimpleTextAttributes.REGULAR_ATTRIBUTES);
            getTextRenderer().setIcon(icon);
        }
        else if (userObject instanceof CodeCompletionFilterSettings){
            CodeCompletionFilterSettings codeCompletionFilterSettings = (CodeCompletionFilterSettings) userObject;
            getTextRenderer().append(codeCompletionFilterSettings.getDisplayName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
        }
    }
}

