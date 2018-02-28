package com.dci.intellij.dbn.data.export;

import java.io.File;
import org.jdom.Element;

import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.common.state.PersistentStateElement;

public class DataExportInstructions extends SettingsUtil implements PersistentStateElement<Element>, Cloneable {
    private boolean createHeader = true;
    private boolean quoteValuesContainingSeparator = true;
    private boolean quoteAllValues = false;
    private String valueSeparator;
    private String fileName;
    private String fileLocation;
    private Scope scope = Scope.GLOBAL;
    private Destination destination = Destination.FILE;
    private DataExportFormat format = DataExportFormat.EXCEL;
    private String baseName;

    public boolean createHeader() {
        return createHeader;
    }

    public void setCreateHeader(boolean createHeader) {
        this.createHeader = createHeader;
    }

    public boolean quoteValuesContainingSeparator() {
        return quoteValuesContainingSeparator;
    }

    public void quoteValuesContainingSeparator(boolean quoteValuesContainingSeparator) {
        this.quoteValuesContainingSeparator = quoteValuesContainingSeparator;
    }

    public boolean quoteAllValues() {
        return quoteAllValues;
    }

    public void setQuoteAllValues(boolean quoteAllValues) {
        this.quoteAllValues = quoteAllValues;
    }

    public DataExportFormat getFormat() {
        return format;
    }

    public void setFormat(DataExportFormat format) {
        this.format = format;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public String getValueSeparator() {
        return valueSeparator;
    }

    public void setValueSeparator(String valueSeparator) {
        this.valueSeparator = valueSeparator;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileLocation() {
        return fileLocation;
    }

    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }

    public File getFile() {
        return new File(fileLocation, fileName);
    }

    public String getBaseName() {
        return baseName;
    }

    public void setBaseName(String baseName) {
        this.baseName = baseName;
    }

    public enum Scope{
        GLOBAL,
        SELECTION
    }

    public enum Destination{
        FILE,
        CLIPBOARD
    }

    @Override
    protected DataExportInstructions clone() throws CloneNotSupportedException {
        return (DataExportInstructions) super.clone();
    }

    /***********************************************
     *            PersistentStateElement           *
     ***********************************************/
    @Override
    public void writeState(Element element) {
        Element child = new Element("export-instructions");
        element.addContent(child);

        setBoolean(child, "create-header", createHeader);
        setBoolean(child, "quote-values-containing-separator", quoteValuesContainingSeparator);
        setBoolean(child, "quote-all-values", quoteAllValues);
        setString(child, "value-separator", valueSeparator);
        setString(child, "file-name", fileName);
        setString(child, "file-location", fileLocation);
        setString(child, "scope", scope.name());
        setString(child, "destination", destination.name());
        setString(child, "format", format.name());
    }

    @Override
    public void readState(Element element) {
        Element child = element.getChild("export-instructions");
        if (child != null) {
            createHeader = getBoolean(child, "create-header", createHeader);
            quoteValuesContainingSeparator = getBoolean(child, "quote-values-containing-separator", quoteValuesContainingSeparator);
            quoteAllValues = getBoolean(child, "quote-all-values", quoteAllValues);
            valueSeparator = getString(child, "value-separator", valueSeparator);
            fileName = getString(child, "file-name", fileName);
            fileLocation = getString(child, "file-location", fileLocation);
            scope = Scope.valueOf(getString(child, "scope", scope.name()));
            destination = Destination.valueOf(getString(child, "destination", destination.name()));
            format = DataExportFormat.valueOf(getString(child, "format", format.name()));
        }
    }
}
