package com.dci.intellij.dbn.data.model.sortable;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.data.model.basic.BasicDataModelRow;
import com.dci.intellij.dbn.data.sorting.SortingInstruction;
import com.dci.intellij.dbn.data.sorting.SortingState;

public class SortableDataModelRow<T extends SortableDataModelCell> extends BasicDataModelRow<T> implements Comparable {

    protected SortableDataModelRow(SortableDataModel model) {
        super(model);
    }

    @Override
    public SortableDataModel getModel() {
        return (SortableDataModel) super.getModel();
    }

    @Override
    public T getCellAtIndex(int index) {
        return super.getCellAtIndex(index);
    }

    public int compareTo(@NotNull Object o) {
        SortableDataModelRow row = (SortableDataModelRow) o;
        SortableDataModel model = getModel();
        SortingState sortingState = model.getSortingState();

        for (SortingInstruction sortingInstruction : sortingState.getSortingInstructions()) {
            int columnIndex = model.getColumnIndex(sortingInstruction.getColumnName());

            SortableDataModelCell local = getCellAtIndex(columnIndex);
            SortableDataModelCell remote = row.getCellAtIndex(columnIndex);

            int compareIndex = sortingInstruction.getDirection().getCompareAdj();

            int result =
                    remote == null && local == null ? 0 :
                    local == null ? -compareIndex :
                    remote == null ? columnIndex :
                    compareIndex * local.compareTo(remote);

            if (result != 0) return result;
        }
        return 0;


/*
        int index = model.getSortColumnIndex();

        if (index == -1) return 0;
        SortableDataModelRow row = (SortableDataModelRow) o;

        SortableDataModelCell local = getCellAtIndex(index);
        SortableDataModelCell remote = row.getCellAtIndex(index);

        int compareIndex = model.getSortDirection().getCompareIndex();

        if (remote == null && local == null) return 0;
        if (local == null) return -compareIndex;
        if (remote == null) return compareIndex;

        return compareIndex * local.compareTo(remote);
*/
    }

}
