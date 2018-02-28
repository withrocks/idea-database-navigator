package com.dci.intellij.dbn.common.option;

import com.dci.intellij.dbn.common.ui.Presentable;

public interface InteractiveOption extends Presentable{
    boolean isCancel();

    boolean isAsk();
}
