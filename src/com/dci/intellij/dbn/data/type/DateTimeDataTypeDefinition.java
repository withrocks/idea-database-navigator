package com.dci.intellij.dbn.data.type;

import java.lang.reflect.Constructor;
import java.util.Date;
import org.jetbrains.annotations.Nullable;

public class DateTimeDataTypeDefinition extends BasicDataTypeDefinition {
    private Constructor constructor;
    public DateTimeDataTypeDefinition(String name, Class typeClass, int sqlType) {
        super(name, typeClass, sqlType, GenericDataType.DATE_TIME);
        try {
            constructor = typeClass.getConstructor(long.class);
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    @Override
    public Object convert(@Nullable Object object) {
        if (object == null) {
            return null;
        } else {
            assert object instanceof Date;

            Date date = (Date) object;
            if (object.getClass().equals(getTypeClass())) {
                return object;
            }
            try {
                return constructor.newInstance(date.getTime());
            } catch (Throwable e) {
                e.printStackTrace();
                return object;
            }            
        }
    }
}