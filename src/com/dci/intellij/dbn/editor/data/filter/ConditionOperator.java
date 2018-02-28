package com.dci.intellij.dbn.editor.data.filter;

import javax.swing.Icon;
import java.util.Date;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.ui.Presentable;

public enum ConditionOperator implements Presentable {
    EQUAL("=", false),
    NOT_EQUAL("!=", false),
    LESS("<", false),
    GREATER(">", false),
    LESS_EQUAL("<=", false),
    GREATER_EQUAL(">=", false),
    LIKE("like", false),
    NOT_LIKE("not like", false),
    BETWEEN("between", false),
    IN("in", false, "(", ")"),
    NOT_IN("not in", false, "(", ")"),
    IS_NULL("is null", true),
    IS_NOT_NULL("is not null", true);

    public static final ConditionOperator[] ALL_CONDITION_OPERATORS = new ConditionOperator[] {
            EQUAL,
            NOT_EQUAL,
            LESS,
            GREATER,
            LESS_EQUAL,
            GREATER_EQUAL,
            LIKE,
            NOT_LIKE,
            BETWEEN,
            IN,
            NOT_IN,
            IS_NULL,
            IS_NOT_NULL};

    public static final ConditionOperator[] NUMBER_CONDITION_OPERATORS = new ConditionOperator[] {
            EQUAL,
            NOT_EQUAL,
            LESS,
            GREATER,
            LESS_EQUAL,
            GREATER_EQUAL,
            BETWEEN,
            IN,
            NOT_IN,
            IS_NULL,
            IS_NOT_NULL};

    public static final ConditionOperator[] DATE_CONDITION_OPERATORS = new ConditionOperator[] {
            EQUAL,
            NOT_EQUAL,
            LESS,
            GREATER,
            LESS_EQUAL,
            GREATER_EQUAL,
            BETWEEN,
            IN,
            NOT_IN,
            IS_NULL,
            IS_NOT_NULL};

    public static final ConditionOperator[] STRING_CONDITION_OPERATORS = new ConditionOperator[] {
            EQUAL,
            NOT_EQUAL,
            LIKE,
            NOT_LIKE,
            IN,
            NOT_IN,
            IS_NULL,
            IS_NOT_NULL};


    public static ConditionOperator[] getConditionOperators (Class dataTypeClass) {
        if (dataTypeClass != null) {
            if (String.class.isAssignableFrom(dataTypeClass)) {
                return STRING_CONDITION_OPERATORS;
            }

            if (Number.class.isAssignableFrom(dataTypeClass)) {
                return NUMBER_CONDITION_OPERATORS;
            }

            if (Date.class.isAssignableFrom(dataTypeClass)) {
                return DATE_CONDITION_OPERATORS;
            }
        }

        return ALL_CONDITION_OPERATORS;
    }

    private String text;
    private boolean isFinal;
    private String valuePrefix;
    private String valuePostfix;

    ConditionOperator(String text, boolean isFinal) {
        this.text = text;
        this.isFinal = isFinal;
    }

    ConditionOperator(String text, boolean isFinal, String valuePrefix, String valuePostfix) {
        this.text = text;
        this.isFinal = isFinal;
        this.valuePrefix = valuePrefix;
        this.valuePostfix = valuePostfix;

    }

    public String getText() {
        return text;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public String getValuePostfix() {
        return valuePostfix;
    }

    public String getValuePrefix() {
        return valuePrefix;
    }

    public String toString() {
        return text;
    }

    @NotNull
    @Override
    public String getName() {
        return text;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return null;
    }

    public static ConditionOperator get(String text) {
        for (ConditionOperator operator : ConditionOperator.values()) {
            if (operator.text.equals(text)) return operator;
        }

        return null;
    }
}
