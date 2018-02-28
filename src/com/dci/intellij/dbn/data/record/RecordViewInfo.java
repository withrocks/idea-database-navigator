package com.dci.intellij.dbn.data.record;

import javax.swing.Icon;

public class RecordViewInfo {
    String title;
    Icon icon;

    public RecordViewInfo(String title, Icon icon) {
        this.title = title;
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public Icon getIcon() {
        return icon;
    }
}
