package com.dci.intellij.dbn.language.common.element.parser;

import com.dci.intellij.dbn.language.common.element.path.ParsePathNode;
import com.intellij.lang.PsiBuilder;

public class TokenPairRangeMarker {
    private ParsePathNode parseNode;
    private PsiBuilder.Marker marker;
    private int offset;
    private boolean explicit;

    public TokenPairRangeMarker(ParsePathNode parseNode, PsiBuilder builder, boolean explicit) {
        this.parseNode = parseNode;
        this.offset = builder.getCurrentOffset();
        this.explicit = explicit;
    }

    public ParsePathNode getParseNode() {
        return parseNode;
    }

    public int getOffset() {
        return offset;
    }

    public void dropMarker() {
        if (marker != null) {
            marker.drop();
        }
    }

    public boolean isExplicit() {
        return explicit;
    }

    public void setExplicit(boolean explicit) {
        this.explicit = explicit;
    }

    @Override
    public String toString() {
        return offset + " " + explicit + " " + (marker != null);
    }
}
