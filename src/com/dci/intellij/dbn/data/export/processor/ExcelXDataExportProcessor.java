package com.dci.intellij.dbn.data.export.processor;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import com.dci.intellij.dbn.data.export.DataExportFormat;

public class ExcelXDataExportProcessor extends ExcelDataExportProcessor{

    protected DataExportFormat getFormat() {
        return DataExportFormat.EXCELX;
    }

    @Override
    public String getFileExtension() {
        return "xlsx";
    }

    @Override
    public String adjustFileName(String fileName) {
        if (!fileName.contains(".xlsx")) {
            fileName = fileName + ".xlsx";
        }
        return fileName;
    }

    @Override
    protected Workbook createWorkbook() {
        return new SXSSFWorkbook();
    }
}