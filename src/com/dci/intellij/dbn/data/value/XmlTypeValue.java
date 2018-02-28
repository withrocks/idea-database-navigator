package com.dci.intellij.dbn.data.value;

import com.dci.intellij.dbn.data.type.GenericDataType;
import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OracleResultSet;
import oracle.sql.OPAQUE;
import oracle.xdb.XMLType;
import org.jetbrains.annotations.Nullable;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class XmlTypeValue extends LargeObjectValue{
    private XMLType xmlType;

    public XmlTypeValue() {
    }

    public XmlTypeValue(CallableStatement callableStatement, int parameterIndex) throws SQLException {
        OracleCallableStatement oracleCallableStatement = (OracleCallableStatement) callableStatement;
        OPAQUE opaque = oracleCallableStatement.getOPAQUE(parameterIndex);
        xmlType = opaque == null ? null : XMLType.createXML(opaque);
    }

    public XmlTypeValue(ResultSet resultSet, int columnIndex) throws SQLException {
        OracleResultSet oracleResultSet = (OracleResultSet) resultSet;
        OPAQUE opaque = oracleResultSet.getOPAQUE(columnIndex);
        xmlType = opaque == null ? null : XMLType.createXML(opaque);
    }

    @Override
    public GenericDataType getGenericDataType() {
        return GenericDataType.XMLTYPE;
    }

    @Nullable
    public String read() throws SQLException {
        return read(0);
    }

    @Override
    @Nullable
    public String read(int maxSize) throws SQLException {
        return xmlType == null ? null : xmlType.getStringVal();
    }

    public void write(Connection connection, PreparedStatement preparedStatement, int parameterIndex, @Nullable String value) throws SQLException {
        xmlType = XMLType.createXML(connection, value);
        preparedStatement.setObject(parameterIndex, xmlType);
    }

    @Override
    public void write(Connection connection, ResultSet resultSet, int columnIndex, @Nullable String value) throws SQLException {
        OracleResultSet oracleResultSet = (OracleResultSet) resultSet;
        xmlType = value == null ? null : XMLType.createXML(connection, value);
        oracleResultSet.updateOracleObject(columnIndex, xmlType);
    }

    @Override
    public void release() {

    }

    @Override
    public long size() throws SQLException {
        return 0;
    }

    @Override
    public String getDisplayValue() {
        return "[XMLTYPE]";
    }
}
