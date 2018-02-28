package com.dci.intellij.dbn.common.environment.options;


import com.dci.intellij.dbn.common.options.PersistentConfiguration;
import com.dci.intellij.dbn.common.options.setting.BooleanSetting;
import org.jdom.Element;

public class EnvironmentVisibilitySettings implements PersistentConfiguration {
    private BooleanSetting connectionTabs = new BooleanSetting("connection-tabs", true);
    private BooleanSetting objectEditorTabs = new BooleanSetting("object-editor-tabs", true);
    private BooleanSetting scriptEditorTabs = new BooleanSetting("script-editor-tabs", false);
    private BooleanSetting dialogHeaders = new BooleanSetting("dialog-headers", true);
    private BooleanSetting executionResultTabs = new BooleanSetting("execution-result-tabs", true);

    public BooleanSetting getConnectionTabs() {
        return connectionTabs;
    }

    public BooleanSetting getObjectEditorTabs() {
        return objectEditorTabs;
    }

    public BooleanSetting getScriptEditorTabs() {
        return scriptEditorTabs;
    }

    public BooleanSetting getDialogHeaders() {
        return dialogHeaders;
    }

    public BooleanSetting getExecutionResultTabs() {
        return executionResultTabs;
    }

    @Override
    public void readConfiguration(Element element) {
        connectionTabs.readConfiguration(element);
        dialogHeaders.readConfiguration(element);
        objectEditorTabs.readConfiguration(element);
        scriptEditorTabs.readConfiguration(element);
        executionResultTabs.readConfiguration(element);
    }

    @Override
    public void writeConfiguration(Element element) {
        connectionTabs.writeConfiguration(element);
        dialogHeaders.writeConfiguration(element);
        objectEditorTabs.writeConfiguration(element);
        scriptEditorTabs.writeConfiguration(element);
        executionResultTabs.writeConfiguration(element);
    }
}
