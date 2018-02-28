package com.dci.intellij.dbn.data.editor.ui;

import java.util.List;

public class BasicListPopupValuesProvider extends ListPopupValuesProviderImpl {
    private  List<String> values;

    public BasicListPopupValuesProvider(String description, List<String> values) {
        super(description, false);
        this.values = values;
    }

    @Override
    public List<String> getValues() {
        return values;
    }
}
