package com.dci.intellij.dbn.execution.explain.result.ui;

import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import java.math.BigDecimal;
import java.util.List;

import com.dci.intellij.dbn.execution.explain.result.ExplainPlanEntry;
import com.dci.intellij.dbn.execution.explain.result.ExplainPlanResult;
import com.intellij.openapi.project.Project;
import com.intellij.ui.treeStructure.treetable.TreeTableModel;

public class ExplainPlanTreeTableModel implements TreeTableModel{
    private ExplainPlanResult result;

    public ExplainPlanTreeTableModel(ExplainPlanResult result) {
        this.result = result;
    }

    private Column[] COLUMNS = new Column[]{
            new Column("OPERATION", TreeTableModel.class) {
                @Override
                public Object getValue(ExplainPlanEntry entry) {
                    String options = entry.getOperationOptions();
                    return this; /*entry.getOperation() + (StringUtil.isEmpty(options) ? "" : "(" + options + ")");*/
                }
            },
/*
            new Column("OBJECT", DBObjectRef.class) {
                @Override
                public Object getValue(ExplainPlanEntry entry) {
                    return entry.getObjectRef();
                }
            },
            new Column("DEPTH", BigDecimal.class) {
                @Override
                public Object getValue(ExplainPlanEntry entry) {
                    return entry.getDepth();
                }
            },
            new Column("POSITION", BigDecimal.class) {
                @Override
                public Object getValue(ExplainPlanEntry entry) {
                    return entry.getPosition();
                }
            },
*/
            new Column("COST", BigDecimal.class) {
                @Override
                public Object getValue(ExplainPlanEntry entry) {
                    return entry.getCost();
                }
            },
            new Column("CARDINALITY", BigDecimal.class) {
                @Override
                public Object getValue(ExplainPlanEntry entry) {
                    return entry.getCardinality();
                }
            },
            new Column("BYTES", BigDecimal.class) {
                @Override
                public Object getValue(ExplainPlanEntry entry) {
                    return entry.getBytes();
                }
            },
            new Column("CPU_COST", BigDecimal.class) {
                @Override
                public Object getValue(ExplainPlanEntry entry) {
                    return entry.getCpuCost();
                }
            },
            new Column("IO_COST", BigDecimal.class) {
                @Override
                public Object getValue(ExplainPlanEntry entry) {
                    return entry.getIoCost();
                }
            },
            new Column("ACCESS_PREDICATES", String.class, true) {
                @Override
                public Object getValue(ExplainPlanEntry entry) {
                    return entry.getAccessPredicates();
                }
            },
            new Column("FILTER_PREDICATES", String.class, true) {
                @Override
                public Object getValue(ExplainPlanEntry entry) {
                    return entry.getFilterPredicates();
                }
            },
            new Column("PROJECTION", String.class, true) {
                @Override
                public Object getValue(ExplainPlanEntry entry) {
                    return entry.getProjection();
                }
            }

    };

    public Project getProject() {
        return result.getProject();
    }

    /***************************************************************
     *                         TableModel                          *
     ***************************************************************/

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column].getName();
    }

    @Override
    public Class getColumnClass(int column) {
        return COLUMNS[column].getClazz();
    }

    @Override
    public Object getValueAt(Object node, int column) {
        if (node instanceof ExplainPlanEntry) {
            ExplainPlanEntry entry = (ExplainPlanEntry) node;
            return COLUMNS[column].getValue(entry);
        }
        return null;
    }

    @Override public boolean isCellEditable(Object node, int column) {return false;}
    @Override public void setValueAt(Object aValue, Object node, int column) {}
    @Override public void setTree(JTree tree) {}
    boolean isLargeValue(int column) {
        return COLUMNS[column].isLarge();
    }

    /***************************************************************
     *                          TreeModel                          *
     ***************************************************************/
    @Override
    public Object getRoot() {
        return result.getRoot();
    }

    @Override
    public Object getChild(Object parent, int index) {
        if (parent instanceof ExplainPlanEntry) {
            ExplainPlanEntry entry = (ExplainPlanEntry) parent;
            return entry.getChildren().get(index);
        }
        return null;
    }

    @Override
    public int getChildCount(Object parent) {
        if (parent instanceof ExplainPlanEntry) {
            ExplainPlanEntry entry = (ExplainPlanEntry) parent;
            List<ExplainPlanEntry> children = entry.getChildren();
            return children == null ? 0 : children.size();
        }
        return 0;
    }

    @Override
    public boolean isLeaf(Object node) {
        return getChildCount(node) == 0;
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {}

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        if (parent instanceof ExplainPlanEntry && child instanceof ExplainPlanEntry) {
            ExplainPlanEntry parentEntry = (ExplainPlanEntry) parent;
            ExplainPlanEntry childEntry = (ExplainPlanEntry) child;
            return parentEntry.getChildren().indexOf(childEntry);
        }
        return -1;
    }

    @Override public void addTreeModelListener(TreeModelListener l) {}
    @Override public void removeTreeModelListener(TreeModelListener l) {}

    public static abstract class Column {
        private String name;
        private Class clazz;
        private boolean large;

        public Column(String name, Class clazz) {
            this.name = name;
            this.clazz = clazz;
        }

        public Column(String name, Class clazz, boolean large) {
            this.name = name;
            this.clazz = clazz;
            this.large = large;
        }

        public String getName() {
            return name;
        }

        public Class getClazz() {
            return clazz;
        }

        public boolean isLarge() {
            return large;
        }

        public abstract Object getValue(ExplainPlanEntry entry);
    }
}
