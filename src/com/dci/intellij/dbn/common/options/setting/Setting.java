package com.dci.intellij.dbn.common.options.setting;

import com.dci.intellij.dbn.common.util.CommonUtil;
import com.intellij.openapi.options.ConfigurationException;

public abstract class Setting<T, E> {
    private T value;
    private String name;

    protected Setting(String configName, T value) {
        this.name = configName;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public T value() {
        return value;
    }

    public boolean setValue(T value) {
        boolean response = !CommonUtil.safeEqual(this.value, value);
        this.value = value;
        return response;
    }

    public T getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "[" + getClass().getSimpleName() + "] " + name + " = " + value;
    }

    public abstract boolean applyChanges(E component) throws ConfigurationException;

    public abstract void resetChanges(E component);
}
