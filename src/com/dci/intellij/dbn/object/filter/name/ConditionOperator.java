package com.dci.intellij.dbn.object.filter.name;

public enum ConditionOperator {
    EQUAL("equal", false),
    NOT_EQUAL("not equal", false),
    LIKE("like", true),
    NOT_LIKE("not like", true);

    private String text;
    private boolean allowsWildcards;

    ConditionOperator(String text, boolean allowsWildcards) {
        this.text = text;
        this.allowsWildcards = allowsWildcards;
    }

    public String getText() {
        return text;
    }

    public boolean allowsWildcards() {
        return allowsWildcards;
    }

    @Override
    public String toString() {
        return text;
    }
}
