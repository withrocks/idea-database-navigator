package com.dci.intellij.dbn.common.ui;

import java.util.List;

public class DBNComboBox<T extends Presentable> extends ValueSelector<T>{
    public DBNComboBox() {
        super(null, null, true);
    }

    public DBNComboBox(List<T> values) {
        super(null, null, values, null, true);
    }

}
