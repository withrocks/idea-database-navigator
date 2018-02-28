package com.dci.intellij.dbn.common.properties.ui;

import com.dci.intellij.dbn.common.properties.Property;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.StringUtil;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.*;

public class PropertiesTableModel implements TableModel {
    private List<Property> properties = new ArrayList<Property>();
    private Set<TableModelListener> listeners = new HashSet<TableModelListener>();

    public PropertiesTableModel(Map<String, String> propertiesMap) {
        loadProperties(propertiesMap);
    }

    public void loadProperties(Map<String, String> propertiesMap) {
        for (String key : propertiesMap.keySet()) {
            Property property = new Property(key, propertiesMap.get(key));
            properties.add(property);
        }
    }

    public Map<String, String> exportProperties() {
        Map<String, String> propertiesMap = new HashMap<String, String>();

        for (Property property : properties) {
            String key = property.getKey();
            if (!StringUtil.isEmptyOrSpaces(key)) {
                String value = CommonUtil.nvl(property.getValue(), "");
                propertiesMap.put(key, value);
            }
        }
        return propertiesMap;
    }

    public int getRowCount() {
        return properties.size();
    }

    public int getColumnCount() {
        return 2;
    }

    public String getColumnName(int columnIndex) {
        return columnIndex == 0 ? "Property" :
               columnIndex == 1 ? "Value" : null;
    }

    public Class<?> getColumnClass(int columnIndex) {
        return String.class;

    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        return
           columnIndex == 0 ? getKey(rowIndex) :
           columnIndex == 1 ? getValue(rowIndex) : null;
    }

    public void setValueAt(Object o, int rowIndex, int columnIndex) {
        Object actualValue = getValueAt(rowIndex, columnIndex);
        if (!CommonUtil.safeEqual(actualValue, o)) {
            Property property = properties.get(rowIndex);
            if (columnIndex == 0) {
                property.setKey((String) o);

            } else if (columnIndex == 1) {
                property.setValue((String) o);
            }

            notifyListeners(rowIndex, rowIndex, columnIndex);
        }
    }

    public void addTableModelListener(TableModelListener l) {
        listeners.add(l);
    }

    public void removeTableModelListener(TableModelListener l) {
        listeners.remove(l);
    }

    private String getKey(int rowIndex) {
        Property property = getProperty(rowIndex);
        return property.getKey();
    }

    private String getValue(int rowIndex) {
        Property property = getProperty(rowIndex);
        return property.getValue();
    }

    private Property getProperty(int rowIndex) {
        while (properties.size() <= rowIndex) {
            properties.add(new Property());
        }
        return properties.get(rowIndex);
    }

    public void insertRow(int rowIndex) {
        properties.add(rowIndex, new Property());
        notifyListeners(rowIndex, properties.size()-1, -1);
    }

    public void removeRow(int rowIndex) {
        if (properties.size() > rowIndex) {
            properties.remove(rowIndex);
            notifyListeners(rowIndex, properties.size()-1, -1);
        }
    }

    private void notifyListeners(int firstRowIndex, int lastRowIndex, int columnIndex) {
        TableModelEvent modelEvent = new TableModelEvent(this, firstRowIndex, lastRowIndex, columnIndex);
        for (TableModelListener modelListener : listeners) {
            modelListener.tableChanged(modelEvent);
        }
    }
}
