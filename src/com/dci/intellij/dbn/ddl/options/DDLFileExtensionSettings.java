package com.dci.intellij.dbn.ddl.options;

import java.util.ArrayList;
import java.util.List;
import org.jdom.Element;

import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.ddl.DDLFileType;
import com.dci.intellij.dbn.ddl.DDLFileTypeId;
import com.dci.intellij.dbn.ddl.options.ui.DDLFileExtensionSettingsForm;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.language.psql.PSQLFileType;
import com.dci.intellij.dbn.language.sql.SQLFileType;
import com.intellij.openapi.project.Project;

public class DDLFileExtensionSettings extends Configuration<DDLFileExtensionSettingsForm> {

    private List<DDLFileType> fileTypes = new ArrayList<DDLFileType>();

    private DDLFileSettings parent;
    public DDLFileExtensionSettings(DDLFileSettings parent) {
        this.parent = parent;
        fileTypes.add(new DDLFileType(DDLFileTypeId.VIEW, "DDL File - View", "vw", SQLFileType.INSTANCE, DBContentType.CODE));
        fileTypes.add(new DDLFileType(DDLFileTypeId.TRIGGER, "DDL File - Trigger", "trg", PSQLFileType.INSTANCE, DBContentType.CODE));
        fileTypes.add(new DDLFileType(DDLFileTypeId.PROCEDURE, "DDL File - Procedure", "prc", PSQLFileType.INSTANCE, DBContentType.CODE));
        fileTypes.add(new DDLFileType(DDLFileTypeId.FUNCTION, "DDL File - Function", "fnc", PSQLFileType.INSTANCE, DBContentType.CODE));
        fileTypes.add(new DDLFileType(DDLFileTypeId.PACKAGE, "DDL File - Package", "pkg", PSQLFileType.INSTANCE, DBContentType.CODE_SPEC_AND_BODY));
        fileTypes.add(new DDLFileType(DDLFileTypeId.PACKAGE_SPEC, "DDL File - Package Spec", "pks", PSQLFileType.INSTANCE, DBContentType.CODE_SPEC));
        fileTypes.add(new DDLFileType(DDLFileTypeId.PACKAGE_BODY, "DDL File - Package Body", "pkb", PSQLFileType.INSTANCE, DBContentType.CODE_BODY));
        fileTypes.add(new DDLFileType(DDLFileTypeId.TYPE, "DDL File - Type", "tpe", PSQLFileType.INSTANCE, DBContentType.CODE_SPEC_AND_BODY));
        fileTypes.add(new DDLFileType(DDLFileTypeId.TYPE_SPEC, "DDL File - Type Spec", "tps", PSQLFileType.INSTANCE, DBContentType.CODE_SPEC));
        fileTypes.add(new DDLFileType(DDLFileTypeId.TYPE_BODY, "DDL File - Type Body", "tpb", PSQLFileType.INSTANCE, DBContentType.CODE_BODY));
    }

    public String getDisplayName() {
        return "DDL file extension settings";
    }

    public DDLFileType getDDLFileType(String fileTypeId) {
        for (DDLFileType fileType : fileTypes) {
            if (fileType.getId().equals(fileTypeId)) {
                return fileType;
            }
        }
        return null;
    }

    public DDLFileType getDDLFileTypeForExtension(String extension) {
        for (DDLFileType fileType : fileTypes) {
            if (fileType.getExtensions().contains(extension)) {
                return fileType;
            }
        }
        return null;
    }

    public List<DDLFileType> getDDLFileTypes() {
        return fileTypes;
    }

    public Project getProject() {
        return parent.getProject();
    }

    /*********************************************************
     *                      Configuration                    *
     *********************************************************/
    public DDLFileExtensionSettingsForm createConfigurationEditor() {
        return new DDLFileExtensionSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "extensions";
    }

    public void readConfiguration(Element element) {
        for (Object o : element.getChildren()) {
            Element fileTypeElement = (Element) o;
            String name = fileTypeElement.getAttributeValue("file-type-id");
            String extensions = fileTypeElement.getAttributeValue("extensions");

            // workaround after fixing the bad naming of the ddl file types
            if (name.equals("TRIGGER_SPEC")) name = "PACKAGE_SPEC";
            if (name.equals("TRIGGER_BODY")) name = "PACKAGE_BODY";

            DDLFileType fileType = getDDLFileType(name);
            fileType.setExtensions(StringUtil.tokenize(extensions, ","));
        }
    }

    public void writeConfiguration(Element element) {
        for (DDLFileType fileType : fileTypes) {
            Element fileTypeElement = new Element("mapping");
            fileTypeElement.setAttribute("file-type-id", fileType.getId());
            String extensions = StringUtil.concatenate(fileType.getExtensions(), ",");
            fileTypeElement.setAttribute("extensions", extensions);
            element.addContent(fileTypeElement);
        }
    }
}
