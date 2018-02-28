package com.dci.intellij.dbn.code.common.style.options;

import com.dci.intellij.dbn.code.common.style.options.ui.CodeStyleCaseSettingsForm;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;

public abstract class CodeStyleCaseSettings extends Configuration<CodeStyleCaseSettingsForm> {
    private List<CodeStyleCaseOption> options = new ArrayList<CodeStyleCaseOption>();
    private boolean enabled = false;

    public CodeStyleCaseSettings() {
        options.add(new CodeStyleCaseOption("KEYWORD_CASE", CodeStyleCase.LOWER));
        options.add(new CodeStyleCaseOption("FUNCTION_CASE", CodeStyleCase.LOWER));
        options.add(new CodeStyleCaseOption("PARAMETER_CASE", CodeStyleCase.LOWER));
        options.add(new CodeStyleCaseOption("DATATYPE_CASE", CodeStyleCase.LOWER));
        options.add(new CodeStyleCaseOption("OBJECT_CASE", CodeStyleCase.PRESERVE));
    }


    public String getDisplayName() {
        return "Case Options";
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public CodeStyleCaseOption getKeywordCaseOption() {
        return getCodeStyleCaseOption("KEYWORD_CASE");
    }

    public CodeStyleCaseOption getFunctionCaseOption() {
        return getCodeStyleCaseOption("FUNCTION_CASE");
    }

    public CodeStyleCaseOption getParameterCaseOption() {
        return getCodeStyleCaseOption("PARAMETER_CASE");
    }

    public CodeStyleCaseOption getDatatypeCaseOption() {
        return getCodeStyleCaseOption("DATATYPE_CASE");
    }


    public CodeStyleCaseOption getObjectCaseOption() {
        return getCodeStyleCaseOption("OBJECT_CASE");
    }

    private CodeStyleCaseOption getCodeStyleCaseOption(String name) {
        for (CodeStyleCaseOption option : options) {
            if (option.getName().equals(name)) return option;
        }
        return null;
    }

    /*********************************************************
     *                     Configuration                     *
     *********************************************************/
    public CodeStyleCaseSettingsForm createConfigurationEditor() {
        return new CodeStyleCaseSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "case-options";
    }

    public void readConfiguration(Element element) {
        enabled = SettingsUtil.getBooleanAttribute(element, "enabled", enabled);
        for (Object object : element.getChildren()) {
            Element optionElement = (Element) object;
            String name = optionElement.getAttributeValue("name");
            CodeStyleCaseOption option = getCodeStyleCaseOption(name);
            option.readConfiguration(optionElement);
        }
    }

    public void writeConfiguration(Element element) {
        SettingsUtil.setBooleanAttribute(element, "enabled", enabled);
        for (CodeStyleCaseOption option : options) {
            Element optionElement = new Element("option");
            option.writeConfiguration(optionElement);
            element.addContent(optionElement);
        }
    }
}
