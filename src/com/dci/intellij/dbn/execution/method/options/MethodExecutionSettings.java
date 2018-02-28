package com.dci.intellij.dbn.execution.method.options;

import org.jdom.Element;

import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.execution.method.options.ui.MethodExecutionSettingsForm;

public class MethodExecutionSettings extends Configuration{
    private int executionTimeout = 30;
    private int debugExecutionTimeout = 600;
    private int parameterHistorySize = 10;

    public String getDisplayName() {
        return "Method execution settings";
    }

    public String getHelpTopic() {
        return "executionEngine";
    }

    /*********************************************************
    *                       Settings                        *
    *********************************************************/

    public int getExecutionTimeout() {
        return executionTimeout;
    }

    public void setExecutionTimeout(int executionTimeout) {
        this.executionTimeout = executionTimeout;
    }

    public int getDebugExecutionTimeout() {
        return debugExecutionTimeout;
    }

    public void setDebugExecutionTimeout(int debugExecutionTimeout) {
        this.debugExecutionTimeout = debugExecutionTimeout;
    }

    public int getParameterHistorySize() {
        return parameterHistorySize;
    }

    public void setParameterHistorySize(int parameterHistorySize) {
        this.parameterHistorySize = parameterHistorySize;
    }

    /****************************************************
     *                   Configuration                  *
     ****************************************************/
    public ConfigurationEditorForm createConfigurationEditor() {
        return new MethodExecutionSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "statement-execution";
    }

    public void readConfiguration(Element element) {
        executionTimeout = SettingsUtil.getInteger(element, "execution-timeout", executionTimeout);
        debugExecutionTimeout = SettingsUtil.getInteger(element, "debug-execution-timeout", debugExecutionTimeout);
        parameterHistorySize = SettingsUtil.getInteger(element, "parameter-history-size", parameterHistorySize);

    }

    public void writeConfiguration(Element element) {
        SettingsUtil.setInteger(element, "execution-timeout", executionTimeout);
        SettingsUtil.setInteger(element, "debug-execution-timeout", debugExecutionTimeout);
        SettingsUtil.setInteger(element, "parameter-history-size", parameterHistorySize);
    }
}
