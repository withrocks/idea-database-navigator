package com.dci.intellij.dbn.object.action;

import java.util.List;

import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.execution.compiler.action.CompileActionGroup;
import com.dci.intellij.dbn.execution.method.action.ExecuteActionGroup;
import com.dci.intellij.dbn.execution.method.action.RunMethodAction;
import com.dci.intellij.dbn.execution.method.action.RunProgramMethodAction;
import com.dci.intellij.dbn.generator.action.GenerateStatementActionGroup;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBProgram;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationList;
import com.dci.intellij.dbn.object.common.property.DBObjectProperties;
import com.dci.intellij.dbn.object.common.property.DBObjectProperty;
import com.dci.intellij.dbn.object.dependency.action.DependenciesActionGroup;
import com.intellij.openapi.actionSystem.DefaultActionGroup;

public class ObjectActionGroup extends DefaultActionGroup {

    public ObjectActionGroup(DBObject object) {
        DBObjectProperties properties = object.getProperties();
        if(object instanceof DBSchemaObject) {
            DBSchemaObject schemaObject = (DBSchemaObject) object;

            if (properties.is(DBObjectProperty.EDITABLE)) {
                DBContentType contentType = schemaObject.getContentType();
                if (contentType == DBContentType.DATA || contentType == DBContentType.CODE_AND_DATA) {
                    add(new EditObjectDataAction(schemaObject));
                } 

                if (contentType == DBContentType.CODE || contentType == DBContentType.CODE_AND_DATA || contentType == DBContentType.CODE_SPEC_AND_BODY) {
                    add(new EditObjectCodeAction(schemaObject));
                }
            }

            if (properties.is(DBObjectProperty.COMPILABLE) && DatabaseFeature.OBJECT_INVALIDATION.isSupported(object)) {
                add(new CompileActionGroup(schemaObject));
            }

            if (properties.is(DBObjectProperty.DISABLEABLE) && DatabaseFeature.OBJECT_DISABLING.isSupported(object)) {
                add(new EnableDisableAction(schemaObject));
            }
        }

        if (object instanceof DBMethod ) {
            if (DatabaseFeature.DEBUGGING.isSupported(object)) {
                add(new ExecuteActionGroup((DBSchemaObject) object));
            } else {
                add(new RunMethodAction((DBMethod) object));
            }
        }

        if (object instanceof DBProgram && properties.is(DBObjectProperty.SCHEMA_OBJECT)) {
            if (DatabaseFeature.DEBUGGING.isSupported(object)) {
                add(new ExecuteActionGroup((DBSchemaObject) object));
            } else {
                add(new RunProgramMethodAction((DBProgram) object));
            }
        }

        if(object instanceof DBSchemaObject) {
            if (properties.is(DBObjectProperty.SCHEMA_OBJECT)) {
                add(new DropObjectAction((DBSchemaObject) object));

                //add(new TestAction(object));
            }

            if(properties.is(DBObjectProperty.REFERENCEABLE) && DatabaseFeature.OBJECT_DEPENDENCIES.isSupported(object)) {
                addSeparator();
                add (new DependenciesActionGroup((DBSchemaObject) object));
            }
        }

        List<DBObjectNavigationList> navigationLists = object.getNavigationLists();
        if (navigationLists != null && navigationLists.size() > 0) {
            if (!properties.is(DBObjectProperty.REFERENCEABLE)) addSeparator();
            //add(new DbsGoToActionGroup(linkLists));
            for (DBObjectNavigationList navigationList : navigationLists) {
                if (navigationList.isLazy()) {
                    add(new ObjectLazyNavigationListAction(object.getParentObject(), navigationList));
                } else {
                    add(new ObjectNavigationListActionGroup(object.getParentObject(), navigationList, false));
                }
            }
        }
        
        if (getChildrenCount() > 0){
            addSeparator();
        }
        addActionGroup(new GenerateStatementActionGroup(object));
        addSeparator();
        add(new RefreshActionGroup(object));

        //add(new ObjectPropertiesAction(object));
        //add(new TestAction(object));
    }

    private void addActionGroup(DefaultActionGroup actionGroup) {
        if (actionGroup.getChildrenCount() > 0) {
            add(actionGroup);
        }

    }



}
