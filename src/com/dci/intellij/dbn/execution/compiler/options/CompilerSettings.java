package com.dci.intellij.dbn.execution.compiler.options;

import org.jdom.Element;

import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.execution.compiler.CompileDependenciesOption;
import com.dci.intellij.dbn.execution.compiler.CompileTypeOption;
import com.dci.intellij.dbn.execution.compiler.options.ui.CompilerSettingsForm;

public class CompilerSettings extends Configuration<CompilerSettingsForm>{
    private CompileTypeOption compileTypeOption = CompileTypeOption.KEEP;
    private CompileDependenciesOption compileDependenciesOption = CompileDependenciesOption.ASK;
    private boolean alwaysShowCompilerControls = false;

    public String getDisplayName() {
        return "Data editor general settings";
    }

    public String getHelpTopic() {
        return "executionEngine";
    }

    /*********************************************************
    *                       Settings                        *
    *********************************************************/

    public CompileTypeOption getCompileTypeOption() {
        return compileTypeOption;
    }

    public void setCompileTypeOption(CompileTypeOption compileTypeOption) {
        this.compileTypeOption = compileTypeOption;
    }

    public CompileDependenciesOption getCompileDependenciesOption() {
        return compileDependenciesOption;
    }

    public void setCompileDependenciesOption(CompileDependenciesOption compileDependenciesOption) {
        this.compileDependenciesOption = compileDependenciesOption;
    }

    public boolean alwaysShowCompilerControls() {
        return alwaysShowCompilerControls;
    }

    public void setAlwaysShowCompilerControls(boolean alwaysShowCompilerControls) {
        this.alwaysShowCompilerControls = alwaysShowCompilerControls;
    }

    /****************************************************
     *                   Configuration                  *
     ****************************************************/
    public CompilerSettingsForm createConfigurationEditor() {
        return new CompilerSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "compiler";
    }

    public void readConfiguration(Element element) {
        compileTypeOption = CompileTypeOption.get(SettingsUtil.getString(element, "compile-type", compileTypeOption.name()));
        compileDependenciesOption = CompileDependenciesOption.get(SettingsUtil.getString(element, "compile-dependencies", compileDependenciesOption.name()));
        alwaysShowCompilerControls = SettingsUtil.getBoolean(element, "always-show-controls", alwaysShowCompilerControls);
    }

    public void writeConfiguration(Element element) {
        SettingsUtil.setString(element, "compile-type", compileTypeOption.name());
        SettingsUtil.setString(element, "compile-dependencies", compileDependenciesOption.name());
        SettingsUtil.setBoolean(element, "always-show-controls", alwaysShowCompilerControls);
    }
}
