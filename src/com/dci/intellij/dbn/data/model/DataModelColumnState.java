package com.dci.intellij.dbn.data.model;

import java.util.ArrayList;
import java.util.List;

public class DataModelColumnState {
    private List<Column> columns = new ArrayList<Column>();

    public List<Column> getColumns() {
        return columns;
    }

    public class Column {
        private String name;
        private boolean visible;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isVisible() {
            return visible;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }
    }
}
