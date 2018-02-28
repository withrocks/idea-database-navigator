package com.dci.intellij.dbn.data.export.processor;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.export.DataExportException;
import com.dci.intellij.dbn.data.export.DataExportFormat;
import com.dci.intellij.dbn.data.export.DataExportInstructions;
import com.dci.intellij.dbn.data.export.DataExportModel;

public class CSVDataExportProcessor extends CustomDataExportProcessor{
    protected DataExportFormat getFormat() {
        return DataExportFormat.CSV;
    }

    @Override
    public String getFileExtension() {
        return "csv";
    }

    public void performExport(DataExportModel model, DataExportInstructions instructions, ConnectionHandler connectionHandler) throws DataExportException, InterruptedException {
        instructions.setValueSeparator(",");
        super.performExport(model, instructions, connectionHandler);
    }
}
