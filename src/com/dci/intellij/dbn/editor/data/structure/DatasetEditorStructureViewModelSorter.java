package com.dci.intellij.dbn.editor.data.structure;

import javax.swing.*;
import java.util.Comparator;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.language.psql.structure.PSQLStructureViewElement;
import com.intellij.ide.util.treeView.smartTree.ActionPresentation;
import com.intellij.ide.util.treeView.smartTree.Sorter;

public class DatasetEditorStructureViewModelSorter implements Sorter {

    public Comparator getComparator() {
        return COMPARATOR;    
    }

    public boolean isVisible() {
        return true;
    }

    @NotNull
    public ActionPresentation getPresentation() {
        return ACTION_PRESENTATION;
    }

    @NotNull
    public String getName() {
        return "Sort by Name";
    }

    private static final ActionPresentation ACTION_PRESENTATION = new ActionPresentation() {
        @NotNull
        public String getText() {
            return "Sort by Name";
        }

        public String getDescription() {
            return "Sort elements alphabetically by name";
        }

        public Icon getIcon() {
            return Icons.ACTION_SORT_ALPHA;
        }
    };

    private static final Comparator COMPARATOR = new Comparator() {
        public int compare(Object object1, Object object2) {
            if (object1 instanceof DatasetEditorStructureViewElement && object2 instanceof DatasetEditorStructureViewElement) {
                DatasetEditorStructureViewElement structureViewElement1 = (DatasetEditorStructureViewElement) object1;
                DatasetEditorStructureViewElement structureViewElement2 = (DatasetEditorStructureViewElement) object2;
                BrowserTreeNode treeNode1 = structureViewElement1.getValue();
                BrowserTreeNode treeNode2 = structureViewElement2.getValue();
                return treeNode1.getName().compareTo(treeNode2.getName());
            } else {
                return object1 instanceof PSQLStructureViewElement ? 1 : -1;
            }
        }
    };
}
