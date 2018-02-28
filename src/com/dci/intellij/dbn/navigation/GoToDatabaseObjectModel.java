package com.dci.intellij.dbn.navigation;

import javax.swing.JList;
import javax.swing.ListCellRenderer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.VirtualConnectionHandler;
import com.dci.intellij.dbn.navigation.options.ObjectsLookupSettings;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.common.list.DBObjectListVisitor;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.intellij.ide.util.gotoByName.ChooseByNameModel;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import gnu.trove.THashSet;

public class GoToDatabaseObjectModel implements ChooseByNameModel {
    private Project project;
    private boolean cancelled = false;
    private ConnectionHandler selectedConnection;
    private DBObjectRef<DBSchema> selectedSchema;
    private ObjectsLookupSettings objectsLookupSettings;
    private Object[] EMPTY_ARRAY = new Object[0];
    private String[] EMPTY_STRING_ARRAY = new String[0];


    public GoToDatabaseObjectModel(@NotNull Project project, @Nullable ConnectionHandler selectedConnection, DBSchema selectedSchema) {
        this.project = project;
        this.selectedConnection = selectedConnection;
        objectsLookupSettings = ProjectSettingsManager.getSettings(project).getNavigationSettings().getObjectsLookupSettings();
    }

    public String getPromptText() {
        String connectionIdentifier = selectedConnection == null || selectedConnection instanceof VirtualConnectionHandler ?
                "All Connections" :
                selectedConnection.getName();
        return "Enter database object name (" + connectionIdentifier + ")";
    }

    public String getNotInMessage() {
        return null;
    }

    public String getNotFoundMessage() {
        return "Database object not found";
    }

    public String getCheckBoxName() {
        return objectsLookupSettings.getForceDatabaseLoad().value() ? "Load database objects" : null;
    }

    public char getCheckBoxMnemonic() {
        return 0;
    }

    public boolean loadInitialCheckBoxState() {
        return false;
    }

    public void saveInitialCheckBoxState(boolean state) {
    }

    public ListCellRenderer getListCellRenderer() {
        return new DatabaseObjectListCellRenderer();
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public boolean willOpenEditor() {
        return false;
    }

    public boolean useMiddleMatching() {
        return true;
    }

    @NotNull
    public String[] getNames(boolean checkBoxState) {
        boolean databaseLoadActive = objectsLookupSettings.getForceDatabaseLoad().value();
        boolean forceLoad = checkBoxState && databaseLoadActive;

        ObjectNamesCollector collector = new ObjectNamesCollector(forceLoad);
        scanObjectLists(collector);

        Set<String> bucket = collector.getBucket();
        return bucket == null ?
                EMPTY_STRING_ARRAY :
                bucket.toArray(new String[bucket.size()]);
    }

    @NotNull
    public Object[] getElementsByName(String name, boolean checkBoxState, String pattern) {
        boolean forceLoad = checkBoxState && objectsLookupSettings.getForceDatabaseLoad().value();
        ObjectCollector collector = new ObjectCollector(name, forceLoad);
        scanObjectLists(collector);
        return collector.getBucket() == null ? EMPTY_ARRAY : collector.getBucket().toArray();
    }

    private void scanObjectLists(DBObjectListVisitor visitor) {
        if (selectedConnection == null || selectedConnection instanceof VirtualConnectionHandler) {
            ConnectionManager connectionManager = ConnectionManager.getInstance(project);
            List<ConnectionHandler> connectionHandlers = connectionManager.getConnectionHandlers();
            for (ConnectionHandler connectionHandler : connectionHandlers) {
                if (breakLoad()) break;
                DBObjectListContainer objectListContainer = connectionHandler.getObjectBundle().getObjectListContainer();
                objectListContainer.visitLists(visitor, false);
            }
        } else {
            DBSchema schema = DBObjectRef.get(selectedSchema);
            DBObjectListContainer objectListContainer =
                    schema == null ?
                            selectedConnection.getObjectBundle().getObjectListContainer() :
                            schema.getChildObjects();
            if (objectListContainer != null) {
                objectListContainer.visitLists(visitor, false);
            }
        }
    }

    private class ObjectNamesCollector implements DBObjectListVisitor {
        private boolean forceLoad;
        private DBObject parentObject;
        private Set<String> bucket;

        private ObjectNamesCollector(boolean forceLoad) {
            this.forceLoad = forceLoad;
        }

        public void visitObjectList(DBObjectList<DBObject> objectList) {
            if (isListScannable(objectList) && isParentRelationValid(objectList)) {
                DBObjectType objectType = objectList.getObjectType();
                if (isLookupEnabled(objectType)) {
                    boolean isLookupEnabled = objectsLookupSettings.isEnabled(objectType);
                    DBObject originalParentObject = parentObject;
                    for (DBObject object : objectList.getObjects()) {
                        if (breakLoad()) break;
                        if (isLookupEnabled) {
                            if (bucket == null) bucket = new THashSet<String>();
                            bucket.add(object.getName());
                        }

                        parentObject = object;
                        DBObjectListContainer childObjects = object.getChildObjects();
                        if (childObjects != null) childObjects.visitLists(this, false);
                    }
                    parentObject = originalParentObject;
                }
            }
        }

        private boolean isListScannable(DBObjectList<DBObject> objectList) {
            return objectList != null && (objectList.isLoaded() || objectList.canLoadFast() || forceLoad);
        }

        private boolean isParentRelationValid(DBObjectList<DBObject> objectList) {
            return parentObject == null || objectList.getObjectType().isChildOf(parentObject.getObjectType());
        }

        public Set<String> getBucket() {
            return bucket;
        }
    }

    private boolean isLookupEnabled(DBObjectType objectType) {
        boolean enabled = objectsLookupSettings.isEnabled(objectType);
        if (!enabled) {
            for (DBObjectType childObjectType : objectType.getChildren()) {
                if (isLookupEnabled(childObjectType)) {
                    return true;
                }
            }
            return false;
        }
        return enabled;
    }


    private class ObjectCollector implements DBObjectListVisitor {
        private String objectName;
        private boolean forceLoad;
        private DBObject parentObject;
        private List<DBObject> bucket;

        private ObjectCollector(String objectName, boolean forceLoad) {
            this.objectName = objectName;
            this.forceLoad = forceLoad;
        }

        public void visitObjectList(DBObjectList<DBObject> objectList) {
            if (isListScannable(objectList) && isParentRelationValid(objectList)) {
                DBObjectType objectType = objectList.getObjectType();
                if (isLookupEnabled(objectType)) {
                    boolean isLookupEnabled = objectsLookupSettings.isEnabled(objectType);
                    DBObject originalParentObject = parentObject;
                    for (DBObject object : objectList.getObjects()) {
                        if (breakLoad()) break;
                        if (isLookupEnabled && object.getName().equals(objectName)) {
                            if (bucket == null) bucket = new ArrayList<DBObject>();
                            bucket.add(object);
                        }

                        parentObject = object;
                        DBObjectListContainer childObjects = object.getChildObjects();
                        if (childObjects != null) childObjects.visitLists(this, false);
                    }
                    parentObject = originalParentObject;
                }
            }
        }

        private boolean isListScannable(DBObjectList<DBObject> objectList) {
            return objectList != null && (objectList.isLoaded() || objectList.canLoadFast() || forceLoad);
        }

        private boolean isParentRelationValid(DBObjectList<DBObject> objectList) {
            return parentObject == null || objectList.getObjectType().isChildOf(parentObject.getObjectType());
        }

        public List<DBObject> getBucket() {
            return bucket;
        }
    }


    private boolean breakLoad() {
        return cancelled || !ApplicationManager.getApplication().isActive();
    }

    public String getElementName(Object element) {
        if (element instanceof DBObject) {
            DBObject object = (DBObject) element;
            return object.getQualifiedName();
        }

        return element == null ? null : element.toString();
    }

    @NotNull
    public String[] getSeparators() {
        return new String[]{"."};
    }

    public String getFullName(Object element) {
        return getElementName(element);
    }

    public String getHelpId() {
        return null;
    }

    public class DatabaseObjectListCellRenderer extends ColoredListCellRenderer {
        @Override
        protected void customizeCellRenderer(JList list, Object value, int index, boolean selected, boolean hasFocus) {
            if (value instanceof DBObject) {
                DBObject object = (DBObject) value;
                setIcon(object.getIcon());
                append(object.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
                ConnectionHandler connectionHandler = FailsafeUtil.get(object.getConnectionHandler());
                append(" [" + connectionHandler.getName() + "]", SimpleTextAttributes.GRAY_ATTRIBUTES);
                if (object.getParentObject() != null) {
                    append(" - " + object.getParentObject().getQualifiedName(), SimpleTextAttributes.GRAY_ATTRIBUTES);
                }
            } else append(value.toString(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
        }
    }
}
