package com.dci.intellij.dbn.code.common.style.options;

import org.jdom.Element;

import com.dci.intellij.dbn.common.options.PersistentConfiguration;
import com.dci.intellij.dbn.common.util.NamingUtil;

public class CodeStyleCaseOption implements PersistentConfiguration {
    private String name;
    private CodeStyleCase styleCase;

    public CodeStyleCaseOption(String id, CodeStyleCase styleCase) {
        this.name = id;
        this.styleCase = styleCase;
    }

    public CodeStyleCaseOption() {
    }

    public String getName() {
        return name;
    }

    public CodeStyleCase getStyleCase() {
        return styleCase;
    }

    public void setStyleCase(CodeStyleCase styleCase) {
        this.styleCase = styleCase;
    }

    public String format(String string) {
        if (string != null) {
            switch (styleCase) {
                case UPPER: return string.toUpperCase();
                case LOWER: return string.toLowerCase();
                case CAPITALIZED: return NamingUtil.capitalize(string);
                case PRESERVE: return string;
            }
        }
        return string;
    }

    /*********************************************************
     *                 PersistentConfiguration               *
     *********************************************************/
    public void readConfiguration(Element element) {
        name = element.getAttributeValue("name");
        String style = element.getAttributeValue("value");
        styleCase =
                style.equals("upper") ? CodeStyleCase.UPPER :
                style.equals("lower") ? CodeStyleCase.LOWER :
                style.equals("capitalized") ? CodeStyleCase.CAPITALIZED :
                style.equals("preserve") ? CodeStyleCase.PRESERVE : CodeStyleCase.PRESERVE;
    }

    public void writeConfiguration(Element element) {
        String value =
                styleCase == CodeStyleCase.UPPER ? "upper" :
                styleCase == CodeStyleCase.LOWER ? "lower" :
                styleCase == CodeStyleCase.CAPITALIZED ? "capitalized" :
                styleCase == CodeStyleCase.PRESERVE ? "preserve" :  null;

        element.setAttribute("name", name);
        element.setAttribute("value", value);
    }
}
