package com.dci.intellij.dbn.data.model;

import java.util.HashMap;
import java.util.Map;

public class DataModelState {
    protected Map<String, String> contentTypesMap;
    private boolean isReadonly;
    private int rowCount;

    public void setTextContentType(String columnName, String contentTypeName) {
        if (contentTypesMap == null) contentTypesMap = new HashMap<String, String>();
        contentTypesMap.put(columnName, contentTypeName);
    }

    public String getTextContentTypeName(String columnName) {
        if (contentTypesMap != null) {
            return contentTypesMap.get(columnName);
        }
        return null;
    }

    public boolean isReadonly() {
        return isReadonly;
    }

    public void setReadonly(boolean readonly) {
        isReadonly = readonly;
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    /*****************************************************************
     *                     equals / hashCode                         *
     *****************************************************************/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataModelState that = (DataModelState) o;

        if (isReadonly != that.isReadonly) return false;
        if (rowCount != that.rowCount) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (isReadonly ? 1 : 0);
        result = 31 * result + rowCount;
        return result;
    }
}
