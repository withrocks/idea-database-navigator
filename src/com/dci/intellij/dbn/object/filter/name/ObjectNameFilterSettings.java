package com.dci.intellij.dbn.object.filter.name;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jdom.Element;

import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.options.ProjectConfiguration;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectRelationType;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.list.DBObjectRelation;
import com.dci.intellij.dbn.object.filter.name.ui.ObjectNameFilterSettingsForm;
import com.intellij.openapi.project.Project;

public class ObjectNameFilterSettings extends ProjectConfiguration<ObjectNameFilterSettingsForm> implements TreeModel {
    private List<ObjectNameFilter> filters = new ArrayList<ObjectNameFilter>();
    private Map<DBObjectType, Filter<DBObject>> objectFilterMap = new EnumMap<DBObjectType, Filter<DBObject>>(DBObjectType.class);
    private Map<DBObjectRelationType, Filter<DBObjectRelation>> objectRelationFilterMap = new EnumMap<DBObjectRelationType, Filter<DBObjectRelation>>(DBObjectRelationType.class);
    public ObjectNameFilterSettings(Project project) {
        super(project);
    }

    public List<ObjectNameFilter> getFilters() {
        return filters;
    }

    public void addFilter(ObjectNameFilter filter) {
        filters.add(filter);
        objectFilterMap.put(filter.getObjectType(), filter);
        notifyNodeAdded(filters.indexOf(filter), filter);
    }

    public void addFilter(ObjectNameFilter filter, int index) {
        filters.add(index, filter);
        objectFilterMap.put(filter.getObjectType(), filter);
        notifyNodeAdded(filters.indexOf(filter), filter);
    }


    public void removeFilter(ObjectNameFilter filter) {
        int index = filters.indexOf(filter);
        filters.remove(filter);
        objectFilterMap.remove(filter.getObjectType());
        notifyNodeRemoved(index, filter);
    }

    public boolean containsFilter(DBObjectType objectType) {
        return objectFilterMap.containsKey(objectType);
    }


    public Filter<DBObjectRelation> getFilter(DBObjectRelationType objectRelationType) {
        if (!objectRelationFilterMap.containsKey(objectRelationType)) {
            final Filter<DBObject> sourceFilter = getFilter(objectRelationType.getSourceType());
            final Filter<DBObject> targetFilter = getFilter(objectRelationType.getTargetType());
            final int hashCode =
                    (sourceFilter == null ? 0 : sourceFilter.hashCode()) +
                    (targetFilter == null ? 0 : targetFilter.hashCode());

            Filter<DBObjectRelation> filter = null;
            if (sourceFilter != null || targetFilter != null) {
                 filter = new Filter<DBObjectRelation>() {
                    public boolean accepts(DBObjectRelation objectRelation) {
                        return
                            (sourceFilter == null || sourceFilter.accepts(objectRelation.getSourceObject())) &&
                            (targetFilter == null || targetFilter.accepts(objectRelation.getTargetObject()));
                    }

                    @Override
                    public int hashCode() {
                        return hashCode;
                    }
                };
            }
            objectRelationFilterMap.put(objectRelationType, filter);
        }

        return objectRelationFilterMap.get(objectRelationType);
    }

    public Filter<DBObject> getFilter(DBObjectType objectType) {
        Filter<DBObject> filter = objectFilterMap.get(objectType);
        if (filter == null) {
            DBObjectType genericObjectType = objectType.getGenericType();
            while (filter == null && genericObjectType != objectType) {
                filter = objectFilterMap.get(genericObjectType);
                objectType = genericObjectType;
                genericObjectType = objectType.getGenericType();
            }
        }
        return filter;
    }

    public ObjectNameFilterSettings clone() {
        try {
            ObjectNameFilterSettings clone = new ObjectNameFilterSettings(getProject());
            Element element = new Element("Temp");
            writeConfiguration(element);
            clone.readConfiguration(element);
            return clone;
        } catch (Exception e) {
            return null;
        }
    }

    /*********************************************************
     *                     Configuration                     *
     *********************************************************/
    @Override
    protected ObjectNameFilterSettingsForm createConfigurationEditor() {
        return new ObjectNameFilterSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "object-name-filters";
    }

    public void readConfiguration(Element element) {
        filters.clear();
        objectFilterMap.clear();
        objectRelationFilterMap.clear();
        for (Object o : element.getChildren()) {
            Element filterElement = (Element) o;
            ObjectNameFilter filter = new ObjectNameFilter(this);
            filter.readConfiguration(filterElement);
            filters.add(filter);
            objectFilterMap.put(filter.getObjectType(), filter);
        }
    }

    public void writeConfiguration(Element element) {
        for (ObjectNameFilter filter : filters) {
            Element filterElement = new Element("filter");
            filter.writeConfiguration(filterElement);
            element.addContent(filterElement);
        }
    }

    /*********************************************************
     *                       TreeModel                       *
     *********************************************************/
    private Set<TreeModelListener> listeners = new HashSet<TreeModelListener>();

    public Object getRoot() {
        return this;
    }

    public Object getChild(Object parent, int index) {
        List children = getChildren(parent);
        return children.size() > index ? children.get(index) : null;
    }

    public int getChildCount(Object parent) {
        return getChildren(parent).size();
    }

    private List getChildren(Object parent) {
        if (parent instanceof ObjectNameFilterSettings) {
            ObjectNameFilterSettings filterSettings = (ObjectNameFilterSettings) parent;
            return filterSettings.filters;
        }

        if (parent instanceof CompoundFilterCondition) {
            CompoundFilterCondition compoundCondition = (CompoundFilterCondition) parent;
            return compoundCondition.getConditions();
        }

        return Collections.EMPTY_LIST;
    }

    public boolean isLeaf(Object node) {
        return node instanceof SimpleFilterCondition;
    }

    public int getIndexOfChild(Object parent, Object child) {
        return getChildren(parent).indexOf(child);
    }

    public void notifyNodeAdded(int index, FilterCondition condition) {
        if (listeners.size()> 0) {
            TreeModelEvent event = createTreeModelEvent(index, condition);
            for (TreeModelListener listener: listeners) {
                listener.treeNodesInserted(event);
            }
        }
    }

    public void notifyNodeRemoved(int index, FilterCondition condition) {
        if (listeners.size()> 0) {
            TreeModelEvent event = createTreeModelEvent(index, condition);
            for (TreeModelListener listener: listeners) {
                listener.treeNodesRemoved(event);
            }
        }
    }

    public void notifyNodeChanged(FilterCondition condition) {
        if (listeners.size() > 0) {
            if (condition instanceof ObjectNameFilter) {
                ObjectNameFilter filter = (ObjectNameFilter) condition;
                int index = filter.getSettings().filters.indexOf(filter);
                TreeModelEvent event = createTreeModelEvent(index, condition);
                for (TreeModelListener listener: listeners) {
                    listener.treeNodesChanged(event);
                }
            } else {
                CompoundFilterCondition parent = condition.getParent();
                if (parent != null) {
                    int index = parent.getConditions().indexOf(condition);
                    TreeModelEvent event = createTreeModelEvent(index, condition);
                    for (TreeModelListener listener: listeners) {
                        listener.treeNodesChanged(event);
                    }
                }
            }

        }
    }

    public void notifyChildNodesChanged(CompoundFilterCondition parentCondition) {
        if (listeners.size()> 0) {
            int[] indexes = new int[parentCondition.getConditions().size()];
            for (int i=0; i<indexes.length; i++) {
                indexes[i] = i;
            }

            TreeModelEvent event = createTreeModelEvent(indexes, parentCondition);
            for (TreeModelListener listener: listeners) {
                listener.treeNodesChanged(event);
            }
        }
    }

    private TreeModelEvent createTreeModelEvent(int index, FilterCondition condition) {
        CompoundFilterCondition parent = condition.getParent();
        TreePath path = createTreePath(parent == null ? this : parent);
        return new TreeModelEvent(this, path, new int[]{index}, new Object[]{condition});
    }

    private TreeModelEvent createTreeModelEvent(int[] indexes, CompoundFilterCondition parentCondition) {
        FilterCondition[] filterConditions = new FilterCondition[indexes.length];
        for (int i=0; i<indexes.length; i++) {
            filterConditions[i] = parentCondition.getConditions().get(indexes[i]);
        }
        TreePath path = createTreePath(parentCondition);
        return new TreeModelEvent(this, path, indexes, filterConditions);
    }


    public TreePath createTreePath(Object object) {
        List path = new ArrayList();
        if (object instanceof FilterCondition) {
            FilterCondition condition = (FilterCondition) object;
            path.add(condition);
            FilterCondition parent = condition.getParent();
            while (parent != null) {
                path.add(0, parent);
                parent = parent.getParent();
            }

        }
        path.add(0, this);

        return new TreePath(path.toArray());
    }


    public void valueForPathChanged(TreePath path, Object newValue) {
        System.out.println("");
    }

    public void addTreeModelListener(TreeModelListener listener) { listeners.add(listener); }

    public void removeTreeModelListener(TreeModelListener listener) { listeners.remove(listener); }

}
