package com.dci.intellij.dbn.code.sql.style.options;

import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public class SQLCustomCodeStyleSettings extends CustomCodeStyleSettings {
    private SQLCodeStyleSettings codeStyleSettings;
    protected SQLCustomCodeStyleSettings(CodeStyleSettings container) {
        super("SQLCodeStyleSettings", container);
        codeStyleSettings = new SQLCodeStyleSettings();
    }

    public SQLCodeStyleSettings getCodeStyleSettings() {
        return codeStyleSettings;
    }

    public void readExternal(Element parentElement) throws InvalidDataException {
        codeStyleSettings.readConfiguration(parentElement);
    }

    public void writeExternal(Element parentElement, @NotNull CustomCodeStyleSettings parentSettings) throws WriteExternalException {
        codeStyleSettings.writeConfiguration(parentElement);
    }
}
