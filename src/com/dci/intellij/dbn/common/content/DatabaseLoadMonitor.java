package com.dci.intellij.dbn.common.content;

import com.dci.intellij.dbn.common.util.ThreadLocalFlag;

public class DatabaseLoadMonitor {
    private static ThreadLocalFlag loadingInBackground = new ThreadLocalFlag(false);
    private static ThreadLocalFlag ensureDataLoaded = new ThreadLocalFlag(true);

    public static boolean isLoadingInBackground() {
        // default false
        return loadingInBackground.get();
    }

    public static void startBackgroundLoad() {
        loadingInBackground.set(true);
    }


    public static void endBackgroundLoad() {
        loadingInBackground.set(false);
    }

    public static boolean isEnsureDataLoaded() {
        // default true
        return ensureDataLoaded.get();
    }

    public static void setEnsureDataLoaded(boolean value) {
        ensureDataLoaded.set(value);
    }
}
