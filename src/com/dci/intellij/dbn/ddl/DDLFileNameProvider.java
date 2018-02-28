package com.dci.intellij.dbn.ddl;

import com.dci.intellij.dbn.object.common.DBObject;

public class DDLFileNameProvider {
    private DBObject object;
    private DDLFileType ddlFileType;
    private String extension;

    public DDLFileNameProvider(DBObject object, DDLFileType ddlFileType, String extension) {
        this.object = object;
        this.ddlFileType = ddlFileType;
        this.extension = extension;
    }

    public DBObject getObject() {
        return object;
    }

    public DDLFileType getDdlFileType() {
        return ddlFileType;
    }

    public String getExtension() {
        return extension;
    }

    public String getFileName() {
        return object.getRef().getFileName().toLowerCase() + '.' + extension;
    }
}
