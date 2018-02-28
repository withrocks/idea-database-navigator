package com.dci.intellij.dbn.navigation.options;

import javax.swing.Icon;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.jdom.Element;

import com.dci.intellij.dbn.common.options.ProjectConfiguration;
import com.dci.intellij.dbn.common.options.setting.BooleanSetting;
import com.dci.intellij.dbn.common.ui.list.Selectable;
import com.dci.intellij.dbn.navigation.options.ui.ObjectsLookupSettingsForm;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;

public class ObjectsLookupSettings extends ProjectConfiguration<ObjectsLookupSettingsForm> {
    private List<ObjectTypeEntry> lookupObjectTypes;
    private Set<DBObjectType> fastLookupObjectTypes;
    private BooleanSetting forceDatabaseLoad = new BooleanSetting("force-database-load", false);
    private BooleanSetting promptConnectionSelection = new BooleanSetting("prompt-connection-selection", true);
    private BooleanSetting promptSchemaSelection = new BooleanSetting("prompt-schema-selection", true);

    public ObjectsLookupSettings(Project project) {
        super(project);
    }

    @Override
    public ObjectsLookupSettingsForm createConfigurationEditor() {
        return new ObjectsLookupSettingsForm(this);
    }

    @Override
    public void apply() throws ConfigurationException {
        super.apply();
        fastLookupObjectTypes = null;
    }

    public boolean isEnabled(DBObjectType objectType) {
        if (fastLookupObjectTypes == null) {
            fastLookupObjectTypes = EnumSet.noneOf(DBObjectType.class);
            for (ObjectTypeEntry objectTypeEntry : getLookupObjectTypes()) {
                if (objectTypeEntry.isSelected()) {
                    fastLookupObjectTypes.add(objectTypeEntry.getObjectType());
                }
            }
        }
        return fastLookupObjectTypes.contains(objectType);
    }

    public BooleanSetting getForceDatabaseLoad() {
        return forceDatabaseLoad;
    }

    public BooleanSetting getPromptConnectionSelection() {
        return promptConnectionSelection;
    }

    public BooleanSetting getPromptSchemaSelection() {
        return promptSchemaSelection;
    }

    private ObjectTypeEntry getObjectTypeEntry(DBObjectType objectType) {
        for (ObjectTypeEntry objectTypeEntry : getLookupObjectTypes()) {
            DBObjectType visibleObjectType = objectTypeEntry.getObjectType();
            if (visibleObjectType == objectType || objectType.isInheriting(visibleObjectType)) {
                return objectTypeEntry;
            }
        }
        return null;
    }

    public synchronized List<ObjectTypeEntry> getLookupObjectTypes() {
        if (lookupObjectTypes == null) {
            lookupObjectTypes = new ArrayList<ObjectTypeEntry>();
            lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.SCHEMA, true));
            lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.USER, false));
            lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.ROLE, false));
            lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.PRIVILEGE, false));
            lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.CHARSET, false));
            lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.TABLE, true));
            lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.VIEW, true));
            lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.MATERIALIZED_VIEW, true));
            lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.NESTED_TABLE, false));
            lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.COLUMN, false));
            lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.INDEX, true));
            lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.CONSTRAINT, true));
            lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.DATASET_TRIGGER, true));
            lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.DATABASE_TRIGGER, true));
            lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.SYNONYM, false));
            lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.SEQUENCE, true));
            lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.PROCEDURE, true));
            lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.FUNCTION, true));
            lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.PACKAGE, true));
            lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.TYPE, true));
            lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.TYPE_ATTRIBUTE, false));
            lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.ARGUMENT, false));

            lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.DIMENSION, false));
            lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.CLUSTER, false));
            lookupObjectTypes.add(new ObjectTypeEntry(DBObjectType.DBLINK, true));
        }
        return lookupObjectTypes;
    }

    /****************************************************
     *                   Configuration                  *
     ****************************************************/
    @Override
    public String getConfigElementName() {
        return "lookup-filters";
    }

    public void readConfiguration(Element element) {
        Element visibleObjectsElement = element.getChild("lookup-objects");
        for (Object o : visibleObjectsElement.getChildren()) {
            Element child = (Element) o;
            String typeName = child.getAttributeValue("name");
            DBObjectType objectType = DBObjectType.getObjectType(typeName);
            if (objectType != null) {
                boolean enabled = Boolean.parseBoolean(child.getAttributeValue("enabled"));
                ObjectTypeEntry objectTypeEntry = getObjectTypeEntry(objectType);
                if (objectTypeEntry != null) {
                    objectTypeEntry.setSelected(enabled);
                }
            }
        }
        forceDatabaseLoad.readConfiguration(element);
        promptConnectionSelection.readConfiguration(element);
        promptSchemaSelection.readConfiguration(element);
    }

    public void writeConfiguration(Element element) {
        Element visibleObjectsElement = new Element("lookup-objects");
        element.addContent(visibleObjectsElement);

        for (ObjectTypeEntry objectTypeEntry : getLookupObjectTypes()) {
            Element child = new Element("object-type");
            child.setAttribute("name", objectTypeEntry.getName());
            child.setAttribute("enabled", Boolean.toString(objectTypeEntry.isSelected()));
            visibleObjectsElement.addContent(child);
        }
        forceDatabaseLoad.writeConfiguration(element);
        promptConnectionSelection.writeConfiguration(element);
        promptSchemaSelection.writeConfiguration(element);
    }
    
    private class ObjectTypeEntry implements Selectable<ObjectTypeEntry> {
        private DBObjectType objectType;
        private boolean enabled = true;

        private ObjectTypeEntry(DBObjectType objectType) {
            this.objectType = objectType;
        }

        private ObjectTypeEntry(DBObjectType objectType, boolean enabled) {
            this.objectType = objectType;
            this.enabled = enabled;
        }

        public DBObjectType getObjectType() {
            return objectType;
        }

        public Icon getIcon() {
            return objectType.getIcon();
        }

        public String getName() {
            return objectType.getName().toUpperCase();
        }

        public String getError() {
            return null;
        }

        public boolean isSelected() {
            return enabled;
        }

        public boolean isMasterSelected() {
            return true;
        }

        public void setSelected(boolean selected) {
            this.enabled = selected;
        }

        @Override
        public int compareTo(ObjectTypeEntry remote) {
            return objectType.compareTo(remote.objectType);
        }
    }
}
