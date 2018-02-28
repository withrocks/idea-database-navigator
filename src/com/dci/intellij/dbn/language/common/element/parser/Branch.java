package com.dci.intellij.dbn.language.common.element.parser;

public class Branch {
    String name;

    public Branch() {}

    public Branch(String def) {
        name = def;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Branch branch = (Branch) o;
        return name.equals(branch.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

}