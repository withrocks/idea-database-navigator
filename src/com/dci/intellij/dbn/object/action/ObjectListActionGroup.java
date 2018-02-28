package com.dci.intellij.dbn.object.action;

import com.dci.intellij.dbn.object.common.DBObject;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import java.util.List;

public class ObjectListActionGroup extends DefaultActionGroup {

    private ObjectListShowAction listShowAction;
    private List<? extends DBObject> objects;

    public ObjectListActionGroup(ObjectListShowAction listShowAction, List<? extends DBObject> objects) {
        super("", true);
        this.objects = objects;
        this.listShowAction = listShowAction;

        if (objects != null) {
            buildNavigationActions();
        }
    }

    private void buildNavigationActions() {
        for (int i=0; i<objects.size(); i++) {
            if (i == objects.size()) {
                return;
            }
            DBObject object = objects.get(i);
            add(listShowAction.createObjectAction(object));
        }
    }
}