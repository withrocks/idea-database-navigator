package com.dci.intellij.dbn.object.action;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Point;
import java.util.List;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.browser.ui.DatabaseBrowserTree;
import com.dci.intellij.dbn.common.Colors;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.object.common.DBObject;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.ui.tree.TreeUtil;

public abstract class ObjectListShowAction extends AnAction {
    protected DBObject sourceObject;
    protected RelativePoint popupLocation;

    public ObjectListShowAction(String text, DBObject sourceObject) {
        super(text);
        this.sourceObject = sourceObject;
    }

    public void setPopupLocation(RelativePoint popupLocation) {
        this.popupLocation = popupLocation;
    }

    public abstract List<DBObject> getObjectList();
    public abstract String getTitle();
    public abstract String getEmptyListMessage();
    public abstract String getListName();

    public final void actionPerformed(@NotNull final AnActionEvent e) {
        new ConnectionAction(sourceObject) {
            @Override
            public void execute() {
                new BackgroundTask(sourceObject.getProject(), "Loading " + getListName(), false, true) {

                    @Override
                    public void execute(@NotNull ProgressIndicator progressIndicator) {
                        final List<DBObject> objects = getObjectList();
                        if (!progressIndicator.isCanceled()) {
                            new SimpleLaterInvocator() {
                                @Override
                                protected void execute() {
                                    if (objects.size() > 0) {
                                        ObjectListActionGroup actionGroup = new ObjectListActionGroup(ObjectListShowAction.this, objects);
                                        JBPopup popup = JBPopupFactory.getInstance().createActionGroupPopup(
                                                ObjectListShowAction.this.getTitle(),
                                                actionGroup,
                                                e.getDataContext(),
                                                JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                                                true, null, 10);

                                        popup.getContent().setBackground(Colors.LIGHT_BLUE);
                                        showPopup(popup);
                                    }
                                    else {
                                        JLabel label = new JLabel(getEmptyListMessage(), Icons.EXEC_MESSAGES_INFO, SwingConstants.LEFT);
                                        label.setBorder(new EmptyBorder(3, 3, 3, 3));
                                        JPanel panel = new JPanel(new BorderLayout());
                                        panel.add(label);
                                        panel.setBackground(Colors.LIGHT_BLUE);
                                        ComponentPopupBuilder popupBuilder = JBPopupFactory.getInstance().createComponentPopupBuilder(panel, null);
                                        JBPopup popup = popupBuilder.createPopup();
                                        showPopup(popup);
                                    }
                                }
                            }.start();


                        }
                    }
                }.start();
            }
        }.start();
    }

    private void showPopup(JBPopup popup) {
        if (popupLocation == null) {
            DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(sourceObject.getProject());
            DatabaseBrowserTree activeBrowserTree = browserManager.getActiveBrowserTree();
            if (activeBrowserTree != null) {
                popupLocation = TreeUtil.getPointForSelection(activeBrowserTree);
                Point point = popupLocation.getPoint();
                point.setLocation(point.getX() + 20, point.getY() + 4);
            }
        }
        if (popupLocation != null) {
            popup.show(popupLocation);
        }
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
    }

    protected abstract AnAction createObjectAction(DBObject object);
}
