package com.dci.intellij.dbn.editor.session.action;

import com.dci.intellij.dbn.editor.session.SessionBrowserFilterType;

public class FilterUsersComboBoxAction extends AbstractFilterComboBoxAction {

    public FilterUsersComboBoxAction() {
        super(SessionBrowserFilterType.USER);
    }
}