package com.dci.intellij.dbn.language.common.element.util;

import java.util.EnumSet;
import java.util.Set;

public enum ElementTypeAttribute {
    
    ROOT("ROOT", "Executable statement"),
    EXECUTABLE("EXECUTABLE", "Executable statement"),
    TRANSACTIONAL("TRANSACTIONAL", "Transactional statement"),
    QUERY("QUERY", "Query statement", true),
    DATA_DEFINITION("DATA_DEFINITION", "Data definition statement", true),
    DATA_MANIPULATION("DATA_MANIPULATION", "Data manipulation statement", true),
    COMPILABLE_BLOCK("COMPILABLE_BLOCK", "Compilable block", false),
    TRANSACTION_CONTROL("TRANSACTION_CONTROL", "Transaction control statement", true),
    OBJECT_SPECIFICATION("OBJECT_SPECIFICATION", "Object specification", true),
    OBJECT_DECLARATION("OBJECT_DECLARATION", "Object declaration", true),
    OBJECT_DEFINITION("OBJECT_DEFINITION", "Object definition", true),
    SUBJECT("SUBJECT", "Statement subject"),
    STATEMENT("STATEMENT", "Statement"),
    CLAUSE("CLAUSE", "Statement clause"),
    CONDITION("CONDITION", "Condition expression"),
    STRUCTURE("STRUCTURE", "Structure view element"),
    SCOPE_ISOLATION("SCOPE_ISOLATION", "Scope isolation"),
    SCOPE_DEMARCATION("SCOPE_DEMARCATION", "Scope demarcation"),
    FOLDABLE_BLOCK("FOLDABLE_BLOCK", "Foldable block"),
    EXECUTABLE_CODE("EXECUTABLE_CODE", "Executable code"),
    BREAKPOINT_POSITION("BREAKPOINT_POSITION", "Default breakpoint position"),
    ACTION("ACTION", "Action"),
    GENERIC("GENERIC", "Generic element"),
    SPECIFIC("SPECIFIC", "Specific element"),
    DATABASE_LOG_PRODUCER("DATABASE_LOG_PRODUCER", "Database logging"),
    METHOD_PARAMETER_HANDLER("METHOD_PARAMETER_HANDLER", "Method parameter handler"),
    COLUMN_PARAMETER_HANDLER("COLUMN_PARAMETER_HANDLER", "Column parameter handler"),
    COLUMN_PARAMETER_PROVIDER("COLUMN_PARAMETER_PROVIDER", "Column parameter provider"),
    ;

    public static final Set<ElementTypeAttribute> EMPTY_LIST = EnumSet.noneOf(ElementTypeAttribute.class);

    private String name;
    private String description;
    private boolean specific;

    ElementTypeAttribute(String name, String description) {
        this(name, description, false);
    }

    ElementTypeAttribute(String name, String description, boolean specific) {
        this.name = name;
        this.description = description;
        this.specific = specific;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return name;
    }

    public boolean isSpecific() {
        return specific;
    }
}
