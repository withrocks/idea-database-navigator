package com.dci.intellij.dbn.language.psql.structure;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.editor.structure.EmptyStructureViewModel;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder;
import com.intellij.lang.PsiStructureViewFactory;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiEditorUtil;

public class PSQLStructureViewBuilderFactory implements PsiStructureViewFactory {

    public StructureViewBuilder getStructureViewBuilder(final PsiFile psiFile) {
        return new TreeBasedStructureViewBuilder() {
            @NotNull
            public StructureViewModel createStructureViewModel() {
                try {
                    return psiFile == null || !psiFile.isValid() || psiFile.getProject().isDisposed() || PsiEditorUtil.Service.getInstance() == null ? EmptyStructureViewModel.INSTANCE : new PSQLStructureViewModel(null, psiFile);
                } catch (Throwable e) {
                    // TODO dirty workaround (compatibility issue)
                    return EmptyStructureViewModel.INSTANCE;
                }
            }

            @NotNull
            @Override
            public StructureViewModel createStructureViewModel(@Nullable Editor editor) {
                try {
                    return psiFile == null || !psiFile.isValid() || psiFile.getProject().isDisposed() || PsiEditorUtil.Service.getInstance() == null ? EmptyStructureViewModel.INSTANCE : new PSQLStructureViewModel(editor, psiFile);
                } catch (Throwable e) {
                    // TODO dirty workaround (compatibility issue)
                    return EmptyStructureViewModel.INSTANCE;
                }
            }
        };
    }
}