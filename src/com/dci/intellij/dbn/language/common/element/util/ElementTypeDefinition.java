package com.dci.intellij.dbn.language.common.element.util;

public enum ElementTypeDefinition {

    SEQUENCE("sequence"),
    ITERATION("iteration"),
    QUALIFIED_IDENTIFIER("qualified-identifier"),
    ONE_OF("one-of"),
    TOKEN("token"),
    ELEMENT("element"),
    WRAPPER("wrapper"),
    OBJECT_DEF("object-def"),
    OBJECT_REF("object-ref"),
    ALIAS_DEF("alias-def"),
    ALIAS_REF("alias-ref"),
    VARIABLE_DEF("variable-def"),
    VARIABLE_REF("variable-ref"),
    EXEC_VARIABLE("exec-variable"),
    BLOCK("block");

    ElementTypeDefinition(String name) {
        this.name = name;
    }

    private String name;

    public String getName() {
        return name;
    }

    public boolean is(String name) {
        return this.name.equals(name);
    }
}

