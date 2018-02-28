package com.dci.intellij.dbn.data.find.action;

import com.dci.intellij.dbn.data.find.DataSearchComponent;
import com.intellij.find.FindSettings;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class ToggleWholeWordsOnlyAction extends DataSearchHeaderToggleAction {
    public ToggleWholeWordsOnlyAction(DataSearchComponent searchComponent) {
        super(searchComponent, "W&hole Words");
    }

    @Override
    public boolean isSelected(AnActionEvent e) {
        return getEditorSearchComponent().getFindModel().isWholeWordsOnly();
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        e.getPresentation().setEnabled(!getEditorSearchComponent().getFindModel().isRegularExpressions());
        e.getPresentation().setVisible(true);
    }

    @Override
    public void setSelected(AnActionEvent e, boolean state) {
        FindSettings.getInstance().setLocalWholeWordsOnly(state);
        getEditorSearchComponent().getFindModel().setWholeWordsOnly(state);
    }
}
