package com.dci.intellij.dbn.code.psql.style.options;

import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public class PSQLCustomCodeStyleSettings extends CustomCodeStyleSettings {
    private PSQLCodeStyleSettings codeStyleSettings;
    protected PSQLCustomCodeStyleSettings(CodeStyleSettings container) {
        super("PSQLCodeStyleSettings", container);
        codeStyleSettings = new PSQLCodeStyleSettings();
    }

    public PSQLCodeStyleSettings getCodeStyleSettings() {
        return codeStyleSettings;
    }

    public void readExternal(Element parentElement) throws InvalidDataException {
        codeStyleSettings.readConfiguration(parentElement);
    }

    public void writeExternal(Element parentElement, @NotNull CustomCodeStyleSettings parentSettings) throws WriteExternalException {
        codeStyleSettings.writeConfiguration(parentElement);
    }
}