package com.dci.intellij.dbn.editor.code;

public class SourceCodeOffsets {
    public static final String GUARDED_BLOCK_END_OFFSET_MARKER = "$$DBN_GUARDED_BLOCK_END_OFFSET$$";
    int guardedBlockEndOffset = 0;
    int headerEndOffset = 0;

    public int getGuardedBlockEndOffset() {
        return guardedBlockEndOffset;
    }

    public void setGuardedBlockEndOffset(int guardedBlockEndOffset) {
        this.guardedBlockEndOffset = guardedBlockEndOffset;
    }

    public int getHeaderEndOffset() {
        return headerEndOffset;
    }

    public void setHeaderEndOffset(int headerEndOffset) {
        this.headerEndOffset = headerEndOffset;
    }
}
