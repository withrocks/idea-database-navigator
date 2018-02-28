package com.dci.intellij.dbn.common.dispose;

import com.intellij.openapi.project.Project;

public interface DisposableProjectComponent extends Disposable {
    Project getProject();
}
