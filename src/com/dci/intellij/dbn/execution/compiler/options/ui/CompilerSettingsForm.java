package com.dci.intellij.dbn.execution.compiler.options.ui;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.ui.DBNComboBox;
import com.dci.intellij.dbn.common.ui.Presentable;
import com.dci.intellij.dbn.execution.compiler.CompileDependenciesOption;
import com.dci.intellij.dbn.execution.compiler.CompileTypeOption;
import com.dci.intellij.dbn.execution.compiler.options.CompilerSettings;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import javax.swing.JPanel;

public class CompilerSettingsForm extends ConfigurationEditorForm<CompilerSettings> {
    private JPanel mainPanel;
    private DBNComboBox<CompileTypeOption> compileTypeComboBox;
    private DBNComboBox<CompileDependenciesOption> compileDependenciesComboBox;
    private DBNComboBox<ShowControlOption> showControlsComboBox;


    public CompilerSettingsForm(CompilerSettings settings) {
        super(settings);

        showControlsComboBox.setValues(
                ShowControlOption.ALWAYS,
                ShowControlOption.WHEN_INVALID);

        compileTypeComboBox.setValues(
                CompileTypeOption.NORMAL,
                CompileTypeOption.DEBUG,
                CompileTypeOption.KEEP,
                CompileTypeOption.ASK);

        compileDependenciesComboBox.setValues(
                CompileDependenciesOption.YES,
                CompileDependenciesOption.NO,
                CompileDependenciesOption.ASK);


        updateBorderTitleForeground(mainPanel);
        resetFormChanges();

        registerComponent(mainPanel);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyFormChanges() throws ConfigurationException {
        CompilerSettings settings = getConfiguration();
        settings.setCompileTypeOption(compileTypeComboBox.getSelectedValue());
        settings.setCompileDependenciesOption(compileDependenciesComboBox.getSelectedValue());
        ShowControlOption showControlOption = showControlsComboBox.getSelectedValue();
        settings.setAlwaysShowCompilerControls(showControlOption != null && showControlOption.getValue());
    }

    public void resetFormChanges() {
        CompilerSettings settings = getConfiguration();
        compileTypeComboBox.setSelectedValue(settings.getCompileTypeOption());
        compileDependenciesComboBox.setSelectedValue(settings.getCompileDependenciesOption());
        showControlsComboBox.setSelectedValue(
                settings.alwaysShowCompilerControls() ?
                        ShowControlOption.ALWAYS:
                        ShowControlOption.WHEN_INVALID);
    }

    private enum ShowControlOption implements Presentable {
        ALWAYS("Always", true),
        WHEN_INVALID("When object invalid", false);

        private String name;
        private boolean value;

        ShowControlOption(String name, boolean value) {
            this.name = name;
            this.value = value;
        }

        @NotNull
        @Override
        public String getName() {
            return name;
        }

        @Nullable
        @Override
        public Icon getIcon() {
            return null;
        }

        public boolean getValue() {
            return value;
        }
    }
}
