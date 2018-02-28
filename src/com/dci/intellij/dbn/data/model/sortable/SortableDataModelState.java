package com.dci.intellij.dbn.data.model.sortable;

import com.dci.intellij.dbn.data.model.DataModelState;
import com.dci.intellij.dbn.data.sorting.SortingState;

public class SortableDataModelState extends DataModelState {
    protected SortingState sortingState = new SortingState();

    public SortingState getSortingState() {
        return sortingState;
    }

    public void setSortingState(SortingState sortingState) {
        this.sortingState = sortingState;
    }

    /*****************************************************************
     *                     equals / hashCode                         *
     *****************************************************************/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SortableDataModelState that = (SortableDataModelState) o;
        return sortingState.equals(that.sortingState);

    }

    @Override
    public int hashCode() {
        return sortingState.hashCode();
    }
}
