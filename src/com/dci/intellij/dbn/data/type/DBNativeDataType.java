package com.dci.intellij.dbn.data.type;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.content.DynamicContentElement;
import com.dci.intellij.dbn.data.value.ValueAdapter;
import com.intellij.openapi.diagnostic.Logger;

public class DBNativeDataType implements DynamicContentElement{
    private static final Logger LOGGER = LoggerFactory.createLogger();

    private DataTypeDefinition dataTypeDefinition;

    public DBNativeDataType(DataTypeDefinition dataTypeDefinition) {
        this.dataTypeDefinition = dataTypeDefinition;
    }

    public boolean isDisposed() {
        return false;
    }

    public String getName() {
        return dataTypeDefinition.getName();
    }

    @Override
    public int getOverload() {
        return 0;
    }

    public DataTypeDefinition getDataTypeDefinition() {
        return dataTypeDefinition;
    }
    
    public GenericDataType getGenericDataType() {
        return dataTypeDefinition.getGenericDataType();
    }

    public boolean isPseudoNative() {
        return dataTypeDefinition.isPseudoNative();
    }

    public boolean isLargeObject() {
        return getGenericDataType().isLOB();
    }

    public Object getValueFromResultSet(ResultSet resultSet, int columnIndex) throws SQLException {
        // FIXME: add support for stream updatable types

        GenericDataType genericDataType = dataTypeDefinition.getGenericDataType();
        if (ValueAdapter.supports(genericDataType)) {
            return ValueAdapter.create(genericDataType, resultSet, columnIndex);
        }

/*
        if (genericDataType == GenericDataType.BLOB) return new BlobValue(resultSet, columnIndex);
        if (genericDataType == GenericDataType.CLOB) return new ClobValue(resultSet, columnIndex);
        if (genericDataType == GenericDataType.XMLTYPE) return new XmlTypeValue((OracleResultSet) resultSet, columnIndex);
        if (genericDataType == GenericDataType.ARRAY) return new ArrayValue(resultSet, columnIndex);
*/
        if (genericDataType == GenericDataType.ROWID) return "[ROWID]";
        if (genericDataType == GenericDataType.FILE) return "[FILE]";

        Class clazz = dataTypeDefinition.getTypeClass();
        if (Number.class.isAssignableFrom(clazz) && resultSet.getString(columnIndex) == null) {
            // mysql converts null numbers to 0!!!
            // FIXME make this database dependent (e.g. in CompatibilityInterface).
            return null;
        }
        try {
            return
                    clazz == String.class ? resultSet.getString(columnIndex) :
                    clazz == Byte.class ? resultSet.getByte(columnIndex) :
                    clazz == Short.class ? resultSet.getShort(columnIndex) :
                    clazz == Integer.class ? resultSet.getInt(columnIndex) :
                    clazz == Long.class ? resultSet.getLong(columnIndex) :
                    clazz == Float.class ? resultSet.getFloat(columnIndex) :
                    clazz == Double.class ? resultSet.getDouble(columnIndex) :
                    clazz == BigDecimal.class ? resultSet.getBigDecimal(columnIndex) :
                    clazz == Date.class ? resultSet.getDate(columnIndex) :
                    clazz == Time.class ? resultSet.getTime(columnIndex) :
                    clazz == Timestamp.class ? resultSet.getTimestamp(columnIndex) :
                    //clazz == Array.class ? resultSet.getArray(columnIndex) :
                            resultSet.getObject(columnIndex);
        } catch (SQLException e) {
            Object object = resultSet.getObject(columnIndex);
            LOGGER.error("Error resolving result set value for '" + object + "'. (data type definition " + dataTypeDefinition + ')', e);
            return object;
        }
    }

    public void setValueToResultSet(ResultSet resultSet, int columnIndex, Object value) throws SQLException {
        // FIXME: add support for stream updatable types
        GenericDataType genericDataType = dataTypeDefinition.getGenericDataType();
        if (genericDataType == GenericDataType.BLOB) return;
        if (genericDataType == GenericDataType.CLOB) return;
        if (genericDataType == GenericDataType.XMLTYPE) return;
        if (genericDataType == GenericDataType.ROWID) return;
        if (genericDataType == GenericDataType.FILE) return;
        if (genericDataType == GenericDataType.ARRAY) return;

        if (value == null) {
            resultSet.updateObject(columnIndex, null);
        } else {
            Class clazz = dataTypeDefinition.getTypeClass();
            if (value.getClass().isAssignableFrom(clazz)) {
                if(clazz == String.class) resultSet.updateString(columnIndex, (String) value); else
                if(clazz == Byte.class) resultSet.updateByte(columnIndex, (Byte) value); else
                if(clazz == Short.class) resultSet.updateShort(columnIndex, (Short) value); else
                if(clazz == Integer.class) resultSet.updateInt(columnIndex, (Integer) value); else
                if(clazz == Long.class) resultSet.updateLong(columnIndex, (Long) value); else
                if(clazz == Float.class) resultSet.updateFloat(columnIndex, (Float) value); else
                if(clazz == Double.class) resultSet.updateDouble(columnIndex, (Double) value); else
                if(clazz == BigDecimal.class) resultSet.updateBigDecimal(columnIndex, (BigDecimal) value); else
                if(clazz == Date.class) resultSet.updateDate(columnIndex, (Date) value); else
                if(clazz == Time.class) resultSet.updateTime(columnIndex, (Time) value); else
                if(clazz == Timestamp.class) resultSet.updateTimestamp(columnIndex, (Timestamp) value); else
                //if(clazz == Array.class) resultSet.updateArray(columnIndex, (Array) value); else
                        resultSet.updateObject(columnIndex, value);
            } else {
                throw new SQLException("Can not convert \"" + value.toString() + "\" into " + dataTypeDefinition.getName());
            }
        }
    }

    public Object getValueFromStatement(CallableStatement callableStatement, int parameterIndex) throws SQLException {
        GenericDataType genericDataType = dataTypeDefinition.getGenericDataType();
        if (ValueAdapter.supports(genericDataType)) {
            return ValueAdapter.create(genericDataType, callableStatement, parameterIndex);
        }
/*
        if (genericDataType == GenericDataType.BLOB) return new BlobValue(callableStatement, parameterIndex);
        if (genericDataType == GenericDataType.CLOB) return new ClobValue(callableStatement, parameterIndex);
        if (genericDataType == GenericDataType.XMLTYPE) return new XmlTypeValue((OracleCallableStatement) callableStatement, parameterIndex);
*/

        return callableStatement.getObject(parameterIndex);
    }

    public void setValueToStatement(PreparedStatement preparedStatement, int parameterIndex, Object value) throws SQLException {
        GenericDataType genericDataType = dataTypeDefinition.getGenericDataType();
        if (ValueAdapter.supports(genericDataType)) {
            ValueAdapter valueAdapter = ValueAdapter.create(genericDataType);
            valueAdapter.write(preparedStatement.getConnection(), preparedStatement, parameterIndex, value);
            return;
        }
        if (genericDataType == GenericDataType.CURSOR) return;// not supported


        if (value == null) {
            preparedStatement.setObject(parameterIndex, null);
        } else {
            Class clazz = dataTypeDefinition.getTypeClass();
            if (value.getClass().isAssignableFrom(clazz)) {
                if(clazz == String.class) preparedStatement.setString(parameterIndex, (String) value); else
                if(clazz == Byte.class) preparedStatement.setByte(parameterIndex, (Byte) value); else
                if(clazz == Short.class) preparedStatement.setShort(parameterIndex, (Short) value); else
                if(clazz == Integer.class) preparedStatement.setInt(parameterIndex, (Integer) value); else
                if(clazz == Long.class) preparedStatement.setLong(parameterIndex, (Long) value); else
                if(clazz == Float.class) preparedStatement.setFloat(parameterIndex, (Float) value); else
                if(clazz == Double.class) preparedStatement.setDouble(parameterIndex, (Double) value); else
                if(clazz == BigDecimal.class) preparedStatement.setBigDecimal(parameterIndex, (BigDecimal) value); else
                if(clazz == Date.class) preparedStatement.setDate(parameterIndex, (Date) value); else
                if(clazz == Time.class) preparedStatement.setTime(parameterIndex, (Time) value); else
                if(clazz == Timestamp.class) preparedStatement.setTimestamp(parameterIndex, (Timestamp) value); else
                if(clazz == Boolean.class) preparedStatement.setBoolean(parameterIndex, (Boolean) value); else
                        preparedStatement.setObject(parameterIndex, value);
            } else {
                throw new SQLException("Can not convert \"" + value.toString() + "\" into " + dataTypeDefinition.getName());
            }
        }
    }

    public int getSqlType(){
        return dataTypeDefinition.getSqlType();
    }


    public String toString() {
        return dataTypeDefinition.getName();
    }

    /*********************************************************
     *                 DynamicContentElement                 *
     *********************************************************/

    public String getDescription() {
        return null;
    }

    public void reload() {
    }

    public void dispose() {

    }

    public int compareTo(@NotNull Object o) {
        DBNativeDataType remote = (DBNativeDataType) o;
        return getName().compareTo(remote.getName());
    }
}
