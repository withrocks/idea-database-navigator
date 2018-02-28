package com.dci.intellij.dbn.execution.method;

import com.dci.intellij.dbn.common.list.MostRecentStack;
import com.dci.intellij.dbn.common.state.PersistentStateElement;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import org.jdom.CDATA;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Text;

import java.util.ArrayList;
import java.util.List;

public class MethodExecutionArgumentValue implements PersistentStateElement<Element>, Cloneable, ArgumentValueHolder<String> {
    private String name;
    private MostRecentStack<String> valueHistory = new MostRecentStack<String>();

    public MethodExecutionArgumentValue(String name) {
        this.name = name;
    }

    public MethodExecutionArgumentValue(Element element) {
        readState(element);
    }

    public MethodExecutionArgumentValue(MethodExecutionArgumentValue source) {
        valueHistory.setValues(source.valueHistory.values());
        name = source.name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getValueHistory() {
        return valueHistory.values();
    }

    public String getValue() {
        return valueHistory.get();
    }

    public void setValue(String value) {
        valueHistory.stack(value);
    }

    @Override
    public void readState(Element element) {
        name = element.getAttributeValue("name");
        List<String> values = new ArrayList<String>();
        String value = CommonUtil.nullIfEmpty(element.getAttributeValue("value"));
        if (StringUtil.isNotEmpty(value)) {
            values.add(0, value);
        }

        List<Element> valueElements = element.getChildren();
        for (Element valueElement : valueElements) {
            if (valueElement.getContentSize() > 0) {
                Content content = valueElement.getContent(0);
                if (content instanceof Text) {
                    Text cdata = (Text) content;
                    value = cdata.getText();
                    if (StringUtil.isNotEmpty(value)) {
                        values.add(0, value);
                    }
                }
            }
        }
        valueHistory = new MostRecentStack<String>(values);
    }

    @Override
    public void writeState(Element element) {
        element.setAttribute("name", name);
        for (String value : valueHistory) {
            Element valueElement = new Element("value");
            element.addContent(valueElement);

            CDATA cdata = new CDATA(value);
            valueElement.setContent(cdata);
        }
    }

    @Override
    protected MethodExecutionArgumentValue clone() {
        return new MethodExecutionArgumentValue(this);
    }
}
