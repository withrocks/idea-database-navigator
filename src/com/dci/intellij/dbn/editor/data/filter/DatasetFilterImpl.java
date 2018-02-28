package com.dci.intellij.dbn.editor.data.filter;

import java.util.UUID;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.object.DBDataset;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.text.StringUtil;

public abstract class DatasetFilterImpl extends Configuration implements DatasetFilter {
    private DatasetFilterGroup filterGroup;
    private String id;
    private String name;
    private String error;
    private boolean isNew = true;
    private boolean isCustomNamed = false;
    private boolean isTemporary;
    private DatasetFilterType filterType;

    protected DatasetFilterImpl(DatasetFilterGroup filterGroup, String name, String id, DatasetFilterType filterType) {
        this.filterGroup = filterGroup;
        this.name = name;
        this.id = id;
        this.filterType = filterType;
    }

    protected DatasetFilterImpl(DatasetFilterGroup filterGroup, String name, DatasetFilterType filterType) {
        this(filterGroup, name, UUID.randomUUID().toString(), filterType);
    }

    @NotNull
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return name;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    public void setName(String name) {
        this.name = StringUtil.first(name, 40, true);
    }

    public String getConnectionId() {
        return filterGroup.getConnectionId();
    }

    public String getDatasetName() {
        return filterGroup.getDatasetName();
    }

    public DatasetFilterGroup getFilterGroup() {
        return filterGroup;
    }

    public boolean isCustomNamed() {
        return isCustomNamed;
    }

    public void setCustomNamed(boolean customNamed) {
        this.isCustomNamed = customNamed;
    }

    public abstract void generateName();

    public boolean isTemporary() {
        return isTemporary;
    }

    public void setTemporary(boolean temporary) {
        isTemporary = temporary;
    }

    public DatasetFilterType getFilterType() {
        return filterType;
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof DatasetFilter) {
            DatasetFilter remote = (DatasetFilter) obj;
            return remote.getFilterGroup().equals(filterGroup) &&
                   remote.getId().equals(id);
        }
        return false;
    }

    @Nullable
    public DBDataset lookupDataset() {
        return filterGroup.lookupDataset();
    }

    public void apply() throws ConfigurationException {
        super.apply();
        isTemporary = false;
        isNew = false;
    }

    /****************************************************
     *                   Configuration                  *
     ****************************************************/
    public void readConfiguration(Element element) {
        id = element.getAttributeValue("id");
        name = element.getAttributeValue("name");
        isTemporary = Boolean.parseBoolean(element.getAttributeValue("temporary"));
        isCustomNamed = Boolean.parseBoolean(element.getAttributeValue("custom-name"));
        isNew = false;
    }

    public void writeConfiguration(Element element) {
        element.setAttribute("id", id);
        element.setAttribute("name", name);
        element.setAttribute("temporary", Boolean.toString(isTemporary));
        element.setAttribute("custom-name", Boolean.toString(isCustomNamed));
    }

}
