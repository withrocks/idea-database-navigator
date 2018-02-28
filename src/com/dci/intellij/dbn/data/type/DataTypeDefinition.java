package com.dci.intellij.dbn.data.type;

import org.jetbrains.annotations.Nullable;

public interface DataTypeDefinition {
    String getName();
    Class getTypeClass();
    int getSqlType();

    boolean isPseudoNative();

    GenericDataType getGenericDataType();
    Object convert(@Nullable Object object);
    @Nullable String getContentTypeName();
}
