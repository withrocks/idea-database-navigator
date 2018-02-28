package com.dci.intellij.dbn.editor.data.filter.global;

import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBTable;

public class DataDependencyPathBuilder {
    public static void buildDependencyPath(DataDependencyPath path, DBColumn sourceColumn, DBColumn targetColumn, DataDependencyPath[] shortestPath) {
        if (path == null) {
            path = new DataDependencyPath(sourceColumn);
        } else {
            if (path.isRecursiveElement(sourceColumn)) {
                return;
            }

            path = (DataDependencyPath) path.clone();
            path.addPathElement(sourceColumn);
            if (sourceColumn.getDataset() == targetColumn.getDataset()) {
                if (shortestPath[0] == null || shortestPath[0].size() > path.size()) {
                    shortestPath[0] = path;
                }
                return;
            }
            if (shortestPath[0] != null && shortestPath[0].size() < path.size()) {
                return;
            }
        }

        DBTable sourceTable = (DBTable) sourceColumn.getDataset();
        for (DBColumn column : sourceTable.getForeignKeyColumns()) {
            if (column != sourceColumn) {
                DBColumn fkColumn = column.getForeignKeyColumn();
                buildDependencyPath(path, fkColumn, targetColumn, shortestPath);
            }

        }

        for (DBColumn pkColumn : sourceTable.getPrimaryKeyColumns()) {
            if (pkColumn != sourceColumn) {
                for (DBColumn fkColumn : pkColumn.getReferencingColumns()) {
                    buildDependencyPath(path, fkColumn, targetColumn, shortestPath);
                }
            }
        }
    }


    public static DBTable[] buildDependencyChain(DBTable masterTable, DBTable targetTable) {

        // look first if targetTable is a referenced from masterTable
        for (DBColumn fkColumn : masterTable.getForeignKeyColumns()) {
            if (fkColumn.getForeignKeyColumn().getDataset() == targetTable) {
                return new DBTable[]{masterTable, targetTable};
            }
        }

        // check for tables referencing the primary key
        for (DBColumn fkColumn : masterTable.getPrimaryKeyColumns().get(0).getReferencingColumns()) {
            if (fkColumn.getDataset() == targetTable) {
                return new DBTable[]{masterTable, targetTable};
            }
            
        }
        //for (column )
        return null;
    }
}
