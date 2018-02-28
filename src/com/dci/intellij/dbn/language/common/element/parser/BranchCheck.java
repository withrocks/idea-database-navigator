package com.dci.intellij.dbn.language.common.element.parser;

public class BranchCheck extends Branch{
    double version = 0;
    private Type type;

    public double getVersion() {
        return version;
    }

    public boolean check(Branch branch, double currentVersion) {
        switch (type) {
            case ALLOWED: return name.equals(branch.getName()) && currentVersion >= version;
            case FORBIDDEN: return !name.equals(branch.getName()) || currentVersion < version;
        }
        return true;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return name + "@" + version;
    }

    public enum Type {
        ALLOWED,
        FORBIDDEN
    }

    public BranchCheck(String def) {
        int startIndex = 0;
        if (def.startsWith("-")) {
            type = Type.FORBIDDEN;
            startIndex = 1;
        } else if (def.startsWith("+")) {
            type = Type.ALLOWED;
            startIndex = 1;
        }

        int atIndex = def.indexOf("@", startIndex);
        if (atIndex > -1) {
            name = def.substring(startIndex, atIndex).trim();
            version = Double.parseDouble(def.substring(atIndex + 1));
        } else {
            name = def.substring(startIndex).trim();
        }
    }
}
