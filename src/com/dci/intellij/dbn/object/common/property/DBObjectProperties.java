package com.dci.intellij.dbn.object.common.property;

import java.util.EnumSet;
import java.util.Set;

public class DBObjectProperties {
    private Set<DBObjectProperty> properties;

    public boolean is(DBObjectProperty property) {
        return properties != null && properties.contains(property);
    }

    public void set(DBObjectProperty property) {
        if (properties == null) {
            properties = EnumSet.noneOf(DBObjectProperty.class);
        }
        properties.add(property);
    }

    public void unset(DBObjectProperty property) {
        if (properties != null) {
            properties.remove(property);
            if (properties.isEmpty()) {
                properties = null;
            }
        }
    }
}
