package com.dci.intellij.dbn.data.export.processor;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.export.DataExportException;
import com.dci.intellij.dbn.data.export.DataExportFormat;
import com.dci.intellij.dbn.data.export.DataExportInstructions;
import com.dci.intellij.dbn.data.export.DataExportModel;

public class CustomDataExportProcessor extends DataExportProcessor{
    protected DataExportFormat getFormat() {
        return DataExportFormat.CUSTOM;
    }

    @Override
    public String getFileExtension() {
        return "csv";
    }

    public boolean canCreateHeader() {
        return true;
    }

    public boolean canExportToClipboard() {
        return true;
    }

    public boolean canQuoteValues() {
        return true;
    }

    public void performExport(DataExportModel model, DataExportInstructions instructions, ConnectionHandler connectionHandler) throws DataExportException, InterruptedException {
        StringBuilder buffer = new StringBuilder();
        if (instructions.createHeader()) {
            for (int columnIndex=0; columnIndex < model.getColumnCount(); columnIndex++){
                String columnName = model.getColumnName(columnIndex);
                String separator = instructions.getValueSeparator();
                boolean containsSeparator = columnName.contains(separator);
                boolean quote =
                        instructions.quoteAllValues() || (
                        instructions.quoteValuesContainingSeparator() && containsSeparator);

                if (containsSeparator && !quote) {
                    throw new DataExportException(
                        "Can not create columns header with the given separator.\n" +
                        "Column " + columnName + " already contains the separator '" + separator + "'. \n" +
                        "Please consider quoting.");
                }

                if (columnIndex > 0) {
                    buffer.append(separator);
                }

                if (quote) {
                    if(columnName.indexOf('"') > -1) {
                        throw new DataExportException(
                            "Can not quote columns header.\n" +
                            "Column " + columnName + " contains quotes.");
                    }
                    buffer.append('"');
                    buffer.append(columnName);
                    buffer.append('"');
                } else {
                    buffer.append(columnName);
                }
            }
            buffer.append('\n');
        }

        for (int rowIndex=0; rowIndex < model.getRowCount(); rowIndex++) {
            for (int columnIndex=0; columnIndex < model.getColumnCount(); columnIndex++){
                checkCancelled();
                String columnName = model.getColumnName(columnIndex);
                Object object = model.getValue(rowIndex, columnIndex);
                String value = object == null ? "" : object.toString();
                String separator = instructions.getValueSeparator();

                boolean containsSeparator = value.contains(separator);
                boolean quote =
                        instructions.quoteAllValues() || (
                        instructions.quoteValuesContainingSeparator() && containsSeparator);

                if (containsSeparator && !quote) {
                    throw new DataExportException(
                        "Can not create row " + rowIndex + " with the given separator.\n" +
                        "Value for column " + columnName + " already contains the separator '" + separator + "'. \n" +
                        "Please consider quoting.");
                }

                if (columnIndex > 0) {
                    buffer.append(separator);
                }

                if (quote) {
                    if(value.indexOf('"') > -1) {
                        throw new DataExportException(
                            "Can not quote value of " + columnName + " at row " + rowIndex + ".\n" +
                            "Value contains quotes itself.");
                    }
                    buffer.append('"');
                    buffer.append(value);
                    buffer.append('"');
                } else {
                    buffer.append(value);
                }
            }
            buffer.append('\n');
        }
        writeContent(instructions, buffer.toString());
    }
}
