package com.dci.intellij.dbn.editor.data.state.column;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBDataset;

public class DatasetColumnSetup {
    private List<DatasetColumnState> columnStates = new ArrayList<DatasetColumnState>();

    @NotNull
    public List<DatasetColumnState> getColumnStates() {
        return columnStates;
    }

    public void init(@NotNull DBDataset dataset) {
        for (DBColumn column : dataset.getColumns()) {
            DatasetColumnState columnsState = getColumnState(column.getName());
            if (columnsState == null) {
                if (!column.isHidden()) {
                    columnsState = new DatasetColumnState(column);
                    columnStates.add(columnsState);
                }
            } else {
                columnsState.init(column);
            }
        }
        for (Iterator<DatasetColumnState> iterator = columnStates.iterator(); iterator.hasNext(); ) {
            DatasetColumnState columnState = iterator.next();
            if (dataset.getColumn(columnState.getName()) == null) {
                iterator.remove();
            }
        }

        Collections.sort(columnStates);
    }


    public DatasetColumnState getColumnState(String columnName) {
        for (DatasetColumnState columnsState : columnStates) {
            if (columnName.equals(columnsState.getName())) {
                return columnsState;
            }
        }
        return null;
    }

    public void readState(Element element) {
        if (element != null) {
            List<Element> childElements = element.getChildren();
            for (Element childElement : childElements) {
                String columnName = childElement.getAttributeValue("name");
                DatasetColumnState columnState = getColumnState(columnName);
                if (columnState == null) {
                    columnState = new DatasetColumnState(childElement);
                    columnStates.add(columnState);
                } else {
                    columnState.readState(childElement);
                }
            }
            Collections.sort(columnStates);
        }
    }

    public void writeState(Element element) {
        for (DatasetColumnState columnState : columnStates) {
            Element childElement = new Element("column");
            element.addContent(childElement);
            columnState.writeState(childElement);
        }
    }

    public void moveColumn(int fromIndex, int toIndex) {
        int visibleFromIndex = fromIndex;
        int visibleToIndex = toIndex;

        int visibleIndex = -1;
        for (int i=0; i< columnStates.size(); i++) {
            DatasetColumnState columnState = columnStates.get(i);
            if (columnState.isVisible()) {
                visibleIndex++;
                if (visibleIndex == fromIndex) visibleFromIndex = i;
                if (visibleIndex == toIndex) visibleToIndex = i;
            }
        }

        DatasetColumnState columnState = columnStates.remove(visibleFromIndex);
        columnStates.add(visibleToIndex, columnState);
        for (int i=0; i< columnStates.size(); i++) {
            columnStates.get(i).setPosition(i);
        }
    }

    public boolean isVisible(String name) {
        DatasetColumnState columnState = getColumnState(name);
        return columnState == null || columnState.isVisible();
    }

    /*****************************************************************
     *                     equals / hashCode                         *
     *****************************************************************/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DatasetColumnSetup that = (DatasetColumnSetup) o;

        if (!columnStates.equals(that.columnStates)) return false;

        return true;
    }

    @Override
    public DatasetColumnSetup clone() {
        DatasetColumnSetup clone = new DatasetColumnSetup();
        for (DatasetColumnState columnState : columnStates) {
            clone.columnStates.add(columnState.clone());
        }

        return clone;
    }

    @Override
    public int hashCode() {
        return columnStates.hashCode();
    }
}
