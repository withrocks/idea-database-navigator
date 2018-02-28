package com.dci.intellij.dbn.database.oracle;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Timestamp;
import java.sql.Types;

import com.dci.intellij.dbn.data.type.GenericDataType;
import com.dci.intellij.dbn.database.common.DatabaseNativeDataTypes;
import oracle.jdbc.OracleTypes;

public class OracleNativeDataTypes extends DatabaseNativeDataTypes {
    {
        createLiteralDefinition("CHAR", String.class, OracleTypes.CHAR);
        createLiteralDefinition("CHAR VARYING", String.class, OracleTypes.CHAR);
        createLiteralDefinition("VARCHAR2", String.class, OracleTypes.VARCHAR);
        createLiteralDefinition("VARCHAR", String.class, OracleTypes.VARCHAR);
        createLiteralDefinition("CHARACTER", String.class, OracleTypes.CHAR);
        createLiteralDefinition("CHARACTER VARYING", String.class, OracleTypes.CHAR);
        createLiteralDefinition("NATIONAL CHAR", String.class, OracleTypes.CHAR);
        createLiteralDefinition("NATIONAL CHAR VARYING", String.class, OracleTypes.CHAR);
        createLiteralDefinition("NATIONAL CHARACTER", String.class, OracleTypes.CHAR);
        createLiteralDefinition("NATIONAL CHARACTER VARYING", String.class, OracleTypes.CHAR);
        createLiteralDefinition("NCHAR VARYING", String.class, OracleTypes.CHAR);
        createLiteralDefinition("NCHAR", String.class, OracleTypes.CHAR);
        createLiteralDefinition("NVARCHAR2", String.class, OracleTypes.CHAR);
        createLiteralDefinition("STRING", String.class, OracleTypes.VARCHAR);
        createLiteralDefinition("RAW", String.class, OracleTypes.RAW);
        
        createLiteralDefinition("LONG RAW", String.class, OracleTypes.LONGVARCHAR);
        createLiteralDefinition("LONG", String.class, OracleTypes.LONGVARCHAR);

        createNumericDefinition("NUMBER", BigDecimal.class, OracleTypes.NUMBER);
        createNumericDefinition("BINARY_INTEGER", Integer.class, OracleTypes.INTEGER);
        createNumericDefinition("BINARY_FLOAT", Float.class, OracleTypes.BINARY_FLOAT);
        createNumericDefinition("BINARY_DOUBLE", Double.class, OracleTypes.BINARY_DOUBLE);
        createNumericDefinition("NUMERIC", Short.class, OracleTypes.NUMERIC);
        createNumericDefinition("DECIMAL", Short.class, OracleTypes.DECIMAL);
        createNumericDefinition("DEC", Short.class, OracleTypes.DECIMAL);
        createNumericDefinition("INTEGER", Integer.class, OracleTypes.INTEGER);
        createNumericDefinition("INT", Integer.class, OracleTypes.INTEGER);
        createNumericDefinition("SMALLINT", Short.class, OracleTypes.SMALLINT);
        createNumericDefinition("FLOAT", Float.class, OracleTypes.FLOAT);
        createNumericDefinition("DOUBLE PRECISION", Double.class, OracleTypes.DOUBLE);
        createNumericDefinition("REAL", Float.class, OracleTypes.FLOAT);

        createDateTimeDefinition("DATE", Timestamp.class, OracleTypes.DATE);
        createDateTimeDefinition("TIME", Timestamp.class, OracleTypes.TIME);
        createDateTimeDefinition("TIME WITH TIME ZONE", Timestamp.class, OracleTypes.TIMESTAMPTZ);
        createDateTimeDefinition("TIMESTAMP WITH LOCAL TIME ZONE", Timestamp.class, OracleTypes.TIMESTAMPLTZ);
        createDateTimeDefinition("TIMESTAMP", Timestamp.class, OracleTypes.TIMESTAMP);
        createBasicDefinition("INTERVAL DAY TO SECOND", Object.class, OracleTypes.INTERVALDS, GenericDataType.DATE_TIME);
        createBasicDefinition("INTERVAL YEAR TO MONTH", Object.class, OracleTypes.INTERVALYM, GenericDataType.DATE_TIME);

        createLargeValueDefinition("BLOB", Blob.class, OracleTypes.BLOB, GenericDataType.BLOB);
        createLargeValueDefinition("CLOB", Clob.class, OracleTypes.CLOB, GenericDataType.CLOB);
        createLargeValueDefinition("NCLOB", Clob.class, OracleTypes.CLOB, GenericDataType.CLOB);
        createLargeValueDefinition("XMLTYPE", Clob.class, Types.SQLXML, GenericDataType.XMLTYPE, true, "XML");

        createBasicDefinition("BFILE", Object.class, OracleTypes.BFILE, GenericDataType.FILE);
        createBasicDefinition("ROWID", Object.class, OracleTypes.ROWID, GenericDataType.ROWID);
        createBasicDefinition("UROWID", Object.class, OracleTypes.ROWID, GenericDataType.ROWID);
        createBasicDefinition("REF CURSOR", Object.class, OracleTypes.CURSOR, GenericDataType.CURSOR);
 
        createBasicDefinition("PL/SQL BOOLEAN", String.class, OracleTypes.VARCHAR, GenericDataType.BOOLEAN);
    }

}
