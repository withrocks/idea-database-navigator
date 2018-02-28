package com.dci.intellij.dbn.common.options;

import org.jdom.Element;

import com.dci.intellij.dbn.common.options.ui.CompositeConfigurationEditorForm;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.intellij.openapi.options.ConfigurationException;

public abstract class CompositeConfiguration<T extends CompositeConfigurationEditorForm> extends Configuration<T> {
    private Configuration[] configurations;

    public final Configuration[] getConfigurations() {
        if (configurations == null) configurations = createConfigurations();
        return configurations;
    }

    protected abstract Configuration[] createConfigurations();

    @Override
    public final boolean isModified() {
        for (Configuration configuration : getConfigurations()) {
            if (configuration.isModified()) return true;
        }
        return super.isModified();
    }

    @Override
    public final void apply() throws ConfigurationException {
        for (Configuration configuration : getConfigurations()) {
            configuration.apply();
        }
        super.apply();
        if (!CommonUtil.isCalledThrough(Configuration.class)) {
            notifyChanges();
        }
        onApply();
    }

    @Override
    public final void reset() {
        for (Configuration configuration : getConfigurations()) {
            configuration.reset();
        }
        super.reset();
    }

    @Override
    public void disposeUIResources() {
        for (Configuration configuration : getConfigurations()) {
            configuration.disposeUIResources();
        }
        super.disposeUIResources();
    }

    public void readConfiguration(Element element) {
        for (Configuration configuration : getConfigurations()) {
            readConfiguration(element, configuration);
        }
    }

    public void writeConfiguration(Element element) {
        for (Configuration configuration : getConfigurations()) {
            writeConfiguration(element, configuration);
        }
    }
}
