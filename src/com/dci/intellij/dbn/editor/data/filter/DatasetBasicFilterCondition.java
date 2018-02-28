package com.dci.intellij.dbn.editor.data.filter;

import java.text.ParseException;
import java.util.Date;
import org.jdom.Element;

import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.locale.options.RegionalSettings;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.data.type.GenericDataType;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.editor.data.filter.ui.DatasetBasicFilterConditionForm;
import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBDataset;

public class DatasetBasicFilterCondition extends Configuration<DatasetBasicFilterConditionForm> {

    private DatasetBasicFilter filter;
    private String columnName = "";
    private String operator = "";
    private String value = "";
    private boolean active = true;

    public DatasetBasicFilterCondition(DatasetBasicFilter filter){
        this.filter = filter;
    }

    public DatasetBasicFilterCondition(DatasetBasicFilter filter, String columnName, Object value, ConditionOperator operator, boolean active) {
        this.filter = filter;
        this.columnName = columnName;
        this.operator = operator.getText();
        this.value = value == null ? "" : value.toString();
        this.active = active;
    }

    public DatasetBasicFilterCondition(DatasetBasicFilter filter, String columnName, Object value, ConditionOperator operator) {
        this.filter = filter;
        this.columnName = columnName;
        this.operator = operator == null ? (value == null || value.toString().trim().length() == 0 ? ConditionOperator.IS_NULL.getText() : ConditionOperator.EQUAL.getText()) : operator.getText();
        this.value = value == null ? "" : value.toString();
        this.active = true;
    }

    public DatasetBasicFilter getFilter() {
        return filter;
    }

    public String getDisplayName() {
        return null;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void appendConditionString(StringBuilder buffer, DBDataset dataset) {
        DatasetBasicFilterConditionForm editorForm = getSettingsEditor();

        ConditionOperator conditionOperator = null;
        String columnName = this.columnName;
        String operator = this.operator;
        String value = this.value;

        if (editorForm != null && !editorForm.isDisposed()) {
            conditionOperator = editorForm.getSelectedOperator();
            DBColumn selectedColumn = editorForm.getSelectedColumn();
            if (selectedColumn != null) {
                columnName = selectedColumn.getName();
                operator = conditionOperator == null ? " " : conditionOperator.getText();
                value = editorForm.getValue();
            }
        }

        DBColumn column = dataset.getColumn(columnName);

        if (conditionOperator != null &&
                conditionOperator.getValuePrefix() != null &&
                conditionOperator.getValuePostfix() != null) {
            value = conditionOperator.getValuePrefix() + value + conditionOperator.getValuePostfix();
        }
        else if (StringUtil.isNotEmptyOrSpaces(value)) {
            DBDataType dataType = column == null ? null : column.getDataType();
            if (dataType != null && dataType.isNative()) {
                ConnectionHandler connectionHandler = FailsafeUtil.get(dataset.getConnectionHandler());
                RegionalSettings regionalSettings = RegionalSettings.getInstance(connectionHandler.getProject());
                GenericDataType genericDataType = dataType.getGenericDataType();
                if (genericDataType == GenericDataType.LITERAL) {
                    value = com.intellij.openapi.util.text.StringUtil.replace(value, "'", "''");
                    value = "'" + value + "'";
                } else if (genericDataType == GenericDataType.DATE_TIME) {
                    DatabaseMetadataInterface metadataInterface = connectionHandler.getInterfaceProvider().getMetadataInterface();
                    try {
                        Date date = regionalSettings.getFormatter().parseDateTime(value);
                        value = metadataInterface.createDateString(date);
                    } catch (ParseException e) {
                        try {
                            Date date = regionalSettings.getFormatter().parseDate(value);
                            value = metadataInterface.createDateString(date);
                        } catch (ParseException e1) {
                            // value can be something like "sysdate" => not parseable
                            //e1.printStackTrace();
                        }
                    }
                } else if (genericDataType == GenericDataType.NUMERIC) {
                /*try {
                    regionalSettings.getFormatter().parseNumber(value);
                } catch (ParseException e) {
                    e.printStackTrace();

                }*/
                }
            }
        }
        buffer.append(column == null ? columnName : column.getQuotedName(false));
        buffer.append(" ");
        buffer.append(operator);
        buffer.append(" ");
        buffer.append(value);
    }

   /****************************************************
    *                   Configuration                  *
    ****************************************************/
    public DatasetBasicFilterConditionForm createConfigurationEditor() {
        DBDataset dataset = filter.lookupDataset();
        return new DatasetBasicFilterConditionForm(dataset, this);
    }

    public void readConfiguration(Element element) {
       columnName = element.getAttributeValue("column");
       operator = element.getAttributeValue("operator");
       value = element.getAttributeValue("value");
       active = Boolean.parseBoolean(element.getAttributeValue("active"));
    }

    public void writeConfiguration(Element element) {
        element.setAttribute("column", columnName);
        element.setAttribute("operator", operator);
        element.setAttribute("value", value);
        element.setAttribute("active", Boolean.toString(active));
    }
}
