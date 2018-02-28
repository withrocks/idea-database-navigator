package com.dci.intellij.dbn.execution.statement.variables;

import com.dci.intellij.dbn.common.state.PersistentStateElement;
import com.intellij.openapi.vfs.VirtualFile;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class StatementExecutionVariablesCache implements PersistentStateElement<Element> {
    private Map<String, Set<StatementExecutionVariable>> fileVariablesMap = new THashMap<String, Set<StatementExecutionVariable>>();

    public Set<StatementExecutionVariable> getVariables(VirtualFile virtualFile) {
        String fileUrl = virtualFile.getUrl();
        Set<StatementExecutionVariable> fileVariables = this.fileVariablesMap.get(fileUrl);
        if (fileVariables == null) {
            fileVariables = new THashSet<StatementExecutionVariable>();
            this.fileVariablesMap.put(fileUrl, fileVariables);
        }
        return fileVariables;
    }

    public void cacheVariable(VirtualFile virtualFile, StatementExecutionVariable executionVariable) {
        Set<StatementExecutionVariable> variables = getVariables(virtualFile);
        for (StatementExecutionVariable variable : variables) {
            if (variable.getName().equals(executionVariable.getName())) {
                variable.setValue(executionVariable.getValue());
                return;
            }
        }
        variables.add(new StatementExecutionVariable(executionVariable));
    }

    @Nullable
    public StatementExecutionVariable getVariable(VirtualFile virtualFile, String name) {
        Set<StatementExecutionVariable> variables = getVariables(virtualFile);
        for (StatementExecutionVariable variable : variables) {
            if (variable.getName().equalsIgnoreCase(name)) {
                return variable;
            }
        }
        return null;
    }

    /*********************************************
     *            PersistentStateElement         *
     *********************************************/
    public void readState(Element parent) {
        Element variablesElement = parent.getChild("execution-variables");
        if (variablesElement != null) {
            this.fileVariablesMap.clear();
            List<Element> fileElements = variablesElement.getChildren();
            for (Element fileElement : fileElements) {
                String filePath = fileElement.getAttributeValue("path");

                Set<StatementExecutionVariable> fileVariables = new THashSet<StatementExecutionVariable>();
                this.fileVariablesMap.put(filePath, fileVariables);

                List<Element> variableElements = fileElement.getChildren();
                for (Element variableElement : variableElements) {
                    StatementExecutionVariable executionVariable = new StatementExecutionVariable(variableElement);
                    fileVariables.add(executionVariable);
                }
            }
        }
    }

    public void writeState(Element parent) {
        Element variablesElement = new Element("execution-variables");
        parent.addContent(variablesElement);

        for (String fileUrl : fileVariablesMap.keySet()) {
            Element fileElement = new Element("file");
            fileElement.setAttribute("path", fileUrl);
            Set<StatementExecutionVariable> executionVariables = fileVariablesMap.get(fileUrl);
            for (StatementExecutionVariable executionVariable : executionVariables) {
                Element variableElement = executionVariable.getState();
                fileElement.addContent(variableElement);
            }
            variablesElement.addContent(fileElement);
        }
    }
}
