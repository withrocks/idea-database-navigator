package com.dci.intellij.dbn.object.filter.name.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.filter.name.ObjectNameFilterSettings;
import com.dci.intellij.dbn.object.filter.name.ui.ObjectNameFilterSettingsForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;

import java.awt.Component;

public class CreateFilterAction extends ObjectNameFilterAction{
    private static final DBObjectType[] OBJECT_TYPES = new DBObjectType[] {
            DBObjectType.SCHEMA,
            DBObjectType.USER,
            DBObjectType.ROLE,
            DBObjectType.PRIVILEGE,
            DBObjectType.TABLE,
            DBObjectType.VIEW,
            DBObjectType.MATERIALIZED_VIEW,
            DBObjectType.NESTED_TABLE,
            DBObjectType.INDEX,
            DBObjectType.CONSTRAINT,
            DBObjectType.DATASET_TRIGGER,
            DBObjectType.DATABASE_TRIGGER,
            DBObjectType.SYNONYM,
            DBObjectType.SEQUENCE,
            DBObjectType.PROCEDURE,
            DBObjectType.FUNCTION,
            DBObjectType.PACKAGE,
            DBObjectType.TYPE,
            DBObjectType.DIMENSION,
            DBObjectType.CLUSTER,
            DBObjectType.DBLINK,
    };

    public CreateFilterAction(ObjectNameFilterSettingsForm settingsForm) {
        super("New Filter", Icons.DATASET_FILTER_NEW, settingsForm);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {

        DefaultActionGroup actionGroup = new DefaultActionGroup();
        ObjectNameFilterSettings settings = (ObjectNameFilterSettings) getFiltersTree().getModel();

        for (DBObjectType objectType : OBJECT_TYPES) {
            if (!settings.containsFilter(objectType)) {
                actionGroup.add(new CreateFilterForObjectTypeAction(objectType, settingsForm));
            }
        }
        ListPopup popup = JBPopupFactory.getInstance().createActionGroupPopup(
                "Select Object Type",
                actionGroup,
                e.getDataContext(),
                JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                true, null, 10);

        Component component = (Component) e.getInputEvent().getSource();
        popup.showUnderneathOf(component);
    }

    @Override
    public void update(AnActionEvent e) {

    }
}
