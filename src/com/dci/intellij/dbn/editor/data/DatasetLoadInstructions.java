package com.dci.intellij.dbn.editor.data;

public class DatasetLoadInstructions {
    private boolean useCurrentFilter;
    private boolean keepChanges;
    private boolean isDeliberateAction;
    private boolean rebuild;

    public DatasetLoadInstructions(boolean useCurrentFilter, boolean keepChanges, boolean isDeliberateAction, boolean rebuild) {
        this.useCurrentFilter = useCurrentFilter;
        this.keepChanges = keepChanges;
        this.isDeliberateAction = isDeliberateAction;
        this.rebuild = rebuild;
    }

    public boolean isUseCurrentFilter() {
        return useCurrentFilter;
    }

    public boolean isKeepChanges() {
        return keepChanges;
    }

    public boolean isDeliberateAction() {
        return isDeliberateAction;
    }

    public boolean isRebuild() {
        return rebuild;
    }

    public void setUseCurrentFilter(boolean useCurrentFilter) {
        this.useCurrentFilter = useCurrentFilter;
    }

    public void setKeepChanges(boolean keepChanges) {
        this.keepChanges = keepChanges;
    }

    public void setDeliberateAction(boolean isDeliberateAction) {
        this.isDeliberateAction = isDeliberateAction;
    }

    public void setRebuild(boolean rebuild) {
        this.rebuild = rebuild;
    }

    public DatasetLoadInstructions clone( ) {
        return new DatasetLoadInstructions(useCurrentFilter, keepChanges, isDeliberateAction, rebuild);
    }
}
