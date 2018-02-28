package com.dci.intellij.dbn.data.editor.ui;

import java.util.List;

public interface ListPopupValuesProvider {
    String getDescription();
    List<String> getValues();
    List<String> getSecondaryValues();
    boolean isLongLoading();
}
