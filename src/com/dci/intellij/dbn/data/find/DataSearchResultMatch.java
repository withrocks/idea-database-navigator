package com.dci.intellij.dbn.data.find;

import com.dci.intellij.dbn.data.model.DataModelCell;
import com.intellij.openapi.Disposable;

public class DataSearchResultMatch implements Disposable {
    private DataModelCell cell;
    private int startOffset;
    private int endOffset;

    public DataSearchResultMatch(DataModelCell cell, int startOffset, int endOffset) {
        this.cell = cell;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    public DataModelCell getCell() {
        return cell;
    }

    public int getStartOffset() {
        return startOffset;
    }

    public int getEndOffset() {
        return endOffset;
    }

    @Override
    public void dispose() {
        cell = null;
    }
}
