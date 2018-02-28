package com.dci.intellij.dbn.editor.data.filter.global;

import com.dci.intellij.dbn.object.DBColumn;

import java.util.ArrayList;
import java.util.List;

public class DataDependencyPath {
    private List<DBColumn> pathElements = new ArrayList<DBColumn>();

    private DataDependencyPath() {}

    public DataDependencyPath(DBColumn startElement) {
        addPathElement(startElement);
    }

    public void addPathElement(DBColumn pathElement) {
        pathElements.add(pathElement);
    }

    public boolean isRecursiveElement(DBColumn element) {
        for (DBColumn pathElement : pathElements ) {
            if (pathElement.getDataset() == element.getDataset()) {
                return true;
            }
        }
        return false;
    }

    public int size() {
        return pathElements.size();
    }

    protected Object clone() {
        DataDependencyPath clone = new DataDependencyPath();
        clone.pathElements.addAll(pathElements);
        return clone; 
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        DBColumn lastElement = pathElements.get(pathElements.size() - 1);
        for (DBColumn pathElement : pathElements) {
            buffer.append(pathElement.getDataset().getName());
            buffer.append(".");
            buffer.append(pathElement.getName());
            if (lastElement != pathElement) {
                buffer.append(" > ");
            }
        }
        return buffer.toString();
    }
}
