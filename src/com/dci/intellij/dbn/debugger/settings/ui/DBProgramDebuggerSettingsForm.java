package com.dci.intellij.dbn.debugger.settings.ui;

import com.dci.intellij.dbn.common.ui.DBNFormImpl;

import javax.swing.JPanel;

public class DBProgramDebuggerSettingsForm extends DBNFormImpl {
    private JPanel mainPanel;

    public JPanel getComponent() {
        return mainPanel;
    }

    public void dispose() {
        super.dispose();
    }
}
