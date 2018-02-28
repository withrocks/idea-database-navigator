package com.dci.intellij.dbn.module;

import javax.swing.Icon;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.util.IconLoader;


public abstract class DBModuleWizardStep extends ModuleWizardStep {
    protected static final Icon ICON = IconLoader.getIcon("/addmodulewizard.png");
    public DBModuleWizardStep(WizardContext wizardContext, ModuleBuilder moduleBuilder, ModulesProvider modulesProvider) {
        this.wizardContext = wizardContext;
        this.moduleBuilder = moduleBuilder;
        this.modulesProvider = modulesProvider;
    }

    protected WizardContext wizardContext;
    protected ModuleBuilder moduleBuilder;
    protected ModulesProvider modulesProvider;

    public DBModuleBuilder getModuleBuilder() {
        return (DBModuleBuilder) moduleBuilder;
    }

    public Icon getIcon() {
        return ICON;
    }



}
