package com.dci.intellij.dbn.editor.code;

public class SourceCodeContent {
    private String sourceCode;
    private SourceCodeOffsets offsets = new SourceCodeOffsets();

    public SourceCodeContent(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public SourceCodeOffsets getOffsets() {
        return offsets;
    }
}
