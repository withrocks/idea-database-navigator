package com.dci.intellij.dbn.connection.config;

import java.io.File;
import org.jdom.Element;

import com.dci.intellij.dbn.common.util.FileUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.config.ui.GenericDatabaseSettingsForm;
import com.intellij.openapi.vfs.VirtualFile;

public class GenericConnectionDatabaseSettings extends ConnectionDatabaseSettings {
    protected String driverLibrary;
    protected String driver;
    protected String databaseUrl;

    public GenericConnectionDatabaseSettings(ConnectionSettings connectionSettings) {
        super(connectionSettings);
    }

    public GenericDatabaseSettingsForm createConfigurationEditor() {
        return new GenericDatabaseSettingsForm(this);
    }

    public String getDriverLibrary() {
        return driverLibrary;
    }

    public void setDriverLibrary(String driverLibrary) {
        this.driverLibrary = driverLibrary;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getDatabaseUrl() {
        return databaseUrl;
    }

    public void setDatabaseUrl(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    public void updateHashCode() {
        hashCode = (name + driver + driverLibrary + databaseUrl + user + password + osAuthentication).hashCode();
    }

    public GenericConnectionDatabaseSettings clone() {
        Element connectionElement = new Element(getConfigElementName());
        writeConfiguration(connectionElement);
        GenericConnectionDatabaseSettings clone = new GenericConnectionDatabaseSettings(getParent());
        clone.readConfiguration(connectionElement);
        clone.setConnectivityStatus(getConnectivityStatus());
        return clone;
    }

    public String getConnectionDetails() {
        return "Name:\t"      + name + "\n" +
               "Description:\t" + description + "\n" +
               "URL:\t"       + databaseUrl + "\n" +
               "User:\t"      + user;
    }

   /*********************************************************
    *                PersistentConfiguration                *
    *********************************************************/
    public void readConfiguration(Element element) {
        super.readConfiguration(element);
        if (element.getName().equals(getConfigElementName())) {
            driverLibrary = convertToAbsolutePath(getString(element, "driver-library", driverLibrary));
            driver        = getString(element, "driver", driver);
            databaseUrl   = getString(element, "url", databaseUrl);
        } else {
            // TODO: decommission (support old configuration)
            driverLibrary = convertToAbsolutePath(element.getAttributeValue("driver-library"));
            driver = element.getAttributeValue("driver");
            databaseUrl = element.getAttributeValue("url");
        }
    }

    public void writeConfiguration(Element element) {
        super.writeConfiguration(element);
        setString(element, "driver-library", nvl(convertToRelativePath(driverLibrary)));
        setString(element, "driver", nvl(driver));
        setString(element, "url", nvl(databaseUrl));
    }

    private String convertToRelativePath(String path) {
        if (!StringUtil.isEmptyOrSpaces(path)) {
            VirtualFile baseDir = getProject().getBaseDir();
            if (baseDir != null) {
                File projectDir = new File(baseDir.getPath());
                String relativePath = com.intellij.openapi.util.io.FileUtil.getRelativePath(projectDir, new File(path));
                if (relativePath != null) {
                    if (relativePath.lastIndexOf(".." + File.separatorChar) < 1) {
                        return relativePath;
                    }
                }
            }
        }
        return path;
    }

    private String convertToAbsolutePath(String path) {
        if (!StringUtil.isEmptyOrSpaces(path)) {
            VirtualFile baseDir = getProject().getBaseDir();
            if (baseDir != null) {
                File projectDir = new File(baseDir.getPath());
                if (new File(path).isAbsolute()) {
                    return path;
                } else {
                    File file = FileUtil.createFileByRelativePath(projectDir, path);
                    return file == null ? null : file.getPath();
                }
            }
        }
        return path;
    }
}
