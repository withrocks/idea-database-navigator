package com.dci.intellij.dbn.editor.data.filter;

import java.util.HashMap;
import java.util.Map;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.data.model.ColumnInfo;
import com.dci.intellij.dbn.editor.data.DatasetEditorManager;
import com.dci.intellij.dbn.editor.data.filter.ui.DatasetFilterDialog;
import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBDataset;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.project.Project;

@State(
    name = "DBNavigator.Project.DatasetFilterManager",
    storages = {
        @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/dbnavigator.xml", scheme = StorageScheme.DIRECTORY_BASED),
        @Storage(file = StoragePathMacros.PROJECT_FILE)}
)
public class DatasetFilterManager extends AbstractProjectComponent implements PersistentStateComponent<Element> {
    public static final DatasetFilter EMPTY_FILTER = new DatasetEmptyFilter();
    private Map<String, Map<String, DatasetFilterGroup>> filters =  new HashMap<String, Map<String, DatasetFilterGroup>>();

    private DatasetFilterManager(Project project) {
        super(project);
    }

    public void switchActiveFilter(DBDataset dataset, DatasetFilter filter){
        Project project = dataset.getProject();
        DatasetFilterManager filterManager = DatasetFilterManager.getInstance(project);
        DatasetFilter activeFilter = filterManager.getActiveFilter(dataset);
        if (activeFilter != filter) {
            filterManager.setActiveFilter(dataset, filter);

        }
    }

    public int openFiltersDialog(DBDataset dataset, boolean isAutomaticPrompt, boolean createNewFilter, DatasetFilterType defaultFilterType) {
        DatasetFilterDialog filterDialog = new DatasetFilterDialog(dataset, isAutomaticPrompt, createNewFilter, defaultFilterType);
        filterDialog.show();
        return filterDialog.getExitCode();
    }

    public void createBasicFilter(DBDataset dataset, String columnName, Object columnValue, ConditionOperator operator, boolean interactive) {
        DatasetFilterGroup filterGroup = getFilterGroup(dataset);
        DatasetBasicFilter filter = filterGroup.createBasicFilter(columnName, columnValue, operator, interactive);

        if (interactive) {
            DatasetFilterDialog filterDialog = new DatasetFilterDialog(dataset, filter);
            filterDialog.show();
        } else {
            filter.setNew(false);
            filter.setTemporary(true);
            setActiveFilter(dataset, filter);
            DatasetEditorManager.getInstance(getProject()).reloadEditorData(dataset);
        }
    }

    public void createBasicFilter(DBDataset dataset, String columnName, Object columnValue, ConditionOperator operator) {
        DatasetFilterGroup filterGroup = getFilterGroup(dataset);
        DatasetBasicFilter filter = filterGroup.createBasicFilter(columnName, columnValue, operator);

        filter.setNew(false);
        filter.setTemporary(true);
        setActiveFilter(dataset, filter);
        DatasetEditorManager.getInstance(getProject()).reloadEditorData(dataset);
    }

    public void createBasicFilter(DatasetFilterInput filterInput) {
        DBDataset dataset = filterInput.getDataset();
        DatasetFilterGroup filterGroup = getFilterGroup(dataset);
        DatasetBasicFilter filter = null;

        for (DBColumn column : filterInput.getColumns()) {
            Object value = filterInput.getColumnValue(column);
            if (filter == null) {
                filter = filterGroup.createBasicFilter(column.getName(), value, ConditionOperator.EQUAL);
            } else {
                filter.addCondition(column.getName(), value, ConditionOperator.EQUAL);
            }
        }

        filter.setNew(false);
        filter.setTemporary(true);
        setActiveFilter(dataset, filter);
        DatasetEditorManager.getInstance(getProject()).reloadEditorData(dataset);

    }

    public void addConditionToFilter(DatasetBasicFilter filter, DBDataset dataset, ColumnInfo columnInfo, Object value, boolean interactive) {
        DatasetFilterGroup filterGroup = getFilterGroup(dataset);
        DatasetBasicFilterCondition condition = interactive ?
                new DatasetBasicFilterCondition(filter, columnInfo.getName(), value, ConditionOperator.EQUAL, true) :
                new DatasetBasicFilterCondition(filter, columnInfo.getName(), value, null);

        filter.addCondition(condition);
        filter.generateName();
        filterGroup.setActiveFilter(filter);
        if (interactive) {
            DatasetFilterDialog filterDialog = new DatasetFilterDialog(dataset, false, false, DatasetFilterType.NONE);
            filterDialog.show();
        } else {
            DatasetEditorManager.getInstance(getProject()).reloadEditorData(dataset);
        }

    }



    public DatasetFilter getActiveFilter(DBDataset dataset) {
        DatasetFilterGroup filterGroup = getFilterGroup(dataset);
        return filterGroup.getActiveFilter();
    }

    public void setActiveFilter(DBDataset dataset, DatasetFilter filter) {
        DatasetFilterGroup filterGroup = getFilterGroup(dataset);
        filterGroup.setActiveFilter(filter);
    }

    private void addFilterGroup(DatasetFilterGroup filterGroup) {
        String connectionId = filterGroup.getConnectionId();
        String datasetName = filterGroup.getDatasetName();
        Map<String, DatasetFilterGroup> connectionFilters = filters.get(connectionId);
        if (connectionFilters == null) {
            connectionFilters = new HashMap<String, DatasetFilterGroup>();
            filters.put(connectionId, connectionFilters);
        }

        connectionFilters.put(datasetName, filterGroup);
    }

    public DatasetFilterGroup getFilterGroup(DBDataset dataset) {
        ConnectionHandler connectionHandler = FailsafeUtil.get(dataset.getConnectionHandler());
        String connectionId = connectionHandler.getId();
        String datasetName = dataset.getQualifiedName();
        return getFilterGroup(connectionId, datasetName);
    }

    public DatasetFilterGroup getFilterGroup(DatasetFilter filter) {
        String connectionId = filter.getConnectionId();
        String datasetName = filter.getDatasetName();
        return getFilterGroup(connectionId, datasetName);
    }

    @NotNull
    public DatasetFilterGroup getFilterGroup(String connectionId, String datasetName) {
        Map<String, DatasetFilterGroup> filterGroups = filters.get(connectionId);
        if (filterGroups == null) {
            filterGroups = new HashMap<String, DatasetFilterGroup>();
            filters.put(connectionId, filterGroups);
        }
        DatasetFilterGroup filterGroup = filterGroups.get(datasetName);
        if (filterGroup == null) {
            filterGroup = new DatasetFilterGroup(getProject(), connectionId, datasetName);
            filterGroups.put(datasetName, filterGroup);
        }
        return filterGroup;
    }

    public static DatasetFilterManager getInstance(Project project) {
        return project.getComponent(DatasetFilterManager.class);
    }

    /***************************************
    *            ProjectComponent           *
    ****************************************/
    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.Project.DatasetFilterManager";
    }
    public void disposeComponent() {
        filters.clear();
        super.disposeComponent();
    }

    /****************************************
     *       PersistentStateComponent       *
     *****************************************/
    @Nullable
    @Override
    public Element getState() {
        Element element = new Element("state");
        for (String connectionId : filters.keySet()){
            ConnectionManager connectionManager = ConnectionManager.getInstance(getProject());
            if (connectionManager.getConnectionHandler(connectionId) != null) {
                Map<String, DatasetFilterGroup> filterLists = filters.get(connectionId);
                for (String datasetName : filterLists.keySet()) {
                    DatasetFilterGroup filterGroup = filterLists.get(datasetName);
                    Element filterListElement = new Element("filter-list");
                    filterGroup.writeConfiguration(filterListElement);
                    element.addContent(filterListElement);
                }
            }
        }
        return element;
    }

    @Override
    public void loadState(Element element) {
        for (Object object : element.getChildren()) {
            Element filterListElement = (Element) object;
            DatasetFilterGroup filterGroup = new DatasetFilterGroup(getProject());
            filterGroup.readConfiguration(filterListElement);
            addFilterGroup(filterGroup);
        }
    }

}
