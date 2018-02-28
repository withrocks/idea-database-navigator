package com.dci.intellij.dbn.database.postgres;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;

import com.dci.intellij.dbn.data.type.GenericDataType;
import com.dci.intellij.dbn.database.common.DatabaseNativeDataTypes;

public class PostgresNativeDataTypes extends DatabaseNativeDataTypes {
    {
        createNumericDefinition("OID", BigDecimal.class, Types.NUMERIC);
        createNumericDefinition("XID", BigDecimal.class, Types.NUMERIC);
        createNumericDefinition("CID", BigDecimal.class, Types.NUMERIC);
        createNumericDefinition("TID", BigDecimal.class, Types.NUMERIC);

        createNumericDefinition("SMALLINT", Integer.class, Types.SMALLINT);
        createNumericDefinition("INTEGER", Integer.class, Types.INTEGER);
        createNumericDefinition("INT", Integer.class, Types.INTEGER);
        createNumericDefinition("INT2", Integer.class, Types.INTEGER);
        createNumericDefinition("INT4", Integer.class, Types.INTEGER);
        createNumericDefinition("INT8", Integer.class, Types.INTEGER);
        createNumericDefinition("BIGINT", Long.class, Types.BIGINT);
        createNumericDefinition("SMALLSERIAL", Integer.class, Types.SMALLINT);
        createNumericDefinition("SERIAL", Long.class, Types.INTEGER);
        createNumericDefinition("BIGSERIAL", Long.class, Types.BIGINT);
        createNumericDefinition("INT8", Long.class, Types.BIGINT);
        createNumericDefinition("SERIAL8", Long.class, Types.BIGINT);
        createNumericDefinition("DECIMAL", BigDecimal.class, Types.DECIMAL);
        createNumericDefinition("NUMERIC", BigDecimal.class, Types.NUMERIC);
        createNumericDefinition("REAL", BigDecimal.class, Types.NUMERIC);
        createNumericDefinition("DOUBLE_PRECISION", BigDecimal.class, Types.DOUBLE);
        createNumericDefinition("MONEY", BigDecimal.class, Types.DOUBLE);

        createLiteralDefinition("CHARACTER VARYING", String.class, Types.VARCHAR);
        createLiteralDefinition("VARCHAR", String.class, Types.VARCHAR);
        createLiteralDefinition("CHARACTER", String.class, Types.CHAR);
        createLiteralDefinition("CHAR", String.class, Types.CHAR);
        createLiteralDefinition("TEXT", String.class, Types.VARCHAR);
        createLiteralDefinition("NAME", String.class, Types.VARCHAR);

        createLiteralDefinition("BYTEA", String.class, Types.BINARY);
        createLiteralDefinition("BIT", String.class, Types.VARCHAR);
        createLiteralDefinition("BIT VARYING", String.class, Types.VARCHAR);
        createLiteralDefinition("VARBIT", String.class, Types.VARCHAR);


        createDateTimeDefinition("TIMESTAMP", Timestamp.class, Types.TIMESTAMP);
        createDateTimeDefinition("TIMESTAMP WITH TIME ZONE", Timestamp.class, Types.TIMESTAMP);
        createDateTimeDefinition("TIMESTAMP WITHOUT TIME ZONE", Timestamp.class, Types.TIMESTAMP);
        createDateTimeDefinition("DATE", Date.class, Types.DATE);
        createDateTimeDefinition("TIME", Time.class, Types.TIME);
        createDateTimeDefinition("TIME WITH TIME ZONE", Time.class, Types.TIME);
        createDateTimeDefinition("TIME WITHOUT TIME ZONE", Time.class, Types.TIME);
        createLiteralDefinition("INTERVAL", String.class, Types.VARCHAR);

        createBasicDefinition("BOOLEAN", Boolean.class, Types.BOOLEAN, GenericDataType.BOOLEAN);
        createBasicDefinition("BOOL", Boolean.class, Types.BOOLEAN, GenericDataType.BOOLEAN);
        createBasicDefinition("POINT", Object.class, Types.OTHER, GenericDataType.OBJECT);
        createBasicDefinition("LINE", Object.class, Types.OTHER, GenericDataType.OBJECT);
        createBasicDefinition("LSEG", Object.class, Types.OTHER, GenericDataType.OBJECT);
        createBasicDefinition("BOX", Object.class, Types.OTHER, GenericDataType.OBJECT);
        createBasicDefinition("PATH", Object.class, Types.OTHER, GenericDataType.OBJECT);
        createBasicDefinition("POLYGON", Object.class, Types.OTHER, GenericDataType.OBJECT);
        createBasicDefinition("CIRCLE", Object.class, Types.OTHER, GenericDataType.OBJECT);
        createBasicDefinition("JSON", Object.class, Types.OTHER, GenericDataType.OBJECT);
        createBasicDefinition("ARRAY", Array.class, Types.ARRAY, GenericDataType.ARRAY);

        createBasicDefinition("INT2VECTOR", Array.class, Types.ARRAY, GenericDataType.ARRAY);

        createLiteralDefinition("CIDR", String.class, Types.VARCHAR);
        createLiteralDefinition("INET", String.class, Types.VARCHAR);
        createLiteralDefinition("MACADDR", String.class, Types.VARCHAR);

        createLiteralDefinition("TSVECTOR", String.class, Types.VARCHAR);
        createLiteralDefinition("TSQUERY", String.class, Types.VARCHAR);
        createLiteralDefinition("UUID", String.class, Types.VARCHAR);
        createLiteralDefinition("XML", String.class, Types.VARCHAR);
        createLiteralDefinition("UNKNOWN", String.class, Types.VARCHAR);

        createBasicDefinition("REFCURSOR", Object.class, Types.OTHER, GenericDataType.CURSOR);
    }
}