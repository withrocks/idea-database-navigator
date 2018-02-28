package com.dci.intellij.dbn.common.load;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;

public class ProgressMonitor {
    public static String getTaskDescription() {
        ProgressIndicator progressIndicator = ProgressManager.getInstance().getProgressIndicator();
        if (progressIndicator != null) {
            return progressIndicator.getText();
        }
        return null;
    }

    public static void setTaskDescription(String description) {
        ProgressIndicator progressIndicator = ProgressManager.getInstance().getProgressIndicator();
        if (progressIndicator != null) {
            progressIndicator.setText(description);
        }
    }

    public static void setSubtaskDescription(String subtaskDescription) {
        ProgressIndicator progressIndicator = ProgressManager.getInstance().getProgressIndicator();
        if (progressIndicator != null) {
            progressIndicator.setText2(subtaskDescription);
        }
    }

    public static boolean isCancelled() {
        ProgressIndicator progressIndicator = ProgressManager.getInstance().getProgressIndicator();
        return progressIndicator != null && progressIndicator.isCanceled();
    }



}
