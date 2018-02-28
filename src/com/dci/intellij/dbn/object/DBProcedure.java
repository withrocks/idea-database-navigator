package com.dci.intellij.dbn.object;

import java.util.List;

public interface DBProcedure extends DBMethod {
    List<DBArgument> getArguments();
    DBArgument getArgument(String name);
}