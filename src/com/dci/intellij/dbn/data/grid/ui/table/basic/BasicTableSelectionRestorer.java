package com.dci.intellij.dbn.data.grid.ui.table.basic;

import com.dci.intellij.dbn.common.ui.table.TableSelectionRestorer;

public class BasicTableSelectionRestorer implements TableSelectionRestorer {
    private boolean restoring = false;

    @Override
    public void snapshot() {

    }

    @Override
    public void restore() {

    }

    @Override
    public boolean isRestoring() {
        return restoring;
    }

    public void setRestoring(boolean restoring) {
        this.restoring = restoring;
    }
}
