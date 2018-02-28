package com.dci.intellij.dbn.execution.method;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jdom.Element;

import com.dci.intellij.dbn.common.state.PersistentStateElement;
import com.dci.intellij.dbn.common.util.StringUtil;
import gnu.trove.THashMap;

public class MethodExecutionArgumentValuesCache implements PersistentStateElement<Element> {
    private Map<String, Set<MethodExecutionArgumentValue>> variablesMap = new THashMap<String, Set<MethodExecutionArgumentValue>>();

    public MethodExecutionArgumentValue getArgumentValue(String connectionId, String name, boolean create) {
        Set<MethodExecutionArgumentValue> argumentValues = variablesMap.get(connectionId);

        if (argumentValues != null) {
            for (MethodExecutionArgumentValue argumentValue : argumentValues) {
                if (argumentValue.getName().equalsIgnoreCase(name)) {
                    return argumentValue;
                }
            }
        }

        if (create) {
            if (argumentValues == null) {
                argumentValues = new HashSet<MethodExecutionArgumentValue>();
                variablesMap.put(connectionId, argumentValues);
            }

            MethodExecutionArgumentValue argumentValue = new MethodExecutionArgumentValue(name);
            argumentValues.add(argumentValue);
            return argumentValue;

        }
        return null;
    }

    public void cacheVariable(String connectionId, String name, String value) {
        if (StringUtil.isNotEmpty(value)) {
            MethodExecutionArgumentValue argumentValue = getArgumentValue(connectionId, name, true);
            argumentValue.setValue(value);
        }
    }

    /*********************************************
     *            PersistentStateElement         *
     *********************************************/
    public void readState(Element parent) {
        Element argumentValuesElement = parent.getChild("argument-values-cache");
        if (argumentValuesElement != null) {
            this.variablesMap.clear();
            List<Element> connectionElements = argumentValuesElement.getChildren();
            for (Element connectionElement : connectionElements) {
                String connectionId = connectionElement.getAttributeValue("connection-id");
                List<Element> argumentElements = connectionElement.getChildren();
                for (Element argumentElement : argumentElements) {
                    String name = argumentElement.getAttributeValue("name");
                    MethodExecutionArgumentValue argumentValue = getArgumentValue(connectionId, name, true);
                    argumentValue.readState(argumentElement);
                }
            }
        }
    }

    public void writeState(Element parent) {
        Element argumentValuesElement = new Element("argument-values-cache");
        parent.addContent(argumentValuesElement);

        for (String connectionId : variablesMap.keySet()) {
            Set<MethodExecutionArgumentValue> argumentValues = variablesMap.get(connectionId);
            Element connectionElement = new Element("connection");
            connectionElement.setAttribute("connection-id", connectionId);
            argumentValuesElement.addContent(connectionElement);
            for (MethodExecutionArgumentValue argumentValue : argumentValues) {
                if (argumentValue.getValueHistory().size() > 0) {
                    Element argumentElement = new Element("argument");
                    connectionElement.addContent(argumentElement);
                    argumentValue.writeState(argumentElement);
                }
            }
        }
    }
}
