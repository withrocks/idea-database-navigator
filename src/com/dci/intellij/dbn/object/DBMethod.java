package com.dci.intellij.dbn.object;

import java.util.List;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.object.common.DBSchemaObject;

public interface DBMethod extends DBSchemaObject {
    List<DBArgument> getArguments();
    DBArgument getArgument(String name);
    DBArgument getReturnArgument();
    DBProgram getProgram();
    String getMethodType();

    int getPosition();

    boolean isProgramMethod();
    boolean isDeterministic();
    boolean hasDeclaredArguments();
    @NotNull
    DBLanguage getLanguage();
}
