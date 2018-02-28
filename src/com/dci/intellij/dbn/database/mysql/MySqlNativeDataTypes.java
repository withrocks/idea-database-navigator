package com.dci.intellij.dbn.database.mysql;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Timestamp;
import java.sql.Types;

import com.dci.intellij.dbn.data.type.GenericDataType;
import com.dci.intellij.dbn.database.common.DatabaseNativeDataTypes;

public class MySqlNativeDataTypes extends DatabaseNativeDataTypes {
    {
        createBasicDefinition("CHAR", String.class, Types.CHAR, GenericDataType.LITERAL);
        createBasicDefinition("VARCHAR", String.class, Types.VARCHAR, GenericDataType.LITERAL);
        createBasicDefinition("BINARY", String.class, Types.BINARY, GenericDataType.LITERAL);
        createBasicDefinition("VARBINARY", String.class, Types.VARBINARY, GenericDataType.LITERAL);
        createBasicDefinition("NATIONAL CHAR", String.class, Types.CHAR, GenericDataType.LITERAL);
        createBasicDefinition("NATIONAL VARCHAR", String.class, Types.VARCHAR, GenericDataType.LITERAL);
        createBasicDefinition("ENUM", String.class, Types.CHAR, GenericDataType.LITERAL);
        createBasicDefinition("SET", String.class, Types.CHAR, GenericDataType.LITERAL);

        createNumericDefinition("BIT", Short.class, Types.BIT);
        createNumericDefinition("TINYINT", Short.class, Types.TINYINT);
        createNumericDefinition("BOOL", Boolean.class, Types.BOOLEAN);
        createNumericDefinition("BOOLEAN", Boolean.class, Types.BOOLEAN);
        createNumericDefinition("SMALLINT", Integer.class, Types.SMALLINT);
        createNumericDefinition("MEDIUMINT", Integer.class, Types.INTEGER);
        createNumericDefinition("INT", Long.class, Types.INTEGER);
        createNumericDefinition("INT UNSIGNED", Long.class, Types.INTEGER);
        createNumericDefinition("INTEGER", Long.class, Types.INTEGER);
        createNumericDefinition("BIGINT", Long.class, Types.BIGINT);
        createNumericDefinition("FLOAT", Float.class, Types.FLOAT);
        createNumericDefinition("DOUBLE", Double.class, Types.DOUBLE);
        createNumericDefinition("DOUBLE PRECISION", Double.class, Types.DOUBLE);
        createNumericDefinition("DECIMAL", BigDecimal.class, Types.DECIMAL);
        createNumericDefinition("DEC", BigDecimal.class, Types.DECIMAL);

        createDateTimeDefinition("DATE", Timestamp.class, Types.DATE);
        createDateTimeDefinition("DATETIME", Timestamp.class, Types.TIMESTAMP);
        createDateTimeDefinition("TIMESTAMP", Timestamp.class, Types.TIMESTAMP);
        createDateTimeDefinition("TIME", Timestamp.class, Types.TIME);
        createDateTimeDefinition("YEAR", Timestamp.class, Types.DATE);

        createLargeValueDefinition("TINYBLOB", Blob.class, Types.BLOB, GenericDataType.BLOB);
        createLargeValueDefinition("TINYTEXT", Blob.class, Types.CLOB, GenericDataType.CLOB);
        createLargeValueDefinition("BLOB", Blob.class, Types.BLOB, GenericDataType.BLOB);
        createLargeValueDefinition("TEXT", Blob.class, Types.CLOB, GenericDataType.CLOB);
        createLargeValueDefinition("MEDIUMBLOB", Blob.class, Types.BLOB, GenericDataType.BLOB);
        createLargeValueDefinition("MEDIUMTEXT", Blob.class, Types.CLOB, GenericDataType.CLOB);
        createLargeValueDefinition("LONGBLOB", Blob.class, Types.BLOB, GenericDataType.BLOB);
        createLargeValueDefinition("LONGTEXT", Blob.class, Types.CLOB, GenericDataType.CLOB);
    }
}