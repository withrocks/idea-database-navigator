package com.dci.intellij.dbn.ddl;

import java.util.List;

import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.vfs.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.vfs.VirtualFile;

public class ObjectToDDLContentSynchronizer implements Runnable {
    DBEditableObjectVirtualFile databaseFile;
    private DBContentType sourceContentType;

    public ObjectToDDLContentSynchronizer(DBContentType sourceContentType, DBEditableObjectVirtualFile databaseFile) {
        this.sourceContentType = sourceContentType;
        this.databaseFile = databaseFile;
    }

    public void run() {
        assert !sourceContentType.isBundle();
        DDLFileManager ddlFileManager = DDLFileManager.getInstance(databaseFile.getProject());
        List<VirtualFile> ddlFiles = databaseFile.getAttachedDDLFiles();

        if (ddlFiles != null && !ddlFiles.isEmpty()) {
            for (VirtualFile ddlFile : ddlFiles) {
                DDLFileType ddlFileType = ddlFileManager.getDDLFileTypeForExtension(ddlFile.getExtension());
                DBContentType fileContentType = ddlFileType.getContentType();

                StringBuilder buffer = new StringBuilder();
                if (fileContentType.isBundle()) {
                    DBContentType[] contentTypes = fileContentType.getSubContentTypes();
                    for (DBContentType contentType : contentTypes) {
                        DBSourceCodeVirtualFile virtualFile = (DBSourceCodeVirtualFile) databaseFile.getContentFile(contentType);
                        String statement = ddlFileManager.createDDLStatement(virtualFile, contentType);
                        if (statement.trim().length() > 0) {
                            buffer.append(statement);
                            buffer.append('\n');
                        }
                        if (contentType != contentTypes[contentTypes.length - 1]) buffer.append('\n');
                    }
                } else {
                    DBSourceCodeVirtualFile virtualFile = (DBSourceCodeVirtualFile) databaseFile.getContentFile(fileContentType);
                    buffer.append(ddlFileManager.createDDLStatement(virtualFile, fileContentType));
                    buffer.append('\n');
                }
                Document document = DocumentUtil.getDocument(ddlFile);
                document.setText(buffer.toString());
            }
        }
    }
}

