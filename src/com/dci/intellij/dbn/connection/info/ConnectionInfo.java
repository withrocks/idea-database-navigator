package com.dci.intellij.dbn.connection.info;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public class ConnectionInfo {
    private String productName;
    private String productVersion;
    private String driverName;
    private String driverVersion;
    private String driverJdbcType;
    private String url;
    private String userName;

    public ConnectionInfo(DatabaseMetaData metaData) throws SQLException {
        productName = metaData.getDatabaseProductName();
        productVersion = metaData.getDatabaseProductVersion();
        int index = productVersion.indexOf('\n');
        productVersion = index > -1 ? productVersion.substring(0, index) : productVersion;
        driverName = metaData.getDriverName();
        driverVersion = metaData.getDriverVersion();
        url = metaData.getURL();
        userName = metaData.getUserName();
        driverJdbcType = metaData.getJDBCMajorVersion() + (metaData.getJDBCMinorVersion() > 0 ? "." + metaData.getJDBCMinorVersion() : "");
    }

    public String toString() {
        return  "Product name:\t" + productName + '\n' +
                "Product version:\t" + productVersion + '\n' +
                "Driver name:\t\t" + driverName + '\n' +
                "Driver version:\t" + driverVersion + '\n'+
                "JDBC Type:\t\t" + driverJdbcType + '\n' +
                "URL:\t\t" + url + '\n' +
                "User name:\t\t" + userName;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductVersion() {
        return productVersion;
    }

    public String getDriverName() {
        return driverName;
    }

    public String getDriverVersion() {
        return driverVersion;
    }

    public String getUrl() {
        return url;
    }

    public String getUserName() {
        return userName;
    }

    public String getDriverJdbcType() {
        return driverJdbcType;
    }
}
