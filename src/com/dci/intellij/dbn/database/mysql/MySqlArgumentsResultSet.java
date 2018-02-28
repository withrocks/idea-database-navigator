package com.dci.intellij.dbn.database.mysql;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.common.util.WordTokenizer;

public class MySqlArgumentsResultSet implements ResultSet {
    private class Argument {
        String name;
        String programName;
        String methodName;
        String methodType;
        int overload;
        int position;
        int sequence;
        String inOut = "IN";
        String dataTypeOwner;
        String dataTypePackage;
        String dataTypeName;
        int dataLength;
        Integer dataPrecision;
        Integer dataScale;
    }
    private Iterator<Argument> arguments;
    private Argument currentArgument;

    public MySqlArgumentsResultSet(ResultSet resultSet) throws SQLException {
        List<Argument> argumentList = new ArrayList<Argument>();
        while (resultSet.next()) {
            String argumentsString = resultSet.getString("ARGUMENTS");
            WordTokenizer wordTokenizer = new WordTokenizer(argumentsString);

            String methodName = resultSet.getString("METHOD_NAME");
            String methodType = resultSet.getString("METHOD_TYPE");
            boolean betweenBrackets = false;
            boolean typePostfixSet = false;
            int argumentPosition = methodType.equals("FUNCTION") ? 0 : 1;

            Argument argument = null; 

            for (String token : wordTokenizer.getTokens()) {
                if (argument == null) {
                    typePostfixSet = false;
                    argument = new Argument();
                    argument.methodName = methodName;
                    argument.methodType = methodType;
                    argument.position = argumentPosition;

                    argumentList.add(argument);
                    argumentPosition++;
                }

                // hit IN OUT or INOUT token and name is not set
                if ((token.equalsIgnoreCase("IN") || token.equalsIgnoreCase("OUT") || token.equalsIgnoreCase("INOUT"))) {
                    if (argument.name != null) throwParseException(argumentsString, token, "Argument name should not be set.");
                    argument.inOut = token.toUpperCase();
                    continue;
                }

                // found open bracket => set betweenBrackets flag
                if (token.equals("(")) {
                    if (betweenBrackets) throwParseException(argumentsString, token, "Bracket already opened.");
                    if (argument.dataTypeName == null) throwParseException(argumentsString, token, "Data type not set yet.");
                    betweenBrackets = true;
                    continue;
                }

                // found close bracket => reset betweenBrackets flag
                if (token.equals(")")) {
                    if (!betweenBrackets) throwParseException(argumentsString, token, "No opened bracket.");
                    if (argument.dataPrecision == null && argument.dataScale == null) throwParseException(argumentsString, token, "Data precision and scale are not set yet.");
                    betweenBrackets = false;
                    continue;
                }

                // found comma token
                if (token.equals(",")) {
                    if (betweenBrackets) {
                        // between brackets
                        if (argument.dataPrecision == null) throwParseException(argumentsString, token, "Data precision is not set yet.");
                        continue;
                    } else {
                        // not between brackets => new argument
                        if (argument.name == null) throwParseException(argumentsString, token, "Argument name not set yet.");
                        if (argument.dataTypeName == null) throwParseException(argumentsString, token, "Data type not set yet.");
                        argument = null;
                        continue;
                    }
                }

                // number token
                if (StringUtil.isInteger(token)) {
                    if (!betweenBrackets) throwParseException(argumentsString, token, "No bracket opened.");
                    if (argument.name == null) throwParseException(argumentsString, token, "Argument name not set yet.");
                    if (argument.dataTypeName == null) throwParseException(argumentsString, token, "Data type not set yet.");

                    // if precision not set then set it
                    if (argument.dataPrecision == null) {
                        argument.dataPrecision = new Integer(token);
                        continue;
                    }
                    // if scale not set then set it
                    if (argument.dataScale == null) {
                        argument.dataScale = new Integer(token);
                        continue;
                    }
                    throwParseException(argumentsString, token);
                }

                // if none of the conditions above are met
                if (argument.name == null) {
                    argument.name = token;
                    continue;
                }

                if (argument.dataTypeName == null) {
                    argument.dataTypeName = token;
                    continue;
                }

                if (!typePostfixSet) {
                    typePostfixSet = true;
                    continue;
                }

                throwParseException(argumentsString, token);
            }
        }

        arguments = argumentList.iterator();
    }

    private static void throwParseException(String argumentsString, String token) throws SQLException {
        throw new SQLException("Could not parse argument list \"" + argumentsString + "\". Unexpected token \"" + token + "\" found.");
    }

    private static void throwParseException(String argumentsString, String token, String customMessage) throws SQLException {
        throw new SQLException("Could not parse argument list \"" + argumentsString + "\". Unexpected token \"" + token + "\" found. " + customMessage);
    }

    public boolean next() throws SQLException {
        currentArgument = arguments.hasNext() ? arguments.next() : null;
        return currentArgument != null;
    }

    public String getString(String columnLabel) throws SQLException {
        return
            columnLabel.equals("ARGUMENT_NAME") ? currentArgument.name :
            columnLabel.equals("METHOD_NAME") ? currentArgument.methodName :
            columnLabel.equals("METHOD_TYPE") ? currentArgument.methodType :        
            columnLabel.equals("IN_OUT") ? currentArgument.inOut :        
            columnLabel.equals("DATA_TYPE_NAME") ? currentArgument.dataTypeName : null;
    }

    public int getInt(String columnLabel) throws SQLException {
        return
            columnLabel.equals("POSITION") ? currentArgument.position :
            columnLabel.equals("SEQUENCE") ? currentArgument.position :
            columnLabel.equals("DATA_PRECISION") ? (currentArgument.dataPrecision == null ? 0 : currentArgument.dataPrecision) :
            columnLabel.equals("DATA_SCALE") ? (currentArgument.dataScale == null ? 0 : currentArgument.dataScale) : 0;
    }

    public long getLong(String columnLabel) throws SQLException {
        return getInt(columnLabel);
    }

    /*****************************************************************/
    public void close() throws SQLException {
    }

    public boolean wasNull() throws SQLException {
        return false;
    }

    public String getString(int columnIndex) throws SQLException {
        return null;
    }

    public boolean getBoolean(int columnIndex) throws SQLException {
        return false;
    }

    public byte getByte(int columnIndex) throws SQLException {
        return 0;
    }

    public short getShort(int columnIndex) throws SQLException {
        return 0;
    }

    public int getInt(int columnIndex) throws SQLException {
        return 0;
    }

    public long getLong(int columnIndex) throws SQLException {
        return 0;
    }

    public float getFloat(int columnIndex) throws SQLException {
        return 0;
    }

    public double getDouble(int columnIndex) throws SQLException {
        return 0;
    }

    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        return null;
    }

    public byte[] getBytes(int columnIndex) throws SQLException {
        return new byte[0];
    }

    public Date getDate(int columnIndex) throws SQLException {
        return null;
    }

    public Time getTime(int columnIndex) throws SQLException {
        return null;
    }

    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        return null;
    }

    public InputStream getAsciiStream(int columnIndex) throws SQLException {
        return null;
    }

    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        return null;
    }

    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        return null;
    }

    public boolean getBoolean(String columnLabel) throws SQLException {
        return false;
    }

    public byte getByte(String columnLabel) throws SQLException {
        return 0;
    }

    public short getShort(String columnLabel) throws SQLException {
        return 0;
    }

    public float getFloat(String columnLabel) throws SQLException {
        return 0;
    }

    public double getDouble(String columnLabel) throws SQLException {
        return 0;
    }

    public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
        return null;
    }

    public byte[] getBytes(String columnLabel) throws SQLException {
        return new byte[0];
    }

    public Date getDate(String columnLabel) throws SQLException {
        return null;
    }

    public Time getTime(String columnLabel) throws SQLException {
        return null;
    }

    public Timestamp getTimestamp(String columnLabel) throws SQLException {
        return null;
    }

    public InputStream getAsciiStream(String columnLabel) throws SQLException {
        return null;
    }

    public InputStream getUnicodeStream(String columnLabel) throws SQLException {
        return null;
    }

    public InputStream getBinaryStream(String columnLabel) throws SQLException {
        return null;
    }

    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    public void clearWarnings() throws SQLException {

    }

    public String getCursorName() throws SQLException {
        return null;
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        return null;
    }

    public Object getObject(int columnIndex) throws SQLException {
        return null;
    }

    public Object getObject(String columnLabel) throws SQLException {
        return null;
    }

    public int findColumn(String columnLabel) throws SQLException {
        return 0;
    }

    public Reader getCharacterStream(int columnIndex) throws SQLException {
        return null;
    }

    public Reader getCharacterStream(String columnLabel) throws SQLException {
        return null;
    }

    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        return null;
    }

    public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
        return null;
    }

    public boolean isBeforeFirst() throws SQLException {
        return false;
    }

    public boolean isAfterLast() throws SQLException {
        return false;
    }

    public boolean isFirst() throws SQLException {
        return false;
    }

    public boolean isLast() throws SQLException {
        return false;
    }

    public void beforeFirst() throws SQLException {

    }

    public void afterLast() throws SQLException {

    }

    public boolean first() throws SQLException {
        return false;
    }

    public boolean last() throws SQLException {
        return false;
    }

    public int getRow() throws SQLException {
        return 0;
    }

    public boolean absolute(int row) throws SQLException {
        return false;
    }

    public boolean relative(int rows) throws SQLException {
        return false;
    }

    public boolean previous() throws SQLException {
        return false;
    }

    public void setFetchDirection(int direction) throws SQLException {

    }

    public int getFetchDirection() throws SQLException {
        return 0;
    }

    public void setFetchSize(int rows) throws SQLException {

    }

    public int getFetchSize() throws SQLException {
        return 0;
    }

    public int getType() throws SQLException {
        return 0;
    }

    public int getConcurrency() throws SQLException {
        return 0;
    }

    public boolean rowUpdated() throws SQLException {
        return false;
    }

    public boolean rowInserted() throws SQLException {
        return false;
    }

    public boolean rowDeleted() throws SQLException {
        return false;
    }

    public void updateNull(int columnIndex) throws SQLException {

    }

    public void updateBoolean(int columnIndex, boolean x) throws SQLException {

    }

    public void updateByte(int columnIndex, byte x) throws SQLException {

    }

    public void updateShort(int columnIndex, short x) throws SQLException {

    }

    public void updateInt(int columnIndex, int x) throws SQLException {

    }

    public void updateLong(int columnIndex, long x) throws SQLException {

    }

    public void updateFloat(int columnIndex, float x) throws SQLException {

    }

    public void updateDouble(int columnIndex, double x) throws SQLException {

    }

    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {

    }

    public void updateString(int columnIndex, String x) throws SQLException {

    }

    public void updateBytes(int columnIndex, byte[] x) throws SQLException {

    }

    public void updateDate(int columnIndex, Date x) throws SQLException {

    }

    public void updateTime(int columnIndex, Time x) throws SQLException {

    }

    public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {

    }

    public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {

    }

    public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {

    }

    public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {

    }

    public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {

    }

    public void updateObject(int columnIndex, Object x) throws SQLException {

    }

    public void updateNull(String columnLabel) throws SQLException {

    }

    public void updateBoolean(String columnLabel, boolean x) throws SQLException {

    }

    public void updateByte(String columnLabel, byte x) throws SQLException {

    }

    public void updateShort(String columnLabel, short x) throws SQLException {

    }

    public void updateInt(String columnLabel, int x) throws SQLException {

    }

    public void updateLong(String columnLabel, long x) throws SQLException {

    }

    public void updateFloat(String columnLabel, float x) throws SQLException {

    }

    public void updateDouble(String columnLabel, double x) throws SQLException {

    }

    public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {

    }

    public void updateString(String columnLabel, String x) throws SQLException {

    }

    public void updateBytes(String columnLabel, byte[] x) throws SQLException {

    }

    public void updateDate(String columnLabel, Date x) throws SQLException {

    }

    public void updateTime(String columnLabel, Time x) throws SQLException {

    }

    public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {

    }

    public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {

    }

    public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {

    }

    public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {

    }

    public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {

    }

    public void updateObject(String columnLabel, Object x) throws SQLException {

    }

    public void insertRow() throws SQLException {

    }

    public void updateRow() throws SQLException {

    }

    public void deleteRow() throws SQLException {

    }

    public void refreshRow() throws SQLException {

    }

    public void cancelRowUpdates() throws SQLException {

    }

    public void moveToInsertRow() throws SQLException {

    }

    public void moveToCurrentRow() throws SQLException {

    }

    public Statement getStatement() throws SQLException {
        return null;
    }

    public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
        return null;
    }

    public Ref getRef(int columnIndex) throws SQLException {
        return null;
    }

    public Blob getBlob(int columnIndex) throws SQLException {
        return null;
    }

    public Clob getClob(int columnIndex) throws SQLException {
        return null;
    }

    public Array getArray(int columnIndex) throws SQLException {
        return null;
    }

    public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
        return null;
    }

    public Ref getRef(String columnLabel) throws SQLException {
        return null;
    }

    public Blob getBlob(String columnLabel) throws SQLException {
        return null;
    }

    public Clob getClob(String columnLabel) throws SQLException {
        return null;
    }

    public Array getArray(String columnLabel) throws SQLException {
        return null;
    }

    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        return null;
    }

    public Date getDate(String columnLabel, Calendar cal) throws SQLException {
        return null;
    }

    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
        return null;
    }

    public Time getTime(String columnLabel, Calendar cal) throws SQLException {
        return null;
    }

    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        return null;
    }

    public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
        return null;
    }

    public URL getURL(int columnIndex) throws SQLException {
        return null;
    }

    public URL getURL(String columnLabel) throws SQLException {
        return null;
    }

    public void updateRef(int columnIndex, Ref x) throws SQLException {

    }

    public void updateRef(String columnLabel, Ref x) throws SQLException {

    }

    public void updateBlob(int columnIndex, Blob x) throws SQLException {

    }

    public void updateBlob(String columnLabel, Blob x) throws SQLException {

    }

    public void updateClob(int columnIndex, Clob x) throws SQLException {

    }

    public void updateClob(String columnLabel, Clob x) throws SQLException {

    }

    public void updateArray(int columnIndex, Array x) throws SQLException {

    }

    public void updateArray(String columnLabel, Array x) throws SQLException {

    }

    public RowId getRowId(int columnIndex) throws SQLException {
        return null;
    }

    public RowId getRowId(String columnLabel) throws SQLException {
        return null;
    }

    public void updateRowId(int columnIndex, RowId x) throws SQLException {
    }

    public void updateRowId(String columnLabel, RowId x) throws SQLException {
    }

    public int getHoldability() throws SQLException {
        return 0;
    }

    public boolean isClosed() throws SQLException {
        return false;
    }

    public void updateNString(int columnIndex, String nString) throws SQLException {
    }

    public void updateNString(String columnLabel, String nString) throws SQLException {
    }

    public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
    }

    public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
    }

    public NClob getNClob(int columnIndex) throws SQLException {
        return null;
    }

    public NClob getNClob(String columnLabel) throws SQLException {
        return null;
    }

    public SQLXML getSQLXML(int columnIndex) throws SQLException {
        return null;
    }

    public SQLXML getSQLXML(String columnLabel) throws SQLException {
        return null;
    }

    public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
    }

    public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
    }

    public String getNString(int columnIndex) throws SQLException {
        return null;
    }

    public String getNString(String columnLabel) throws SQLException {
        return null;
    }

    public Reader getNCharacterStream(int columnIndex) throws SQLException {
        return null;
    }

    public Reader getNCharacterStream(String columnLabel) throws SQLException {
        return null;
    }

    public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
    }

    public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
    }

    public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
    }

    public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
    }

    public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
    }

    public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
    }

    public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
    }

    public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
    }

    public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
    }

    public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
    }

    public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
    }

    public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
    }

    public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
    }

    public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
    }

    public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
    }

    public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
    }

    public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
    }

    public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
    }

    public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
    }

    public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
    }

    public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
    }

    public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
    }

    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
    }

    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
    }

    public void updateClob(int columnIndex, Reader reader) throws SQLException {
    }

    public void updateClob(String columnLabel, Reader reader) throws SQLException {
    }

    public void updateNClob(int columnIndex, Reader reader) throws SQLException {
    }

    public void updateNClob(String columnLabel, Reader reader) throws SQLException {
    }

    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {return null;}

    public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {return null;}

    public <T> T unwrap(Class<T> iface) throws SQLException {return null;}

    public boolean isWrapperFor(Class<?> iface) throws SQLException {return false;}



}
