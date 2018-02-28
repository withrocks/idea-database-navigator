package com.dci.intellij.dbn.data.type;

import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.object.DBPackage;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.DBType;
import com.dci.intellij.dbn.object.common.DBObjectBundle;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.List;

public class DBDataType {
    private DBNativeDataType nativeDataType;
    private DBType declaredType;
    private String name;
    private String qualifiedName;
    private long length;
    private int precision;
    private int scale;
    private boolean set;

    public static DBDataType get(ConnectionHandler connectionHandler, ResultSet resultSet) throws SQLException {
        return new Ref(resultSet, "").get(connectionHandler);
    }

    public static DBDataType get(ConnectionHandler connectionHandler, String dataTypeName, long length, int precision, int scale, boolean set) {
        String dataTypeOwner = null;
        String dataTypePackage = null;
        if (dataTypeName.contains(".")) {
            String[] nameChain = dataTypeName.split("\\.");
            if (nameChain.length == 1) {
                dataTypeName = nameChain[0];
            } else if (nameChain.length == 2) {
                dataTypeOwner = nameChain[0];
                dataTypeName = nameChain[1];
            } else if (nameChain.length == 3) {
                dataTypeOwner = nameChain[0];
                dataTypePackage = nameChain[1];
                dataTypeName = nameChain[2];
            }
        }
        return new Ref(dataTypeName, dataTypeOwner, dataTypePackage, length, precision, scale, set).get(connectionHandler);
    }

    protected DBDataType() {
    }

/*    public DBDataType(DBObject parent, ResultSet resultSet) throws SQLException {
        length = resultSet.getLong("DATA_LENGTH");
        precision = resultSet.getInt("DATA_PRECISION");
        scale = resultSet.getInt("DATA_SCALE");
        set = "Y".equals(resultSet.getString("IS_SET"));

        String typeOwner = resultSet.getString("DATA_TYPE_OWNER");
        String typePackage = resultSet.getString("DATA_TYPE_PACKAGE");
        String dataTypeName = resultSet.getString("DATA_TYPE_NAME");
        DBObjectBundle objectBundle = parent.getConnectionHandler().getObjectBundle();
        if (typeOwner != null) {
            DBSchema typeSchema = objectBundle.getSchema(typeOwner);
            if (typePackage != null) {
                DBPackage packagee = typeSchema.getPackage(typePackage);
                if (packagee != null) {
                    declaredType = packagee.getType(dataTypeName);
                }
            } else {
                declaredType = typeSchema.getType(dataTypeName);
            }
            if (declaredType == null) typeName = dataTypeName;
        } else {
            nativeDataType = objectBundle.getNativeDataType(dataTypeName);
            if (nativeDataType == null) typeName = dataTypeName;
        }
    }*/

    public boolean isSet() {
        return set;
    }

    public boolean isDeclared() {
        return declaredType != null;
    }

    public boolean isNative() {
        return nativeDataType != null;
    }

    public boolean isPurelyDeclared() {
        return isDeclared() && !isNative();
    }

    public boolean isPurelyNative() {
        return isNative() && !isDeclared();
    }

    public boolean isNativeDeclared() {
        return nativeDataType != null && declaredType != null;
    }

    public String getName() {
        return (set ? "set of " : "") +
                (nativeDataType == null && declaredType == null ? name :
                 nativeDataType == null ? declaredType.getQualifiedName() :
                 nativeDataType.getName());
    }

    public Class getTypeClass() {
        return nativeDataType == null ? Object.class : nativeDataType.getDataTypeDefinition().getTypeClass();
    }

    public int getSqlType() {
        return nativeDataType == null ? Types.CHAR : nativeDataType.getSqlType();
    }

    public DBNativeDataType getNativeDataType() {
        return nativeDataType;
    }

    public DBType getDeclaredType() {
        return declaredType;
    }

    public long getLength() {
        return length;
    }

    public int getPrecision() {
        return precision;
    }

    public int getScale() {
        return scale;
    }


    public Object getValueFromResultSet(ResultSet resultSet, int columnIndex) throws SQLException {
        if (nativeDataType != null) {
            return nativeDataType.getValueFromResultSet(resultSet, columnIndex);
        } else {
            return declaredType == null ? "[" + name + "]" : '[' + declaredType.getName() + ']';
        }
    }

    public void setValueToResultSet(ResultSet resultSet, int columnIndex, Object value) throws SQLException {
        if (nativeDataType != null) {
            nativeDataType.setValueToResultSet(resultSet, columnIndex, value);
        }
    }

    public void setValueToPreparedStatement(PreparedStatement preparedStatement, int index, Object value) throws SQLException {
        if (nativeDataType != null) {
            nativeDataType.setValueToStatement(preparedStatement, index, value);
        }
    }

    public String getQualifiedName() {
        if (qualifiedName == null) {
            StringBuilder buffer = new StringBuilder();
            String name = getName();
            buffer.append(name == null ? "" : name.toLowerCase());
            if (precision > 0) {
                buffer.append(" (");
                buffer.append(precision);
                if (scale > 0) {
                    buffer.append(", ");
                    buffer.append(scale);
                }
                buffer.append(')');
            } else if (length > 0) {
                buffer.append(" (");
                buffer.append(length);
                buffer.append(')');
            }
            qualifiedName = buffer.toString();
        }
        return qualifiedName;
    }

    /**
     * @deprecated
     */
    public Object convert(String stringValue) throws Exception{
        Class clazz = getTypeClass();
        if (String.class.isAssignableFrom(clazz)) {
            return stringValue;
        }
        if (Date.class.isAssignableFrom(clazz)) {
            Method method = clazz.getMethod("valueOf", String.class);
            return method.invoke(clazz, stringValue);
        } else {
            Constructor constructor = clazz.getConstructor(String.class);
            return constructor.newInstance(stringValue);
        }
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    public GenericDataType getGenericDataType() {
        return nativeDataType != null ? nativeDataType.getGenericDataType() : GenericDataType.OBJECT;
    }

    public String getContentTypeName() {
        return nativeDataType == null ? null : nativeDataType.getDataTypeDefinition().getContentTypeName();
    }

    public static class Ref {
        String dataTypeName;
        String dataTypeOwner;
        String dataTypePackage;
        long length;
        int precision;
        int scale;
        boolean set;

        public Ref(ResultSet resultSet, String lookupPrefix) throws SQLException {
            dataTypeName = resultSet.getString(lookupPrefix + "DATA_TYPE_NAME");
            length = resultSet.getLong(lookupPrefix + "DATA_LENGTH");
            precision = resultSet.getInt(lookupPrefix + "DATA_PRECISION");
            scale = resultSet.getInt(lookupPrefix + "DATA_SCALE");
            set = "Y".equals(resultSet.getString(lookupPrefix + "IS_SET"));

            dataTypeOwner = resultSet.getString(lookupPrefix + "DATA_TYPE_OWNER");
            dataTypePackage = resultSet.getString(lookupPrefix + "DATA_TYPE_PACKAGE");
        }

        public Ref(String dataTypeName, String dataTypeOwner, String dataTypePackage, long length, int precision, int scale, boolean set) {
            this.dataTypeName = dataTypeName;
            this.dataTypeOwner = dataTypeOwner;
            this.dataTypePackage = dataTypePackage;
            this.length = length;
            this.precision = precision;
            this.scale = scale;
            this.set = set;
        }

        public DBDataType get(ConnectionHandler connectionHandler) {
            String name = null;
            DBType declaredType = null;
            DBNativeDataType nativeDataType = null;

            DBObjectBundle objectBundle = connectionHandler.getObjectBundle();
            if (dataTypeOwner != null) {
                DBSchema typeSchema = objectBundle.getSchema(dataTypeOwner);
                if (typeSchema != null) {
                    if (dataTypePackage != null) {
                        DBPackage packagee = typeSchema.getPackage(dataTypePackage);
                        if (packagee != null) {
                            declaredType = packagee.getType(dataTypeName);
                        }
                    } else {
                        declaredType = typeSchema.getType(dataTypeName);
                    }
                }
                if (declaredType == null)  {
                    name = dataTypeName;
                } else {
                    DBNativeDataType nDataType = objectBundle.getNativeDataType(dataTypeName);
                    if (nDataType != null && nDataType.getDataTypeDefinition().isPseudoNative()) {
                        nativeDataType = nDataType;
                    }
                }

            } else {
                nativeDataType = objectBundle.getNativeDataType(dataTypeName);
                if (nativeDataType == null) name = dataTypeName;
            }

            List<DBDataType> cachedDataTypes = objectBundle.getCachedDataTypes();
            for (DBDataType dataType : cachedDataTypes) {
                if (CommonUtil.safeEqual(dataType.declaredType, declaredType) &&
                        CommonUtil.safeEqual(dataType.nativeDataType, nativeDataType) &&
                        CommonUtil.safeEqual(dataType.name, name) &&
                        dataType.length == length &&
                        dataType.precision == precision &&
                        dataType.scale == scale &&
                        dataType.set == set) {
                    return dataType;
                }
            }

            DBDataType dataType = new DBDataType();
            dataType.nativeDataType = nativeDataType;
            dataType.declaredType = declaredType;
            dataType.name = name;
            dataType.length = length;
            dataType.precision = precision;
            dataType.scale = scale;
            dataType.set = set;
            cachedDataTypes.add(dataType);
            return dataType;
        }
    }
}