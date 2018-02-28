package com.dci.intellij.dbn.common.util;

import java.awt.Color;

import com.intellij.openapi.editor.colors.ColorKey;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.ui.SimpleTextAttributes;

public class TextAttributesUtil {
    
    public static SimpleTextAttributes getSimpleTextAttributes(TextAttributesKey textAttributesKey) {
        EditorColorsManager colorManager = EditorColorsManager.getInstance();
        TextAttributes textAttributes = colorManager.getGlobalScheme().getAttributes(textAttributesKey);
        return new SimpleTextAttributes(
                textAttributes.getBackgroundColor(),
                textAttributes.getForegroundColor(),
                textAttributes.getEffectColor(),
                textAttributes.getFontType());
    }
    
    public static Color getColor(ColorKey colorKey) {
        EditorColorsManager colorManager = EditorColorsManager.getInstance();
        return colorManager.getGlobalScheme().getColor(colorKey);
    }
    
}
