package com.dci.intellij.dbn.data.type;

import java.lang.reflect.Constructor;
import org.jetbrains.annotations.Nullable;

public class NumericDataTypeDefinition extends BasicDataTypeDefinition {
    private Constructor constructor;
    public NumericDataTypeDefinition(String name, Class typeClass, int sqlType) {
        super(name, typeClass, sqlType, GenericDataType.NUMERIC);
        try {
            constructor = typeClass.getConstructor(String.class);
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    @Override
    public Object convert(@Nullable Object object) {
        if (object == null) {
            return null;
        } else {
            assert object instanceof Number;

            Number number = (Number) object;
            if (object.getClass().equals(getTypeClass())) {
                return object;
            }
            try {
                return constructor.newInstance(number.toString());
            } catch (Throwable e) {
                e.printStackTrace();
                return object;
            }
        }
    }
}
