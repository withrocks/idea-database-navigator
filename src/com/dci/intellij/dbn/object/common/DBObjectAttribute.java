package com.dci.intellij.dbn.object.common;

import com.dci.intellij.dbn.common.util.NamingUtil;

public class DBObjectAttribute {
    private String name;
    private String friendlyName;
    public DBObjectAttribute(String name) {
        this.name = name;
        this.friendlyName = NamingUtil.createFriendlyName(name);
    }

    public String getName() {
        return name;
    }

    public String getFriendlyName() {
        return friendlyName;
    }
}
