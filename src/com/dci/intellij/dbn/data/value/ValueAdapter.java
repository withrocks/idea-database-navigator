package com.dci.intellij.dbn.data.value;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.data.type.GenericDataType;
import com.intellij.openapi.diagnostic.Logger;

public abstract class ValueAdapter<T> {
    private static final Logger LOGGER = LoggerFactory.createLogger();

    public abstract GenericDataType getGenericDataType();
    public abstract @Nullable T read() throws SQLException;
    public abstract void write(Connection connection, PreparedStatement preparedStatement, int parameterIndex, @Nullable T value) throws SQLException;
    public abstract void write(Connection connection, ResultSet resultSet, int columnIndex, @Nullable T value) throws SQLException;
    public abstract String getDisplayValue();

    public static final Map<GenericDataType, Class<? extends ValueAdapter>> REGISTRY = new EnumMap<GenericDataType, Class<? extends ValueAdapter>>(GenericDataType.class);
    static {
        REGISTRY.put(GenericDataType.ARRAY, ArrayValue.class);
        REGISTRY.put(GenericDataType.BLOB, BlobValue.class);
        REGISTRY.put(GenericDataType.CLOB, ClobValue.class);
        REGISTRY.put(GenericDataType.XMLTYPE, XmlTypeValue.class);
    }

    public static boolean supports(GenericDataType genericDataType) {
        return REGISTRY.containsKey(genericDataType);
    }

    public static ValueAdapter create(GenericDataType genericDataType) throws SQLException {
        try {
            Class<? extends ValueAdapter> valueAdapterClass = REGISTRY.get(genericDataType);
            return valueAdapterClass.newInstance();
        } catch (Throwable e) {
            handleException(e, genericDataType);
        }
        return null;
    }

    public static ValueAdapter create(GenericDataType genericDataType, ResultSet resultSet, int columnIndex) throws SQLException {
        try {
            Class<? extends ValueAdapter> valueAdapterClass = REGISTRY.get(genericDataType);
            Constructor<? extends ValueAdapter> constructor = valueAdapterClass.getConstructor(ResultSet.class, int.class);
            return constructor.newInstance(resultSet, columnIndex);
        } catch (Throwable e) {
            handleException(e, genericDataType);
        }
        return null;
    }

    public static ValueAdapter create(GenericDataType genericDataType, CallableStatement callableStatement, int parameterIndex) throws SQLException {
        Class<? extends ValueAdapter> valueAdapterClass = REGISTRY.get(genericDataType);
        try {
            Constructor<? extends ValueAdapter> constructor = valueAdapterClass.getConstructor(CallableStatement.class, int.class);
            return constructor.newInstance(callableStatement, parameterIndex);
        } catch (Throwable e) {
            handleException(e, genericDataType);
            return null;
        }
    }

    private static void handleException(Throwable e, GenericDataType genericDataType) throws SQLException {
        if (e instanceof InvocationTargetException) {
            InvocationTargetException invocationTargetException = (InvocationTargetException) e;
            e = invocationTargetException.getTargetException();
        }
        if (e instanceof SQLException) {
            throw (SQLException) e;
        } else {
            throw new SQLException("Error creating value adapter for generic type " + genericDataType.name() + '.', e);
        }
    }
}
