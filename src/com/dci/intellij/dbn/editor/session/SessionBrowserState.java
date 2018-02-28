package com.dci.intellij.dbn.editor.session;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.data.model.sortable.SortableDataModelState;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import gnu.trove.THashMap;

public class SessionBrowserState extends SortableDataModelState implements FileEditorState {
    public static final SessionBrowserState VOID = new SessionBrowserState();
    private SessionBrowserFilterState filterState = new SessionBrowserFilterState();
    private int refreshInterval = 0;

    public boolean canBeMergedWith(FileEditorState fileEditorState, FileEditorStateLevel fileEditorStateLevel) {
        return false;
    }

    public void readState(@NotNull Element element) {
        refreshInterval = SettingsUtil.getInteger(element, "refresh-interval", refreshInterval);

        Element sortingElement = element.getChild("sorting");
        sortingState.readState(sortingElement);

        Element filterElement = element.getChild("filter");
        if (filterElement != null) {
            filterState.setFilterValue(SessionBrowserFilterType.USER, SettingsUtil.getString(filterElement, "user", null));
            filterState.setFilterValue(SessionBrowserFilterType.HOST, SettingsUtil.getString(filterElement, "host", null));
            filterState.setFilterValue(SessionBrowserFilterType.STATUS, SettingsUtil.getString(filterElement, "status", null));
        }
    }

    public void writeState(Element element) {
        SettingsUtil.setInteger(element, "refresh-interval", refreshInterval);

        Element sortingElement = new Element("sorting");
        element.addContent(sortingElement);
        sortingState.writeState(sortingElement);

        Element filterElement = new Element("filter");
        element.addContent(filterElement);
        SettingsUtil.setString(filterElement, "user", filterState.getFilterValue(SessionBrowserFilterType.USER));
        SettingsUtil.setString(filterElement, "host", filterState.getFilterValue(SessionBrowserFilterType.HOST));
        SettingsUtil.setString(filterElement, "status", filterState.getFilterValue(SessionBrowserFilterType.STATUS));
    }

    public int getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(int refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public SessionBrowserState clone() {
        SessionBrowserState clone = new SessionBrowserState();
        clone.refreshInterval = refreshInterval;
        clone.setReadonly(isReadonly());
        clone.setRowCount(getRowCount());
        clone.setSortingState(getSortingState().clone());
        clone.filterState = filterState.clone();
        if (contentTypesMap != null) {
            clone.contentTypesMap = new THashMap<String, String>(contentTypesMap);
        }

        return clone;
    }

    public void setFilterState(SessionBrowserFilterState filterState) {
        this.filterState = filterState;
    }

    public SessionBrowserFilterState getFilterState() {
        return filterState;
    }

    /*****************************************************************
     *                     equals / hashCode                         *
     *****************************************************************/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SessionBrowserState that = (SessionBrowserState) o;
        if (this.refreshInterval != that.refreshInterval) return false;
        return filterState.equals(that.filterState);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + filterState.hashCode();
        return result;
    }
}