package com.dci.intellij.dbn.browser.options;

import java.util.ArrayList;
import java.util.List;
import org.jdom.Element;

import com.dci.intellij.dbn.browser.options.ui.DatabaseBrowserSortingSettingsForm;
import com.dci.intellij.dbn.common.options.ProjectConfiguration;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.sorting.DBObjectComparator;
import com.dci.intellij.dbn.object.common.sorting.SortingType;
import com.intellij.openapi.project.Project;

public class DatabaseBrowserSortingSettings extends ProjectConfiguration<DatabaseBrowserSortingSettingsForm> {
    private List<DBObjectComparator> comparators = new ArrayList<DBObjectComparator>();

    public DatabaseBrowserSortingSettings(Project project) {
        super(project);
        comparators.add(DBObjectComparator.get(DBObjectType.COLUMN, SortingType.NAME));
        comparators.add(DBObjectComparator.get(DBObjectType.FUNCTION, SortingType.NAME));
        comparators.add(DBObjectComparator.get(DBObjectType.PROCEDURE, SortingType.NAME));
        comparators.add(DBObjectComparator.get(DBObjectType.ARGUMENT, SortingType.POSITION));
    }

    public List<DBObjectComparator> getComparators() {
        return comparators;
    }

    public void setComparators(List<DBObjectComparator> comparators) {
        this.comparators = comparators;
    }

    public DBObjectComparator getComparator(DBObjectType objectType) {
        for (DBObjectComparator comparator : comparators) {
            if (comparator.getObjectType().matches(objectType)) {
                return comparator;
            }
        }
        return null;
    }

    private static boolean contains(List<DBObjectComparator> comparators, DBObjectType objectType) {
        for (DBObjectComparator comparator : comparators) {
            if (comparator.getObjectType() == objectType){
                return true;
            }
        }
        return false;
    }

    @Override
    public DatabaseBrowserSortingSettingsForm createConfigurationEditor() {
        return new DatabaseBrowserSortingSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "sorting";
    }

    public String getDisplayName() {
        return "Database Browser";
    }

    public String getHelpTopic() {
        return "browserSettings";
    }

    /*********************************************************
     *                        Custom                         *
     *********************************************************/

    /*********************************************************
     *                     Configuration                     *
     *********************************************************/

    public void readConfiguration(Element element) {
        List<DBObjectComparator> newComparators = new ArrayList<DBObjectComparator>();
        List<Element> children = element.getChildren();
        for (Element child : children) {
            String objectTypeName = child.getAttributeValue("name");
            String sortingTypeName = child.getAttributeValue("sorting-type");
            DBObjectType objectType = DBObjectType.getObjectType(objectTypeName);
            SortingType sortingType = SortingType.valueOf(sortingTypeName);
            DBObjectComparator comparator = DBObjectComparator.get(objectType, sortingType);
            if (comparator != null) {
                newComparators.add(comparator);
            }
        }
        for (DBObjectComparator comparator : comparators) {
            if (!contains(newComparators, comparator.getObjectType())) {
                newComparators.add(comparator);
            }
        }
        comparators = newComparators;
    }

    public void writeConfiguration(Element element) {
        for (DBObjectComparator comparator : comparators) {
            Element child = new Element("object-type");
            child.setAttribute("name", comparator.getObjectType().getName().toUpperCase());
            child.setAttribute("sorting-type", comparator.getSortingType().name());
            element.addContent(child);
        }
    }
}
