package com.dci.intellij.dbn.editor.data.filter;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.options.PersistentConfiguration;
import com.dci.intellij.dbn.data.sorting.SortingState;
import com.dci.intellij.dbn.object.DBDataset;
import com.intellij.openapi.options.UnnamedConfigurable;

public interface DatasetFilter extends UnnamedConfigurable, PersistentConfiguration {
    Icon getIcon();
    @NotNull
    String getId();
    String getName();
    String getVolatileName();
    String getConnectionId();
    String getDatasetName();
    boolean isNew();
    boolean isTemporary();
    boolean isIgnored();
    DatasetFilterType getFilterType();

    String getError();
    void setError(String error);

    DatasetFilterGroup getFilterGroup() ;

    String createSelectStatement(DBDataset dataset, SortingState sortingState);
}
