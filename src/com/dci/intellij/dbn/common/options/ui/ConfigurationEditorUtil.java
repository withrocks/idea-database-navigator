package com.dci.intellij.dbn.common.options.ui;

import javax.swing.JTextField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.options.ConfigurationException;

public class ConfigurationEditorUtil {
    public static int validateIntegerInputValue(@NotNull JTextField inputField, @NotNull String name, int min, int max, @Nullable String hint) throws ConfigurationException {
        try {
            int integer = Integer.parseInt(inputField.getText());
            if (min > integer || max < integer) throw new NumberFormatException("Number not in range");
            return integer;
        } catch (NumberFormatException e) {
            inputField.grabFocus();
            inputField.selectAll();
            String message = "Input value for \"" + name + "\" must be an integer between " + min + " and " + max + ".";
            if (hint != null) {
                message = message + " " + hint;
            }
            throw new ConfigurationException(message, "Invalid config value");
        }
    }

    public static String validateStringInputValue(@NotNull JTextField inputField, @NotNull String name, boolean required) throws ConfigurationException {
        String value = inputField.getText().trim();
        if (required && value.length() == 0) {
            String message = "Input value for \"" + name + "\" must be specified";
            throw new ConfigurationException(message, "Invalid config value");
        }
        return value;
    }
    
}
