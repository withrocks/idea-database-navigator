package com.dci.intellij.dbn.editor.data.filter;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.data.sorting.SortingState;
import com.dci.intellij.dbn.editor.data.filter.ui.DatasetCustomFilterForm;
import com.dci.intellij.dbn.object.DBDataset;
import com.intellij.openapi.util.text.StringUtil;
import org.jdom.CDATA;
import org.jdom.Element;

import javax.swing.Icon;

public class DatasetCustomFilter extends DatasetFilterImpl {
    private String condition;

    protected DatasetCustomFilter(DatasetFilterGroup parent, String name) {
        super(parent, name, DatasetFilterType.CUSTOM);
    }

    public void generateName() {}

    public String getVolatileName() {
        ConfigurationEditorForm configurationEditorForm = getSettingsEditor();
        if (configurationEditorForm != null) {
            DatasetCustomFilterForm customFilterForm = (DatasetCustomFilterForm) configurationEditorForm;
            return customFilterForm.getFilterName();
        }
        return super.getDisplayName();
    }

    public boolean isIgnored() {
        return false;
    }

    public Icon getIcon() {
        return getError() == null ?
                Icons.DATASET_FILTER_CUSTOM :
                Icons.DATASET_FILTER_CUSTOM_ERR;
    }

    public String createSelectStatement(DBDataset dataset, SortingState sortingState) {
        setError(null);
        StringBuilder buffer = new StringBuilder();
        DatasetFilterUtil.createSimpleSelectStatement(dataset, buffer);
        buffer.append(" where ");
        buffer.append(condition);
        DatasetFilterUtil.addOrderByClause(dataset, buffer, sortingState);
        DatasetFilterUtil.addForUpdateClause(dataset, buffer);
        return buffer.toString();
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    /*****************************************************
     *                   Configuration                   *
     *****************************************************/
    public ConfigurationEditorForm createConfigurationEditor() {
        DBDataset dataset = lookupDataset();
        return dataset == null ? null : new DatasetCustomFilterForm(dataset, this);
    }

    public void readConfiguration(Element element) {
        super.readConfiguration(element);
        Element conditionElement = element.getChild("condition");
        if (conditionElement.getContentSize() > 0) {
            CDATA cdata = (CDATA) conditionElement.getContent(0);
            condition = cdata.getText();
            condition = StringUtil.replace(condition, "<br>", "\n");
            condition = StringUtil.replace(condition, "<sp>", "  ");
        }
    }

    public void writeConfiguration(Element element) {
        super.writeConfiguration(element);
        element.setAttribute("type", "custom");
        Element conditionElement = new Element("condition");
        element.addContent(conditionElement);
        if (this.condition != null) {
            String condition = StringUtil.replace(this.condition, "\n", "<br>");
            condition = StringUtil.replace(condition, "  ", "<sp>");
            CDATA cdata = new CDATA(condition);
            conditionElement.setContent(cdata);
        }
    }

}
