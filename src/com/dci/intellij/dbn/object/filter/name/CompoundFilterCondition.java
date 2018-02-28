package com.dci.intellij.dbn.object.filter.name;

import java.util.ArrayList;
import java.util.List;
import org.jdom.Element;

import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;

public class CompoundFilterCondition extends Filter<DBObject> implements FilterCondition {
    private List<FilterCondition> conditions = new ArrayList<FilterCondition>();
    private CompoundFilterCondition parent;
    private ConditionJoinType joinType = ConditionJoinType.AND;

    public CompoundFilterCondition() {
    }

    public CompoundFilterCondition(ConditionJoinType joinType) {
        this.joinType = joinType;
    }

    public void addCondition(ConditionOperator operator, String text) {
        SimpleFilterCondition condition = new SimpleFilterCondition(operator, text);
        addCondition(condition);
    }

    public void addCondition(FilterCondition condition) {
        condition.setParent(this);
        conditions.add(condition);

        ObjectNameFilterSettings settings = getSettings();
        if (settings != null) {
            settings.notifyNodeAdded(conditions.indexOf(condition), condition);
            settings.notifyChildNodesChanged(this);
        }
    }

    public void addCondition(FilterCondition condition, int index) {
        condition.setParent(this);
        conditions.add(index, condition);

        ObjectNameFilterSettings settings = getSettings();
        if (settings != null) {
            settings.notifyNodeAdded(index, condition);
            settings.notifyChildNodesChanged(this);
        }
    }

    public void removeCondition(FilterCondition condition, boolean cleanup) {
        int removeIndex = conditions.indexOf(condition);
        conditions.remove(condition);
        ObjectNameFilterSettings settings = getSettings();
        if (settings != null) {
            settings.notifyNodeRemoved(removeIndex, condition);
            settings.notifyChildNodesChanged(this);
        }

        if (cleanup) cleanup();
    }

    protected void cleanup() {
        if (conditions.size() == 1) {
            if (parent != null) {
                int index = parent.conditions.indexOf(this);
                FilterCondition condition = conditions.get(0);
                parent.addCondition(condition, index);
                parent.removeCondition(this, true);
            }

            FilterCondition filterCondition = conditions.get(0);
            if (filterCondition instanceof CompoundFilterCondition) {
                CompoundFilterCondition compoundFilterCondition = (CompoundFilterCondition) filterCondition;
                this.joinType = compoundFilterCondition.joinType;
                for (FilterCondition childCondition : compoundFilterCondition.conditions) {
                    addCondition(childCondition);
                }
                removeCondition(compoundFilterCondition, true);
            }
        }
    }

    public ObjectNameFilterSettings getSettings() {
        return parent == null ? null : parent.getSettings();
    }

    public boolean accepts(DBObject object) {
        if (joinType == ConditionJoinType.AND) {
            for (FilterCondition condition : conditions) {
                if (!condition.accepts(object)) return false;
            }
            return true;
        }

        if (joinType == ConditionJoinType.OR) {
            for (FilterCondition condition : conditions) {
                if (condition.accepts(object)) return true;
            }
            return false;
        }

        return false;
    }

    public ConditionJoinType getJoinType() {
        return joinType;
    }

    public void setJoinType(ConditionJoinType joinType) {
        this.joinType = joinType;
    }

    public DBObjectType getObjectType() {
        return parent.getObjectType();
    }

    public void setParent(CompoundFilterCondition parent) {
        this.parent = parent;
    }

    public CompoundFilterCondition getParent() {
        return parent;
    }

    public String getConditionString() {
        StringBuilder buffer = new StringBuilder();
        for (FilterCondition condition : conditions) {
            if (buffer.length() > 0) {
                buffer.append(joinType);
            }
            buffer.append(" (");
            buffer.append(condition.getConditionString());
            buffer.append(")");
        }
        return buffer.toString();
    }

    public List<FilterCondition> getConditions() {
        return conditions;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder("(");
        for (FilterCondition condition : conditions) {
            if (conditions.indexOf(condition) != 0) {
                buffer.append(" ");
                buffer.append(joinType.toString());
                buffer.append(" ");
            }
            buffer.append(condition.toString());
        }
        buffer.append(")");
        return buffer.toString();
    }

    /*********************************************************
     *                     Configuration                     *
     *********************************************************/
    public void readConfiguration(Element element) {
        String joinTypeString = element.getAttributeValue("join-type");
        joinType = StringUtil.isEmptyOrSpaces(joinTypeString) ? ConditionJoinType.AND : ConditionJoinType.valueOf(joinTypeString);
        for (Object o : element.getChildren()) {
            Element childElement = (Element) o;
            FilterCondition condition =
                    childElement.getName().equals("simple-condition") ? new SimpleFilterCondition() :
                    childElement.getName().equals("compound-condition") ? new CompoundFilterCondition() : null;
            if (condition != null) {
                condition.readConfiguration(childElement);
                condition.setParent(this);
                conditions.add(condition);
            }
        }
    }

    public void writeConfiguration(Element element) {
        element.setAttribute("join-type", joinType.toString());
        for (FilterCondition condition : conditions) {
            Element childElement =
                    condition instanceof SimpleFilterCondition ? new Element("simple-condition") :
                    condition instanceof CompoundFilterCondition ? new Element("compound-condition") : null;

            if (childElement != null) {
                condition.writeConfiguration(childElement);
                element.addContent(childElement);
            }
        }

    }
}
