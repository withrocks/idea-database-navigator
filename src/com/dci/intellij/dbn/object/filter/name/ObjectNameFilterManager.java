package com.dci.intellij.dbn.object.filter.name;

import javax.swing.JTree;
import javax.swing.tree.TreePath;
import java.util.List;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.filter.name.ui.EditFilterConditionDialog;
import com.dci.intellij.dbn.object.filter.name.ui.EditFilterConditionForm;
import com.dci.intellij.dbn.object.filter.name.ui.ObjectNameFilterSettingsForm;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;

@State(
        name = "DBNavigator.Project.ObjectNameFilterManager",
        storages = {
                @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/dbnavigator.xml", scheme = StorageScheme.DIRECTORY_BASED),
                @Storage(file = StoragePathMacros.PROJECT_FILE)}
)
public class ObjectNameFilterManager extends AbstractProjectComponent implements PersistentStateComponent<Element> {

    private ObjectNameFilterManager(Project project) {
        super(project);
    }

    public void createFilter(DBObjectType objectType, ObjectNameFilterSettingsForm settingsForm) {
        EditFilterConditionDialog dialog =
                new EditFilterConditionDialog(getProject(), null, null, objectType, EditFilterConditionForm.Operation.CREATE);
        dialog.show();
        if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
            settingsForm.getConfiguration().setModified(true);
            JTree filtersTree = settingsForm.getFiltersTree();
            ObjectNameFilterSettings settings = (ObjectNameFilterSettings) filtersTree.getModel();
            ObjectNameFilter objectNameFilter = new ObjectNameFilter(settings, objectType, dialog.getCondition());
            settings.addFilter(objectNameFilter);

            TreePath treePath = settings.createTreePath(objectNameFilter);
            filtersTree.expandPath(treePath);
            filtersTree.setSelectionPath(treePath);
        }
    }

    public void createFilterCondition(CompoundFilterCondition parentCondition, ObjectNameFilterSettingsForm settingsForm) {
        EditFilterConditionDialog dialog =
                new EditFilterConditionDialog(getProject(), parentCondition, null, parentCondition.getObjectType(), EditFilterConditionForm.Operation.CREATE);
        dialog.show();
        if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
            settingsForm.getConfiguration().setModified(true);
            SimpleFilterCondition newCondition = dialog.getCondition();
            parentCondition.addCondition(newCondition);
            ConditionJoinType joinType = dialog.getJoinType();
            if (joinType != null) {
                parentCondition.setJoinType(joinType);
            }

            ObjectNameFilterSettings settings = (ObjectNameFilterSettings) settingsForm.getFiltersTree().getModel();
            TreePath treePath = settings.createTreePath(newCondition);
            settingsForm.getFiltersTree().setSelectionPath(treePath);
        }
    }

    public void switchConditionJoinType(CompoundFilterCondition condition, ObjectNameFilterSettingsForm settingsForm) {
        condition.setJoinType(condition.getJoinType() == ConditionJoinType.AND ?
                ConditionJoinType.OR :
                ConditionJoinType.AND);

        ObjectNameFilterSettings settings = (ObjectNameFilterSettings) settingsForm.getFiltersTree().getModel();
        settings.notifyNodeChanged(condition);
        settings.notifyChildNodesChanged(condition);
    }


    public void joinFilterCondition(SimpleFilterCondition condition, ObjectNameFilterSettingsForm settingsForm) {
        EditFilterConditionDialog dialog =
                new EditFilterConditionDialog(getProject(), condition.getParent(), null, condition.getObjectType(), EditFilterConditionForm.Operation.JOIN);
        dialog.show();
        if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
            settingsForm.getConfiguration().setModified(true);
            SimpleFilterCondition newCondition = dialog.getCondition();
            ConditionJoinType joinType = dialog.getJoinType();
            CompoundFilterCondition parent = condition.getParent();
            if (parent.getConditions().size() == 1) {
                parent.setJoinType(joinType);
                parent.addCondition(newCondition);
            } else {
                CompoundFilterCondition compoundFilterCondition = new CompoundFilterCondition(joinType);
                parent.addCondition(compoundFilterCondition, parent.getConditions().indexOf(condition));
                parent.removeCondition(condition, false);
                compoundFilterCondition.addCondition(condition);
                compoundFilterCondition.addCondition(newCondition);
                settingsForm.getFiltersTree().expandPath(compoundFilterCondition.getSettings().createTreePath(compoundFilterCondition));
            }
        }
    }

    public void editFilterCondition(SimpleFilterCondition condition, ObjectNameFilterSettingsForm settingsForm) {
        EditFilterConditionDialog dialog =
                new EditFilterConditionDialog(getProject(), condition.getParent(), condition, condition.getObjectType(), EditFilterConditionForm.Operation.EDIT);
        dialog.show();
        if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
            settingsForm.getConfiguration().setModified(true);
            SimpleFilterCondition newCondition = dialog.getCondition();
            condition.setOperator(newCondition.getOperator());
            condition.setText(newCondition.getText());
            condition.getSettings().notifyNodeChanged(condition);
        }
    }

    public void removeFilterCondition(FilterCondition condition, ObjectNameFilterSettingsForm settingsForm) {
        settingsForm.getConfiguration().setModified(true);
        ObjectNameFilterSettings model = (ObjectNameFilterSettings) settingsForm.getFiltersTree().getModel();
        ObjectNameFilterSettings settings = (ObjectNameFilterSettings) model.getRoot();
        if (condition instanceof ObjectNameFilter) {
            ObjectNameFilter filter = (ObjectNameFilter) condition;
            List<ObjectNameFilter> filters = settings.getFilters();
            int index = filters.indexOf(filter);
            settings.removeFilter(filter);

            // select next element
            if (index >= filters.size()) index = filters.size() -1;
            if (index > -1) {
                ObjectNameFilter selectionFilter = filters.get(index);
                TreePath treePath = settings.createTreePath(selectionFilter);
                settingsForm.getFiltersTree().setSelectionPath(treePath);
            }
        } else {
            CompoundFilterCondition parent = condition.getParent();
            List<FilterCondition> conditions = parent.getConditions();
            int index = conditions.indexOf(condition);
            parent.removeCondition(condition, true);

            // select next element
            if (index >= conditions.size()) index = conditions.size() -1;
            if (index > -1 && index < conditions.size()) {
                FilterCondition selectionCondition = conditions.get(index);
                TreePath treePath = settings.createTreePath(selectionCondition);
                settingsForm.getFiltersTree().setSelectionPath(treePath);
            }
        }
    }

    public void moveFilterConditionUp(FilterCondition condition, ObjectNameFilterSettingsForm settingsForm) {
        settingsForm.getConfiguration().setModified(true);
        if (condition instanceof ObjectNameFilter) {
            ObjectNameFilter filter = (ObjectNameFilter) condition;
            ObjectNameFilterSettings settings = filter.getSettings();
            int oldIndex = settings.getFilters().indexOf(filter);
            if (oldIndex > 0) {
                settings.removeFilter(filter);
                settings.addFilter(filter, oldIndex - 1);
            }

        } else {
            CompoundFilterCondition parentCondition = condition.getParent();
            int oldIndex = parentCondition.getConditions().indexOf(condition);
            if (oldIndex > 0) {
                parentCondition.removeCondition(condition, false);
                parentCondition.addCondition(condition, oldIndex - 1);
            }
        }

        JTree filtersTree = settingsForm.getFiltersTree();
        ObjectNameFilterSettings settings = (ObjectNameFilterSettings) filtersTree.getModel();
        TreePath treePath = settings.createTreePath(condition);
        filtersTree.setSelectionPath(treePath);
        filtersTree.expandPath(treePath);
    }



    public void moveFilterConditionDown(FilterCondition condition, ObjectNameFilterSettingsForm settingsForm) {
        if (condition instanceof ObjectNameFilter) {
            ObjectNameFilter filter = (ObjectNameFilter) condition;
            ObjectNameFilterSettings settings = filter.getSettings();
            int oldIndex = settings.getFilters().indexOf(filter);
            settings.removeFilter(filter);
            settings.addFilter(filter, oldIndex + 1);

        } else {
            CompoundFilterCondition parentCondition = condition.getParent();
            int oldIndex = parentCondition.getConditions().indexOf(condition);
            parentCondition.removeCondition(condition, false);
            parentCondition.addCondition(condition, oldIndex + 1);
        }

        JTree filtersTree = settingsForm.getFiltersTree();
        ObjectNameFilterSettings settings = (ObjectNameFilterSettings) filtersTree.getModel();
        TreePath treePath = settings.createTreePath(condition);
        filtersTree.setSelectionPath(treePath);
        filtersTree.expandPath(treePath);

    }

    /***************************************
     *            ProjectComponent         *
     ***************************************/
    public static ObjectNameFilterManager getInstance(@NotNull Project project) {
        return project.getComponent(ObjectNameFilterManager.class);
    }

    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.Project.ObjectNameFilterManager";
    }

    /*********************************************
     *            PersistentStateComponent       *
     *********************************************/
    @Nullable
    @Override
    public Element getState() {
        return null;
    }

    @Override
    public void loadState(Element element) {

    }
}
