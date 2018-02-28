package com.dci.intellij.dbn.editor.data.filter;

import javax.swing.Icon;
import javax.swing.JComponent;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.data.sorting.SortingState;
import com.dci.intellij.dbn.object.DBDataset;
import com.intellij.openapi.options.ConfigurationException;

public class DatasetEmptyFilter implements DatasetFilter{

    public Icon getIcon() {
        return Icons.DATASET_FILTER_EMPTY;
    }

    @NotNull
    public String getId() {
        return "EMPTY_FILTER";
    }

    public String getName() {
        return "No Filter";
    }

    public String getVolatileName() {
        return getName();
    }

    public String createSelectStatement(DBDataset dataset, SortingState sortingState) {
        setError(null);
        StringBuilder buffer = new StringBuilder();
        DatasetFilterUtil.createSimpleSelectStatement(dataset, buffer);
        DatasetFilterUtil.addOrderByClause(dataset, buffer, sortingState);
        DatasetFilterUtil.addForUpdateClause(dataset, buffer);
        return buffer.toString();
    }

    public String getConnectionId() { return null; }
    public String getDatasetName() { return null; }
    public boolean isNew() { return false; }

    public boolean isTemporary() {
        return false;
    }

    public boolean isIgnored() {
        return false;
    }

    public DatasetFilterType getFilterType() {
        return DatasetFilterType.NONE;
    }

    public String getError() { return null; }
    public void setError(String error) {}
    public DatasetFilterGroup getFilterGroup() { return null; }
    public JComponent createComponent() { return null; }
    public boolean isModified() { return false; }
    public void apply() throws ConfigurationException {}
    public void reset() {}
    public void disposeUIResources() {}

    public void readConfiguration(Element element) {}
    public void writeConfiguration(Element element) {}
}
