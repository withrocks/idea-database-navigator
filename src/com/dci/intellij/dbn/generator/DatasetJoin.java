package com.dci.intellij.dbn.generator;

import java.util.HashMap;
import java.util.Map;

import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBDataset;

public class DatasetJoin {
    private DBDataset dataset1;
    private DBDataset dataset2;
    private Map<DBColumn, DBColumn> mappings;

    public DatasetJoin(DBDataset dataset1, DBDataset dataset2, boolean lenient) {
        this.dataset1 = dataset1;
        this.dataset2 = dataset2;

        joinByReference(dataset1, dataset2);
        joinByReference(dataset2, dataset1);
        if (lenient) {
            joinByName(dataset1, dataset2);
        }
    }

    private void joinByReference(DBDataset dataset1, DBDataset dataset2) {
        if (mappings == null) {
            for (DBColumn column1 : dataset1.getColumns()) {
                if (column1.isForeignKey()) {
                    DBColumn column2 = column1.getForeignKeyColumn();
                    if (column2.getDataset().equals(dataset2)) {
                        createMapping(column1, column2);
                    }
                }
            }
        }
    }

    private void joinByName(DBDataset dataset1, DBDataset dataset2) {
        if (mappings == null) {
            for (DBColumn column1 : dataset1.getColumns()) {
                for (DBColumn column2 : dataset2.getColumns()) {
                    String name1 = column1.getName();
                    String name2 = column2.getName();
                    if (name1.length() > 2 && name1.equalsIgnoreCase(name2) && name1.toUpperCase().endsWith("ID")) {
                        createMapping(column1, column2);
                    }
                }
            }
        }
    }

    private void createMapping(DBColumn column1, DBColumn column2) {
        if (mappings == null) mappings = new HashMap<DBColumn, DBColumn>();
        mappings.put(column1, column2);
    }

    protected boolean contains(DBDataset... datasets) {
        for (DBDataset dataset : datasets) {
            if (!dataset1.equals(dataset) && !dataset2.equals(dataset)) {
                return false;
            }
        }

        return true;
    }

    public DBDataset getDataset1() {
        return dataset1;
    }

    public DBDataset getDataset2() {
        return dataset2;
    }

    public Map<DBColumn, DBColumn> getMappings() {
        return mappings;
    }

    public boolean isEmpty() {
        return mappings == null || mappings.size() == 0;
    }
}
