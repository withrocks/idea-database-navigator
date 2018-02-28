package com.dci.intellij.dbn.common.ui;

import com.dci.intellij.dbn.common.dispose.DisposableProjectComponent;

import javax.swing.JComponent;

public interface DBNForm extends DisposableProjectComponent {
    JComponent getComponent();
}
