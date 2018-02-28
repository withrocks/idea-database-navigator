package com.dci.intellij.dbn.data.editor.text;

import com.dci.intellij.dbn.data.editor.ui.UserValueHolder;

public interface TextEditorAdapter {
    UserValueHolder getUserValueHolder();

    void afterUpdate();
}
