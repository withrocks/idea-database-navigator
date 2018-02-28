package com.dci.intellij.dbn.editor.data.state;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.data.model.sortable.SortableDataModelState;
import com.dci.intellij.dbn.editor.data.state.column.DatasetColumnSetup;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import gnu.trove.THashMap;

public class DatasetEditorState extends SortableDataModelState implements FileEditorState {
    public static final DatasetEditorState VOID = new DatasetEditorState();
    private DatasetColumnSetup columnSetup = new DatasetColumnSetup();

    public boolean canBeMergedWith(FileEditorState fileEditorState, FileEditorStateLevel fileEditorStateLevel) {
        return fileEditorState instanceof DatasetEditorState && fileEditorStateLevel == FileEditorStateLevel.FULL;
    }

    public DatasetColumnSetup getColumnSetup() {
        return columnSetup;
    }

    public void readState(@NotNull Element element) {
        setRowCount(SettingsUtil.getIntegerAttribute(element, "row-count", 100));
        setReadonly(SettingsUtil.getBooleanAttribute(element, "readonly", false));

/*
        getSortingState().setColumnName(element.getAttributeValue("sort-column-name"));
        getSortingState().setDirectionAsString(element.getAttributeValue("sort-direction"));
*/

        Element columnsElement = element.getChild("columns");
        columnSetup.readState(columnsElement);

        Element sortingElement = element.getChild("sorting");
        sortingState.readState(sortingElement);


        Element contentTypesElement = element.getChild("content-types");
        if (contentTypesElement != null) {
            for (Object o : contentTypesElement.getChildren()) {
                Element contentTypeElement = (Element) o;
                String columnName = contentTypeElement.getAttributeValue("column-name");
                String contentTypeName = contentTypeElement.getAttributeValue("type-name");
                setTextContentType(columnName, contentTypeName);
            }
        }
    }

    public void writeState(Element targetElement) {
        targetElement.setAttribute("row-count", Integer.toString(getRowCount()));
        targetElement.setAttribute("readonly", Boolean.toString(isReadonly()));
/*
        targetElement.setAttribute("sort-column-name", getSortingState().getColumnName());
        targetElement.setAttribute("sort-direction", getSortingState().getDirectionAsString());
*/

        Element columnsElement = new Element("columns");
        targetElement.addContent(columnsElement);
        columnSetup.writeState(columnsElement);

        Element sortingElement = new Element("sorting");
        targetElement.addContent(sortingElement);
        sortingState.writeState(sortingElement);

        Element contentTypesElement = new Element("content-types");
        targetElement.addContent(contentTypesElement);
        if (contentTypesMap != null && contentTypesMap.size() > 0) {
            for (String columnName : contentTypesMap.keySet()) {
                Element contentTypeElement = new Element("content-type");
                String contentTypeName = contentTypesMap.get(columnName);
                contentTypeElement.setAttribute("column-name", columnName);
                contentTypeElement.setAttribute("type-name", contentTypeName);
                contentTypesElement.addContent(contentTypeElement);
            }
        }
    }

    public DatasetEditorState clone() {
        DatasetEditorState clone = new DatasetEditorState();
        clone.setReadonly(isReadonly());
        clone.setRowCount(getRowCount());
        clone.setSortingState(getSortingState());
        clone.columnSetup = columnSetup.clone();
        if (contentTypesMap != null) {
            clone.contentTypesMap = new THashMap<String, String>(contentTypesMap);
        }

        return clone;
    }
    /*****************************************************************
     *                     equals / hashCode                         *
     *****************************************************************/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        DatasetEditorState that = (DatasetEditorState) o;

        if (!columnSetup.equals(that.columnSetup)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + columnSetup.hashCode();
        return result;
    }
}