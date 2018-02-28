package com.dci.intellij.dbn.data.sorting;

import java.util.ArrayList;
import java.util.List;
import org.jdom.Element;

import com.dci.intellij.dbn.common.options.setting.SettingsUtil;

public class SortingState{
    private List<SortingInstruction> sortingInstructions = new ArrayList<SortingInstruction>();

    public boolean applySorting(String columnName, SortDirection direction, boolean keepExisting, int maxColumns) {
        SortingInstruction instruction = getInstruction(columnName);
        boolean isNewColumn = instruction == null;
        if (isNewColumn) {
            if (direction.isIndefinite()) {
                direction = SortDirection.ASCENDING;
            }
            instruction = new SortingInstruction(columnName, direction);
        } else {
            if (direction.isIndefinite()) {
                instruction.switchDirection();
            } else {
                instruction.setDirection(direction);
            }
        }


        if (keepExisting) {
            while (sortingInstructions.size() > maxColumns) {
                sortingInstructions.remove(sortingInstructions.size()-1);
            }

            if (isNewColumn) {
                if (sortingInstructions.size()== maxColumns) {
                    sortingInstructions.remove(sortingInstructions.size()-1);
                }
                sortingInstructions.add(instruction);
            }

        } else {
            sortingInstructions.clear();
            sortingInstructions.add(instruction);
        }

        updateIndexes();
        return true;
    }

    private void updateIndexes() {
        int index = 1;
        for (SortingInstruction sortingInstruction : sortingInstructions) {
            sortingInstruction.setIndex(index);
            index++;
        }
    }

    private SortingInstruction getInstruction(String columnName) {
        for (SortingInstruction instruction : sortingInstructions) {
            if (instruction.getColumnName().equals(columnName)) {
                return instruction;
            }
        }
        return null;
    }

    public void clear() {
        sortingInstructions.clear();
    }

    public void addSortingInstruction(SortingInstruction sortingInstruction) {
        sortingInstructions.add(sortingInstruction);
    }

    public SortingInstruction addSortingInstruction(String columnName, SortDirection direction) {
        SortingInstruction sortingInstruction = new SortingInstruction(columnName, direction);
        sortingInstructions.add(sortingInstruction);
        return sortingInstruction;
    }

    public List<SortingInstruction> getSortingInstructions() {
        return sortingInstructions;
    }

    public SortingInstruction getSortingInstruction(String columnName) {
        for (SortingInstruction sortingInstruction :  sortingInstructions) {
            if (sortingInstruction.getColumnName().equalsIgnoreCase(columnName)) {
                return sortingInstruction;
            }
        }
        return null;
    }

    public boolean isValid() {
        return true;
    }

    public SortingState clone() {
        SortingState clone = new SortingState();
        for (SortingInstruction criterion : sortingInstructions) {
            clone.sortingInstructions.add(criterion.clone());
        }
        return clone;
    }

    public void writeState(Element element) {
        for (SortingInstruction sortingInstruction : sortingInstructions) {
            String columnName = sortingInstruction.getColumnName();
            SortDirection sortDirection = sortingInstruction.getDirection();
            if (columnName != null && !sortDirection.isIndefinite()) {
                Element columnElement = new Element("column");
                columnElement.setAttribute("name", columnName);
                columnElement.setAttribute("index", Integer.toString(sortingInstruction.getIndex()));
                columnElement.setAttribute("direction", sortDirection.name());
                element.addContent(columnElement);
            }
        }
    }

    public void readState(Element element) {
        if (element != null) {
            List<Element> columnElements = element.getChildren();
            for (Element columnElement: columnElements) {
                String columnName = columnElement.getAttributeValue("name");
                String sortDirection = columnElement.getAttributeValue("direction");
                SortingInstruction sortingInstruction = addSortingInstruction(columnName, SortDirection.valueOf(sortDirection));
                sortingInstruction.setIndex(SettingsUtil.getIntegerAttribute(element, "index", 1));
            }
            updateIndexes();
        }
    }

    public int size() {
        return sortingInstructions.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SortingState that = (SortingState) o;

        return sortingInstructions.equals(that.sortingInstructions);

    }

    @Override
    public int hashCode() {
        return sortingInstructions.hashCode();
    }
}
