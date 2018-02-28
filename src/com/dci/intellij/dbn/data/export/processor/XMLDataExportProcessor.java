package com.dci.intellij.dbn.data.export.processor;

import java.awt.datatransfer.Transferable;
import java.util.Date;

import com.dci.intellij.dbn.common.locale.Formatter;
import com.dci.intellij.dbn.common.locale.options.RegionalSettings;
import com.dci.intellij.dbn.common.util.ClipboardUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.export.DataExportException;
import com.dci.intellij.dbn.data.export.DataExportFormat;
import com.dci.intellij.dbn.data.export.DataExportInstructions;
import com.dci.intellij.dbn.data.export.DataExportModel;
import com.dci.intellij.dbn.data.type.GenericDataType;


public class XMLDataExportProcessor extends DataExportProcessor{
    protected DataExportFormat getFormat() {
        return DataExportFormat.XML;
    }

    @Override
    public String getFileExtension() {
        return "xml";
    }

    @Override
    public String adjustFileName(String fileName) {
        if (!fileName.contains(".xml")) {
            fileName = fileName + ".xml";
        }
        return fileName;
    }

    public boolean canCreateHeader() {
        return false;
    }

    public boolean canExportToClipboard() {
        return true;
    }

    public boolean canQuoteValues() {
        return false;
    }

    @Override
    public Transferable createClipboardContent(String content) {
        return ClipboardUtil.createXmlContent(content);
    }

    public void performExport(DataExportModel model, DataExportInstructions instructions, ConnectionHandler connectionHandler) throws DataExportException, InterruptedException {
        StringBuilder buffer = new StringBuilder();
        buffer.append("<table name=\"");
        buffer.append(model.getTableName());
        buffer.append("\">\n");
        RegionalSettings regionalSettings = RegionalSettings.getInstance(connectionHandler.getProject());

        for (int rowIndex=0; rowIndex < model.getRowCount(); rowIndex++) {
            buffer.append("    <row index=\"");
            buffer.append(rowIndex);
            buffer.append("\">\n");
            for (int columnIndex=0; columnIndex < model.getColumnCount(); columnIndex++){
                checkCancelled();
                String columnName = model.getColumnName(columnIndex);
                GenericDataType genericDataType = model.getGenericDataType(columnIndex);
                String value = null;
                if (genericDataType == GenericDataType.LITERAL ||
                        genericDataType == GenericDataType.NUMERIC ||
                        genericDataType == GenericDataType.DATE_TIME) {

                    Object object = model.getValue(rowIndex, columnIndex);

                    if (object != null) {
                        Formatter formatter = regionalSettings.getFormatter();
                        if (object instanceof Number) {
                            Number number = (Number) object;
                            value = formatter.formatNumber(number);
                        } else if (object instanceof Date) {
                            Date date = (Date) object;
                            value = hasTimeComponent(date) ?
                                    formatter.formatDateTime(date) :
                                    formatter.formatDate(date);
                        } else {
                            value = object.toString();
                        }
                    }
                }

                if (value == null) value = "";

                boolean isCDATA = StringUtil.containsOneOf(value, "\n", "<", ">");
                boolean isWrap = value.length() > 100 || isCDATA;

                buffer.append("        <column name=\"");
                buffer.append(columnName);
                buffer.append("\">");
                if (isWrap) {
                    value = ("\n" + value);//.replace("\n", "\n            ");
                }
                
                if (isCDATA) {
                    buffer.append("\n            <![CDATA[");
                    buffer.append(value);
                    buffer.append("\n            ]]>");
                } else {
                    buffer.append(value);
                }
                buffer.append(isWrap ? "\n        </column>\n" : "</column>\n");
            }

            buffer.append("    </row>\n");
        }
        buffer.append("</table>\n");


        writeContent(instructions, buffer.toString());
    }
}
