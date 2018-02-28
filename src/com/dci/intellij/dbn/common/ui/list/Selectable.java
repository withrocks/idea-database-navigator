package com.dci.intellij.dbn.common.ui.list;

import javax.swing.Icon;

public interface Selectable<T> extends Comparable<T>{
    Icon getIcon();
    String getName();
    String getError();
    boolean isSelected();
    boolean isMasterSelected();
    void setSelected(boolean selected);
}
