package com.dci.intellij.dbn.debugger.execution;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;

public class DBProgramRunConfigurationFactory extends ConfigurationFactory {
    protected DBProgramRunConfigurationFactory(@org.jetbrains.annotations.NotNull ConfigurationType type) {
        super(type);
    }

    @Override
    public Icon getIcon(@NotNull RunConfiguration configuration) {
        DBProgramRunConfiguration runConfiguration = (DBProgramRunConfiguration) configuration;
        MethodExecutionInput executionInput = runConfiguration.getExecutionInput();
        if (executionInput == null) {
            return super.getIcon();
        } else {
            DBObjectRef<DBMethod> methodRef = executionInput.getMethodRef();
            DBMethod method = methodRef.get();
            return method == null ? methodRef.getObjectType().getIcon() : method.getIcon();
        }
    }

    @Override
    public RunConfiguration createTemplateConfiguration(Project project) {
        return new DBProgramRunConfiguration(project, this, "");
    }

    public DBProgramRunConfiguration createConfiguration(DBMethod method) {
        String name = DatabaseDebuggerManager.createConfigurationName(method);
        DBProgramRunConfiguration runConfiguration = new DBProgramRunConfiguration(method.getProject(), this, name);
        MethodExecutionManager executionManager = MethodExecutionManager.getInstance(method.getProject());
        MethodExecutionInput executionInput = executionManager.getExecutionInput(method);
        runConfiguration.setExecutionInput(executionInput);
        return runConfiguration;
    }

}
