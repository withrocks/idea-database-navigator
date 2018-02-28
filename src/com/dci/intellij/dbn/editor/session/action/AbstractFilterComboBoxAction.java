package com.dci.intellij.dbn.editor.session.action;

import javax.swing.Icon;
import javax.swing.JComponent;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.action.DBNDataKeys;
import com.dci.intellij.dbn.common.ui.DBNComboBoxAction;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.editor.session.SessionBrowser;
import com.dci.intellij.dbn.editor.session.SessionBrowserFilterState;
import com.dci.intellij.dbn.editor.session.SessionBrowserFilterType;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserModel;
import com.dci.intellij.dbn.editor.session.options.SessionBrowserSettings;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.fileEditor.FileEditor;

public abstract class AbstractFilterComboBoxAction extends DBNComboBoxAction {
    private SessionBrowserFilterType filterType;

    public AbstractFilterComboBoxAction(SessionBrowserFilterType filterType) {
        this.filterType = filterType;
    }

    @NotNull
    protected DefaultActionGroup createPopupActionGroup(JComponent component) {
        SessionBrowser sessionBrowser = getSessionBrowser(component);
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.add(new SelectFilterValueAction(null));
        actionGroup.addSeparator();
        if (sessionBrowser != null) {
            SessionBrowserModel model = sessionBrowser.getTableModel();
            if (model != null) {
                SessionBrowserFilterState filter = model.getFilter();
                String selectedFilterValue = filter == null ? null : filter.getFilterValue(filterType);
                List<String> filterValues = model.getDistinctValues(filterType, selectedFilterValue);
                for (String filterValue : filterValues) {
                    SelectFilterValueAction action = new SelectFilterValueAction(filterValue);
                    actionGroup.add(action);
                }
            }
        }
        return actionGroup;
    }

    public synchronized void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        String text = filterType.getName();
        Icon icon = null;//Icons.DATASET_FILTER_EMPTY;

        SessionBrowser sessionBrowser = getSessionBrowser(e);
        if (sessionBrowser != null) {
            SessionBrowserModel model = sessionBrowser.getTableModel();
            if (model != null) {
                SessionBrowserFilterState modelFilter = model.getFilter();
                if (modelFilter != null) {
                    String filterValue = modelFilter.getFilterValue(filterType);
                    if (StringUtil.isNotEmpty(filterValue)) {
                        text = filterValue;
                        icon = filterType.getIcon();
                    }
                }
            }
        }

        presentation.setText(text);
        presentation.setIcon(icon);
    }

    @Nullable
    public static SessionBrowser getSessionBrowser(JComponent component) {
        DataContext dataContext = DataManager.getInstance().getDataContext(component);
        SessionBrowser sessionBrowser = DBNDataKeys.SESSION_BROWSER.getData(dataContext);
        if (sessionBrowser == null) {
            FileEditor fileEditor = PlatformDataKeys.FILE_EDITOR.getData(dataContext);
            if (fileEditor instanceof SessionBrowser) {
                sessionBrowser = (SessionBrowser) fileEditor;
            }
        }
        return sessionBrowser;
    }

    @Nullable
    public static SessionBrowser getSessionBrowser(AnActionEvent e) {
        SessionBrowser sessionBrowser = e.getData((DBNDataKeys.SESSION_BROWSER));
        if (sessionBrowser == null) {
            FileEditor fileEditor = e.getData(PlatformDataKeys.FILE_EDITOR);
            if (fileEditor instanceof SessionBrowser) {
                sessionBrowser = (SessionBrowser) fileEditor;
            }
        }
        return sessionBrowser;
    }

    private class SelectFilterValueAction extends AnAction {
        private String filterValue;

        public SelectFilterValueAction(String filterValue) {
            super(filterValue == null ? "No Filter" : filterValue, null, filterValue == null ? null : filterType.getIcon());
            this.filterValue = filterValue;
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            SessionBrowser sessionBrowser = getSessionBrowser(e);
            if (sessionBrowser != null) {
                SessionBrowserModel model = sessionBrowser.getTableModel();
                if (model !=  null) {
                    SessionBrowserFilterState modelFilter = model.getFilter();
                    if (modelFilter != null) {
                        modelFilter.setFilterValue(filterType, filterValue);
                        SessionBrowserSettings sessionBrowserSettings = sessionBrowser.getSettings();
                        if (sessionBrowserSettings.isReloadOnFilterChange()) {
                            sessionBrowser.loadSessions(false);
                        } else {
                            sessionBrowser.refreshTable();
                        }
                    }
                }
            }
        }
    }
 }