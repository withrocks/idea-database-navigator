package com.dci.intellij.dbn.editor.data.state.column.ui;

import javax.swing.*;
import java.util.Comparator;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.list.Selectable;
import com.dci.intellij.dbn.editor.data.state.column.DatasetColumnState;
import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBDataset;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;

public class ColumnStateSelectable implements Selectable {
    public static final Comparator<ColumnStateSelectable> NAME_COMPARATOR = new Comparator<ColumnStateSelectable>() {
        @Override
        public int compare(ColumnStateSelectable o1, ColumnStateSelectable o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };

    public static final Comparator<ColumnStateSelectable> POSITION_COMPARATOR = new Comparator<ColumnStateSelectable>() {
        @Override
        public int compare(ColumnStateSelectable o1, ColumnStateSelectable o2) {
            return o1.getOriginalPosition() - o2.getOriginalPosition();
        }
    };

    private DatasetColumnState state;
    private DBObjectRef<DBDataset> datasetRef;

    public ColumnStateSelectable(DBDataset dataset, DatasetColumnState state) {
        this.datasetRef = DBObjectRef.from(dataset);
        this.state = state;
    }

    private DBDataset getDataset() {
        return DBObjectRef.get(datasetRef);
    }

    public DBColumn getColumn() {
        DBDataset dataset = getDataset();
        if (dataset != null) {
            return dataset.getColumn(state.getName());
        }
        return null;
    }

    @Override
    public Icon getIcon() {
        DBColumn column = getColumn();
        return column == null ? Icons.DBO_COLUMN : column.getIcon();
    }


    public int getOriginalPosition() {
        DBColumn column = getColumn();
        if (column != null) {
            return column.getPosition();
        }
        return 0;
    }

    @Override
    public String getName() {
        return state.getName();
    }

    @Override
    public String getError() {
        return null;
    }

    @Override
    public boolean isSelected() {
        return state.isVisible();
    }

    @Override
    public boolean isMasterSelected() {
        return true;
    }

    @Override
    public void setSelected(boolean selected) {
        state.setVisible(selected);
    }

    @Override
    public int compareTo(@NotNull Object o) {
        return 0;
    }

    public int getPosition() {
        return state.getPosition();
    }

    public void setPosition(int position) {
        state.setPosition(position);
    }
}
