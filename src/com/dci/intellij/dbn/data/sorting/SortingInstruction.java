package com.dci.intellij.dbn.data.sorting;

public class SortingInstruction {
    private int index;
    private String columnName;
    private SortDirection direction;

    public SortingInstruction(String columnName, SortDirection direction) {
        this.columnName = columnName;
        this.direction = direction;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public SortDirection getDirection() {
        return direction;
    }

    public void setDirection(SortDirection direction) {
        this.direction = direction;
    }

    public void switchDirection() {
        if (direction == SortDirection.ASCENDING) {
            direction = SortDirection.DESCENDING;
        } else if (direction == SortDirection.DESCENDING) {
            direction = SortDirection.ASCENDING;
        }
    }

    public SortingInstruction clone() {
        return new SortingInstruction(columnName, direction);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SortingInstruction that = (SortingInstruction) o;

        if (index != that.index) return false;
        if (!columnName.equals(that.columnName)) return false;
        return direction == that.direction;

    }

    @Override
    public int hashCode() {
        int result = index;
        result = 31 * result + columnName.hashCode();
        result = 31 * result + direction.hashCode();
        return result;
    }
}
