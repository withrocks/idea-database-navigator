package com.dci.intellij.dbn.object.filter.name;

import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.openapi.util.text.StringUtil;
import org.jdom.Element;

import java.util.StringTokenizer;

public class SimpleFilterCondition implements FilterCondition {
    private CompoundFilterCondition parent;
    private ConditionOperator operator;
    private String text;

    public SimpleFilterCondition() {
    }


    public SimpleFilterCondition(ConditionOperator operator, String text) {
        this.operator = operator;
        this.text = text;
    }

    public ObjectNameFilterSettings getSettings() {
        return parent.getSettings();
    }

    public boolean accepts(DBObject object) {
        String name = object.getName();
        switch (operator) {
            case EQUAL: return isEqual(name);
            case NOT_EQUAL: return !isEqual(name);
            case LIKE: return isLike(name);
            case NOT_LIKE: return !isLike(name);
        }
        return false;
    }

    private boolean isEqual(String name) {
        return name.equalsIgnoreCase(text);
    }

    private boolean isLike(String name) {
        StringTokenizer tokenizer = new StringTokenizer(text, "*%");
        int startIndex = 0;
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            int index = StringUtil.indexOfIgnoreCase(name, token, startIndex);
            if (index == -1 || (index > 0 && startIndex == 0 && !startsWithWildcard())) return false;
            startIndex = index + token.length();
        }

        return true;
    }

    private boolean startsWithWildcard() {
        return text.indexOf('*') == 0 || text.indexOf('%') == 0;
    }



    public void setOperator(ConditionOperator operator) {
        this.operator = operator;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setParent(CompoundFilterCondition parent) {
        this.parent = parent;
    }

    public CompoundFilterCondition getParent() {
        return parent;
    }

    public DBObjectType getObjectType() {
        return parent.getObjectType();
    }

    public String getConditionString() {
        return "OBJECT_NAME " + operator + " '" + text + "'";
    }

    public ConditionOperator getOperator() {
        return operator;
    }

    public String getText() {
        return text;
    }

    public String toString() {
        return getObjectType().getName().toUpperCase() + "_NAME " + operator + " '" + text + "'";
    }

    /*********************************************************
     *                     Configuration                     *
     *********************************************************/
    public void readConfiguration(Element element) {
        operator = ConditionOperator.valueOf(element.getAttributeValue("operator"));
        text = element.getAttributeValue("text");
    }

    public void writeConfiguration(Element element) {
        element.setAttribute("operator", operator.name());
        element.setAttribute("text", text);
    }
}
