package com.dci.intellij.dbn.object.filter.name.ui;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import org.jdom.Element;

import com.dci.intellij.dbn.browser.options.ObjectFilterChangeListener;
import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.options.SettingsChangeNotifier;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.object.filter.name.FilterCondition;
import com.dci.intellij.dbn.object.filter.name.ObjectNameFilter;
import com.dci.intellij.dbn.object.filter.name.ObjectNameFilterManager;
import com.dci.intellij.dbn.object.filter.name.ObjectNameFilterSettings;
import com.dci.intellij.dbn.object.filter.name.SimpleFilterCondition;
import com.dci.intellij.dbn.object.filter.name.action.AddConditionAction;
import com.dci.intellij.dbn.object.filter.name.action.CreateFilterAction;
import com.dci.intellij.dbn.object.filter.name.action.MoveConditionDownAction;
import com.dci.intellij.dbn.object.filter.name.action.MoveConditionUpAction;
import com.dci.intellij.dbn.object.filter.name.action.RemoveConditionAction;
import com.dci.intellij.dbn.object.filter.name.action.SwitchConditionJoinTypeAction;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;

public class ObjectNameFilterSettingsForm extends ConfigurationEditorForm<ObjectNameFilterSettings> {
    private JPanel mainPanel;
    private JTree filtersTree;
    private JPanel actionsPanel;

    public ObjectNameFilterSettingsForm(ObjectNameFilterSettings configuration) {
        super(configuration);
        updateBorderTitleForeground(mainPanel);

/*        ObjectNameFilter schemaFilter = new ObjectNameFilter(DBObjectType.SCHEMA, ConditionOperator.NOT_LIKE, "T%");

        schemaFilter.addCondition(ConditionOperator.LIKE, "AE9%");
        configuration.addFilter(schemaFilter);

        ObjectNameFilter tableFilter = new ObjectNameFilter(DBObjectType.TABLE, ConditionOperator.NOT_LIKE, "T%");
        SimpleFilterCondition filterX = new SimpleFilterCondition(ConditionOperator.NOT_LIKE, "ZZ_%");
        tableFilter.addCondition(filterX);
        filterX.joinCondition(ConditionOperator.EQUAL, "BLA");

        configuration.addFilter(tableFilter);*/

        ActionToolbar actionToolbar = ActionUtil.createActionToolbar(
                "DBNavigator.ObjectNameFilters.Setup", true,
                new CreateFilterAction(this),
                new AddConditionAction(this),
                new RemoveConditionAction(this),
                new SwitchConditionJoinTypeAction(this),
                new Separator(),
                new MoveConditionUpAction(this),
                new MoveConditionDownAction(this));
        actionsPanel.add(actionToolbar.getComponent());

        filtersTree.setCellRenderer(new FilterSettingsTreeCellRenderer());
        ObjectNameFilterSettings tableModel = configuration.clone();
        filtersTree.setModel(tableModel);
        filtersTree.setShowsRootHandles(true);
        filtersTree.setRootVisible(false);

        for (ObjectNameFilter filter : tableModel.getFilters()) {
            filtersTree.expandPath(tableModel.createTreePath(filter));
        }

        filtersTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() > 1) {
                    Object selection = getSelection();
                    if (selection instanceof SimpleFilterCondition) {
                        SimpleFilterCondition condition = (SimpleFilterCondition) selection;
                        getManager().editFilterCondition(condition, ObjectNameFilterSettingsForm.this);
                    }
                }
            }
        });

        filtersTree.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == 10) {  // ENTER
                    Object selection = getSelection();
                    if (selection instanceof SimpleFilterCondition) {
                        SimpleFilterCondition condition = (SimpleFilterCondition) selection;
                        getManager().editFilterCondition(condition, ObjectNameFilterSettingsForm.this);
                    }
                } else if (e.getKeyChar() == 127) { //DEL
                    Object selection = getSelection();
                    if (selection instanceof FilterCondition) {
                        FilterCondition condition = (FilterCondition) selection;
                        getManager().removeFilterCondition(condition, ObjectNameFilterSettingsForm.this);
                    }
                }
            }
        });
    }

    private ObjectNameFilterManager getManager() {
        return ObjectNameFilterManager.getInstance(getConfiguration().getProject());
    }

    public Object getSelection() {
        TreePath selectionPath = filtersTree.getSelectionPath();
        return selectionPath == null ? null : selectionPath.getLastPathComponent();
    }


    public JTree getFiltersTree() {
        return filtersTree;
    }

    public JComponent getComponent() {
        return mainPanel;
    }

    public void applyFormChanges() throws ConfigurationException {
        final ObjectNameFilterSettings filterSettings = getConfiguration();
        final boolean notifyFilterListeners = filterSettings.isModified();
        Element element = new Element("Temp");
        ObjectNameFilterSettings tempSettings = (ObjectNameFilterSettings) filtersTree.getModel();
        tempSettings.writeConfiguration(element);
        filterSettings.readConfiguration(element);

        new SettingsChangeNotifier() {
            @Override
            public void notifyChanges() {
                if (notifyFilterListeners) {
                    Project project = filterSettings.getProject();
                    ObjectFilterChangeListener listener = EventManager.notify(project, ObjectFilterChangeListener.TOPIC);
                    ConnectionHandler connectionHandler = getConnectionHandler();
                    if (connectionHandler != null) {
                        listener.nameFiltersChanged(connectionHandler, null);
                    }
                }
            }
        };
    }

    private ConnectionHandler getConnectionHandler() {
        ObjectNameFilterSettings nameFilterSettings = getConfiguration();
        ConnectionManager connectionManager = ConnectionManager.getInstance(nameFilterSettings.getProject());
        ConnectionBundle connectionBundle = connectionManager.getConnectionBundle();
        for (ConnectionHandler connectionHandler : connectionBundle.getConnectionHandlers()) {
            if (nameFilterSettings == connectionHandler.getSettings().getFilterSettings().getObjectNameFilterSettings()) {
                return connectionHandler;
            }
        }
        return null;
    }

    public void resetFormChanges() {}
}
