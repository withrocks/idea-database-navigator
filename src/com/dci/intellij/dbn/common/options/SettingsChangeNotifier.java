package com.dci.intellij.dbn.common.options;

public abstract class SettingsChangeNotifier {
    public SettingsChangeNotifier() {
        Configuration.registerChangeNotifier(this);
    }

    public abstract void notifyChanges();
}
