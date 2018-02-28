package com.dci.intellij.dbn.object.filter.name;

import com.dci.intellij.dbn.object.common.DBObjectType;
import org.jdom.Element;

public class ObjectNameFilter extends CompoundFilterCondition {
    private ObjectNameFilterSettings settings;
    private DBObjectType objectType;
    private int hashCode;


    public ObjectNameFilter(ObjectNameFilterSettings settings) {
        this.settings = settings;
    }

    public ObjectNameFilterSettings getSettings() {
        return settings;
    }

    public ObjectNameFilter(ObjectNameFilterSettings settings, DBObjectType objectType, ConditionOperator operator, String text) {
        this.settings = settings;
        this.objectType = objectType;
        addCondition(operator, text);
    }

    public ObjectNameFilter(ObjectNameFilterSettings settings, DBObjectType objectType, SimpleFilterCondition condition) {
        this.settings = settings;
        this.objectType = objectType;
        addCondition(condition);
    }

    @Override
    protected void cleanup() {
        super.cleanup();
        if (getConditions().isEmpty()) {
            settings.removeFilter(this);
        }
    }

    public DBObjectType getObjectType() {
        return objectType;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    /*********************************************************
     *                     Configuration                     *
     *********************************************************/
    @Override
    public void readConfiguration(Element element) {
        super.readConfiguration(element);
        objectType = DBObjectType.getObjectType(element.getAttributeValue("object-type"));
        hashCode = toString().hashCode();
    }

    @Override
    public void writeConfiguration(Element element) {
        super.writeConfiguration(element);
        element.setAttribute("object-type", objectType.getName());
    }
}
