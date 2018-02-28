package com.dci.intellij.dbn.common.action;

import javax.swing.Icon;
import java.awt.Component;
import java.awt.Point;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;

public abstract class GroupPopupAction extends DumbAwareAction {
    private String groupTitle;
    public GroupPopupAction(String name, @Nullable String groupTitle, @Nullable Icon icon) {
        super(name, null, icon);
        this.groupTitle = groupTitle;
    }

    public final void actionPerformed(@NotNull AnActionEvent e) {
        DefaultActionGroup actionGroup = new DefaultActionGroup();

        for (AnAction action : getActions(e)) {
            actionGroup.add(action);
        }
        ListPopup popup = JBPopupFactory.getInstance().createActionGroupPopup(
                groupTitle,
                actionGroup,
                e.getDataContext(),
                JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                true, null, 10);

        //Project project = (Project) e.getDataContext().getData(DataConstants.PROJECT);
        Component component = (Component) e.getInputEvent().getSource();
        showBelowComponent(popup, component);
    }

    private static void showBelowComponent(ListPopup popup, Component component) {
        Point locationOnScreen = component.getLocationOnScreen();
        Point location = new Point(
                (int) (locationOnScreen.getX() + 10),
                (int) locationOnScreen.getY() + component.getHeight());
        popup.showInScreenCoordinates(component, location);
    }

    protected abstract AnAction[] getActions(AnActionEvent e);
}
