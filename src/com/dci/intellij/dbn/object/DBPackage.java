package com.dci.intellij.dbn.object;

import java.util.List;

public interface DBPackage<P extends DBPackageProcedure, F extends DBPackageFunction> extends DBProgram<P, F> {
    List<DBPackageType> getTypes();
    DBPackageType getType(String name);
}