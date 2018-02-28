package com.dci.intellij.dbn.data.type;

public enum GenericDataType {
    LITERAL("Literal"),
    NUMERIC("Numeric"),
    DATE_TIME("Date/Time"),
    CLOB("Character Large Object"),
    BLOB("Byte Large Object"),
    ROWID("Row ID"),
    FILE("File"),
    BOOLEAN("Boolean"),
    CURSOR("Cursor"),
    OBJECT("Object"),
    ARRAY("Array"),
    XMLTYPE("XML Type"),
    ;

    private String name;

    private GenericDataType(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public boolean is(GenericDataType... genericDataTypes) {
        for (GenericDataType genericDataType : genericDataTypes) {
            if (this == genericDataType) return true;
        }
        return false;
    }

    public boolean isLOB() {
        return is(BLOB, CLOB, XMLTYPE);
    }
}
