package com.dci.intellij.dbn.language.common.structure;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.sql.structure.SQLStructureViewElement;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.TextEditorBasedStructureViewModel;
import com.intellij.ide.util.treeView.smartTree.Filter;
import com.intellij.ide.util.treeView.smartTree.Grouper;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;

public abstract class DBLanguageStructureViewModel extends TextEditorBasedStructureViewModel implements StructureViewModel.ElementInfoProvider {

    public DBLanguageStructureViewModel(Editor editor, PsiFile psiFile) {
        super(editor, psiFile);
    }

    @NotNull
    protected Class[] getSuitableClasses() {
        return new Class[] {BasePsiElement.class};
    }

    @NotNull
    public Grouper[] getGroupers() {
        return Grouper.EMPTY_ARRAY;
    }

    @NotNull
    public Sorter[] getSorters() {
        return Sorter.EMPTY_ARRAY;
    }

    @NotNull
    public Filter[] getFilters() {
        return Filter.EMPTY_ARRAY;
    }

    @Override
    public boolean isAlwaysShowsPlus(StructureViewTreeElement element) {
        return false;
    }

    @Override
    public boolean isAlwaysLeaf(StructureViewTreeElement element) {
        if (element instanceof SQLStructureViewElement) {
            SQLStructureViewElement sqlElement = (SQLStructureViewElement) element;
            if (sqlElement.getPsiElement() instanceof BasePsiElement) {
                BasePsiElement basePsiElement = (BasePsiElement) sqlElement.getPsiElement();
                BasePsiElement childStructureElement = basePsiElement.findFirstPsiElement(ElementTypeAttribute.STRUCTURE);
                if (childStructureElement == null) {
                    return true;
                }
            }
        }
        return false;
    }
}
