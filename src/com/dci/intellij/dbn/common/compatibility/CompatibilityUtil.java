package com.dci.intellij.dbn.common.compatibility;

import javax.swing.JComponent;
import javax.swing.JTextField;
import java.awt.Color;

import com.intellij.find.editorHeaderActions.Utils;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.ui.components.JBList;
import com.intellij.util.ui.UIUtil;

public class CompatibilityUtil {
    public static Color getEditorBackgroundColor(EditorEx editorEx) {
        return editorEx.getBackgroundColor();
    }

    public static ModuleType getModuleType(Module module) {
        //return module.getModuleType();
        return ModuleType.get(module);
    }

    public static void showSearchCompletionPopup(boolean byClickingToolbarButton, JComponent toolbarComponent, JBList list, String title, JTextField textField) {
        //Utils.showCompletionPopup(byClickingToolbarButton ? toolbarComponent : null, list, title, textField);
        Utils.showCompletionPopup(byClickingToolbarButton ? toolbarComponent : null, list, title, textField, "");
    }

    public static void setSmallerFontForChildren(JComponent component) {
        Utils.setSmallerFontForChildren(component);
    }

    public static void setSmallerFont(JComponent component) {
        Utils.setSmallerFont(component);
    }

    public static boolean isUnderGTKLookAndFeel() {
        return UIUtil.isUnderGTKLookAndFeel();
    }
}
