package com.dci.intellij.dbn.module;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;

public class DBModuleNameLocationWizardStep extends DBModuleWizardStep {

    public DBModuleNameLocationWizardStep(WizardContext wizardContext, ModuleBuilder moduleBuilder, ModulesProvider modulesProvider) {
        super(wizardContext,  moduleBuilder, modulesProvider);
    }

    public JComponent getComponent() {
        return new JPanel();
    }

    public void updateDataModel() {

    }
}
