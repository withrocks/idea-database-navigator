package com.dci.intellij.dbn.editor.session.action;

import com.dci.intellij.dbn.editor.session.SessionBrowserFilterType;

public class FilterHostsComboBoxAction extends AbstractFilterComboBoxAction {

    public FilterHostsComboBoxAction() {
        super(SessionBrowserFilterType.HOST);
    }
}