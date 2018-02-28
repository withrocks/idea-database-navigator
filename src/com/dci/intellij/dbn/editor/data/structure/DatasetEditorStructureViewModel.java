package com.dci.intellij.dbn.editor.data.structure;

import com.dci.intellij.dbn.common.editor.structure.DBObjectStructureViewModel;
import com.dci.intellij.dbn.editor.data.DatasetEditor;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DatasetEditorStructureViewModel extends DBObjectStructureViewModel {
    private Sorter[] sorters = new Sorter[] {new DatasetEditorStructureViewModelSorter()};
    private DatasetEditor datasetEditor;
    private StructureViewTreeElement root;

    public DatasetEditorStructureViewModel(DatasetEditor datasetEditor) {
        this.datasetEditor = datasetEditor;

    }

    @NotNull
    @Override
    public Sorter[] getSorters() {
        return sorters;
    }

    @Nullable
    public Object getCurrentEditorElement() {
        return null;
    }

    @NotNull
    public StructureViewTreeElement getRoot() {
        if (root == null) {
            //DBObjectBundle objectBundle = datasetEditor.getConnectionHandler().getObjectBundle();
            root = new DatasetEditorStructureViewElement(datasetEditor.getDataset(), datasetEditor);
        }
        return root;
    }
}
