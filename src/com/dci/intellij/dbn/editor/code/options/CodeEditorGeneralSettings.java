package com.dci.intellij.dbn.editor.code.options;

import org.jdom.Element;

import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.editor.code.options.ui.CodeEditorGeneralSettingsForm;

public class CodeEditorGeneralSettings extends Configuration<CodeEditorGeneralSettingsForm> {
    private boolean showObjectsNavigationGutter = false;
    private boolean showSpecDeclarationNavigationGutter = true;

    public String getDisplayName() {
        return "Code editor general settings";
    }

    public String getHelpTopic() {
        return "codeEditor";
    }

    /*********************************************************
    *                       Settings                        *
    *********************************************************/

    public boolean isShowObjectsNavigationGutter() {
        return showObjectsNavigationGutter;
    }

    public void setShowObjectsNavigationGutter(boolean showObjectsNavigationGutter) {
        this.showObjectsNavigationGutter = showObjectsNavigationGutter;
    }

    public boolean isShowSpecDeclarationNavigationGutter() {
        return showSpecDeclarationNavigationGutter;
    }

    public void setShowSpecDeclarationNavigationGutter(boolean showSpecDeclarationNavigationGutter) {
        this.showSpecDeclarationNavigationGutter = showSpecDeclarationNavigationGutter;
    }

    /****************************************************
     *                   Configuration                  *
     ****************************************************/
    public CodeEditorGeneralSettingsForm createConfigurationEditor() {
        return new CodeEditorGeneralSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "general";
    }

    public void readConfiguration(Element element) {
        showObjectsNavigationGutter = SettingsUtil.getBoolean(element, "show-object-navigation-gutter", showObjectsNavigationGutter);
        showSpecDeclarationNavigationGutter = SettingsUtil.getBoolean(element, "show-spec-declaration-navigation-gutter", showSpecDeclarationNavigationGutter);
    }

    public void writeConfiguration(Element element) {
        SettingsUtil.setBoolean(element, "show-object-navigation-gutter", showObjectsNavigationGutter);
        SettingsUtil.setBoolean(element, "show-spec-declaration-navigation-gutter", showSpecDeclarationNavigationGutter);
    }
}
