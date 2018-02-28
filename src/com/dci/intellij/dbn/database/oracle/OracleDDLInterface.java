package com.dci.intellij.dbn.database.oracle;

import java.sql.Connection;
import java.sql.SQLException;

import com.dci.intellij.dbn.code.common.style.options.CodeStyleCaseOption;
import com.dci.intellij.dbn.code.common.style.options.CodeStyleCaseSettings;
import com.dci.intellij.dbn.code.psql.style.options.PSQLCodeStyleSettings;
import com.dci.intellij.dbn.code.sql.style.options.SQLCodeStyleSettings;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.DatabaseObjectTypeId;
import com.dci.intellij.dbn.database.common.DatabaseDDLInterfaceImpl;
import com.dci.intellij.dbn.ddl.options.DDLFileSettings;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.code.SourceCodeContent;
import com.dci.intellij.dbn.object.factory.ArgumentFactoryInput;
import com.dci.intellij.dbn.object.factory.MethodFactoryInput;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;

public class OracleDDLInterface extends DatabaseDDLInterfaceImpl {
    public OracleDDLInterface(DatabaseInterfaceProvider provider) {
        super("oracle_ddl_interface.xml", provider);
    }

    public String createDDLStatement(Project project, DatabaseObjectTypeId objectTypeId, String userName, String schemaName, String objectName, DBContentType contentType, String code, String alternativeDelimiter) {
        DDLFileSettings ddlFileSettings = DDLFileSettings.getInstance(project);
        boolean useQualified = ddlFileSettings.getGeneralSettings().isUseQualifiedObjectNames();
        boolean makeRerunnable = ddlFileSettings.getGeneralSettings().isMakeScriptsRerunnable();

        CodeStyleCaseSettings styleCaseSettings = SQLCodeStyleSettings.getInstance(project).getCaseSettings();
        CodeStyleCaseOption kco = styleCaseSettings.getKeywordCaseOption();
        CodeStyleCaseOption oco = styleCaseSettings.getObjectCaseOption();

        if (objectTypeId == DatabaseObjectTypeId.VIEW) {
            return kco.format("create" + (makeRerunnable ? " or replace" : "") + " view ") + oco.format((useQualified ? schemaName + "." : "") + objectName) + kco.format(" as\n") + code + "\n/";
        } else {
            String objectType = objectTypeId.toString().toLowerCase();
            if (contentType == DBContentType.CODE_BODY) {
                objectType = objectType + " body";
            }
            code = updateNameQualification(code, useQualified, objectType, schemaName, objectName, styleCaseSettings);
            return kco.format("create" + (makeRerunnable ? " or replace" : "") + "\n") + code + "\n/";
        }
    }

    @Override
    public void computeSourceCodeOffsets(SourceCodeContent content, DatabaseObjectTypeId objectTypeId, String objectName) {
        String sourceCode = content.getSourceCode();
        if (StringUtil.isNotEmpty(sourceCode)) {
            if (objectTypeId == DatabaseObjectTypeId.DATASET_TRIGGER || objectTypeId == DatabaseObjectTypeId.DATABASE_TRIGGER) {
                if (sourceCode.length() > 0) {
                    int startIndex = StringUtil.indexOfIgnoreCase(sourceCode, objectName, 0) + objectName.length();
                    int headerEndOffset = StringUtil.indexOfIgnoreCase(sourceCode, "declare", startIndex);
                    if (headerEndOffset == -1) headerEndOffset = StringUtil.indexOfIgnoreCase(sourceCode, "begin", startIndex);
                    if (headerEndOffset == -1) headerEndOffset = StringUtil.indexOfIgnoreCase(sourceCode, "call", startIndex);
                    if (headerEndOffset == -1) headerEndOffset = 0;
                    content.getOffsets().setHeaderEndOffset(headerEndOffset);
                }
            }

            if (objectTypeId != DatabaseObjectTypeId.VIEW && objectTypeId != DatabaseObjectTypeId.MATERIALIZED_VIEW) {
                int nameIndex = StringUtil.indexOfIgnoreCase(sourceCode, objectName, 0);
                if (nameIndex > -1) {
                    int guardedBlockEndOffset = nameIndex + objectName.length();
                    if (sourceCode.charAt(guardedBlockEndOffset) == '"'){
                        guardedBlockEndOffset++;
                    }
                    content.getOffsets().setGuardedBlockEndOffset(guardedBlockEndOffset);
                }
            }
        }
    }

    /*********************************************************
     *                   CHANGE statements                   *
     *********************************************************/
    public void updateView(String viewName, String oldCode, String newCode, Connection connection) throws SQLException {
        executeUpdate(connection, "change-view", viewName, newCode);
    }

    @Override
    public void updateTrigger(String tableOwner, String tableName, String triggerName, String oldCode, String newCode, Connection connection) throws SQLException {
        updateObject(triggerName, "trigger", oldCode, newCode, connection);
    }

    public void updateObject(String objectName, String objectType, String oldCode, String newCode, Connection connection) throws SQLException {
        // code contains object type and name
        executeUpdate(connection, "change-object", newCode);
    }

    /*********************************************************
     *                   CREATE statements                   *
     *********************************************************/
    public void createMethod(MethodFactoryInput method, Connection connection) throws SQLException {
        CodeStyleCaseSettings styleCaseSettings = PSQLCodeStyleSettings.getInstance(method.getSchema().getProject()).getCaseSettings();
        CodeStyleCaseOption kco = styleCaseSettings.getKeywordCaseOption();
        CodeStyleCaseOption oco = styleCaseSettings.getObjectCaseOption();
        CodeStyleCaseOption dco = styleCaseSettings.getDatatypeCaseOption();

        StringBuilder buffer = new StringBuilder();
        String methodType = method.isFunction() ? "function " : "procedure ";
        buffer.append(kco.format(methodType));
        buffer.append(oco.format(method.getObjectName()));
        buffer.append("(");
        
        int maxArgNameLength = 0;
        int maxArgDirectionLength = 0;
        for (ArgumentFactoryInput argument : method.getArguments()) {
            maxArgNameLength = Math.max(maxArgNameLength, argument.getObjectName().length());
            maxArgDirectionLength = Math.max(maxArgDirectionLength,
                    argument.isInput() && argument.isOutput() ? 6 :
                    argument.isInput() ? 2 :
                    argument.isOutput() ? 3 : 0);
        }


        for (ArgumentFactoryInput argument : method.getArguments()) {
            buffer.append("\n    ");
            buffer.append(oco.format(argument.getObjectName()));
            buffer.append(StringUtil.repeatSymbol(' ', maxArgNameLength - argument.getObjectName().length() + 1));
            String direction =
                    argument.isInput() && argument.isOutput() ? kco.format("in out") :
                    argument.isInput() ? kco.format("in") :
                    argument.isOutput() ? kco.format("out") : "";
            buffer.append(direction);
            buffer.append(StringUtil.repeatSymbol(' ', maxArgDirectionLength - direction.length() + 1));
            buffer.append(dco.format(argument.getDataType()));
            if (argument != method.getArguments().get(method.getArguments().size() -1)) {
                buffer.append(",");
            }
        }

        buffer.append(")\n");
        if (method.isFunction()) {
            buffer.append(kco.format("return "));
            buffer.append(dco.format(method.getReturnArgument().getDataType()));
            buffer.append("\n");
        }
        buffer.append(kco.format("is\nbegin\n\n"));
        if (method.isFunction()) buffer.append(kco.format("    return null;\n\n"));
        buffer.append("end;");
        createObject(buffer.toString(), connection);
    }
}