package com.dci.intellij.dbn.data.record.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.data.record.ColumnSortingType;
import com.dci.intellij.dbn.data.record.DatasetRecord;
import com.dci.intellij.dbn.data.record.RecordViewInfo;
import com.dci.intellij.dbn.editor.data.DatasetEditorManager;
import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBDataset;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.UIUtil;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RecordViewerForm extends DBNFormImpl<RecordViewerDialog> {
    private JPanel actionsPanel;
    private JPanel columnsPanel;
    private JPanel mainPanel;
    private JScrollPane columnsPanelScrollPane;
    private JPanel headerPanel;

    private List<RecordViewerColumnForm> columnForms = new ArrayList<RecordViewerColumnForm>();

    private DatasetRecord record;

    public RecordViewerForm(RecordViewerDialog parentComponent, DatasetRecord record) {
        super(parentComponent);
        this.record = record;
        DBDataset dataset = record.getDataset();

        RecordViewInfo recordViewInfo = new RecordViewInfo(dataset.getQualifiedName(), dataset.getIcon());

        // HEADER
        String headerTitle = recordViewInfo.getTitle();
        Icon headerIcon = recordViewInfo.getIcon();
        Color headerBackground = UIUtil.getPanelBackground();
        Project project = getProject();
        if (getEnvironmentSettings(project).getVisibilitySettings().getDialogHeaders().value()) {
            headerBackground = dataset.getEnvironmentType().getColor();
        }
        DBNHeaderForm headerForm = new DBNHeaderForm(
                headerTitle,
                headerIcon,
                headerBackground);
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);

        ActionToolbar actionToolbar = ActionUtil.createActionToolbar(
                "DBNavigator.Place.DataEditor.TextAreaPopup", true,
                new SortAlphabeticallyAction(),
                ActionUtil.SEPARATOR);
        actionsPanel.add(actionToolbar.getComponent(), BorderLayout.WEST);


        columnsPanel.setLayout(new BoxLayout(columnsPanel, BoxLayout.Y_AXIS));

        for (DBColumn column : record.getDataset().getColumns()) {
            RecordViewerColumnForm columnForm = new RecordViewerColumnForm(this, record, column);
            columnForms.add(columnForm);
        }
        ColumnSortingType columnSortingType = DatasetEditorManager.getInstance(project).getRecordViewColumnSortingType();
        sortColumns(columnSortingType);

        int[] metrics = new int[]{0, 0};
        for (RecordViewerColumnForm columnForm : columnForms) {
            metrics = columnForm.getMetrics(metrics);
        }

        for (RecordViewerColumnForm columnForm : columnForms) {
            columnForm.adjustMetrics(metrics);
        }

        Dimension preferredSize = mainPanel.getPreferredSize();
        int width = (int) preferredSize.getWidth() + 24;
        int height = (int) Math.min(preferredSize.getHeight(), 380);
        mainPanel.setPreferredSize(new Dimension(width, height));


        int scrollUnitIncrement = (int) columnForms.get(0).getComponent().getPreferredSize().getHeight();
        columnsPanelScrollPane.getVerticalScrollBar().setUnitIncrement(scrollUnitIncrement);
    }

    public JComponent getPreferredFocusedComponent() {
        return columnForms.get(0).getViewComponent();
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public JComponent getColumnsPanel() {
        return columnsPanel;
    }

    /*********************************************************
     *                   Column sorting                      *
     *********************************************************/
    private void sortColumns(ColumnSortingType sortingType) {
        Comparator<RecordViewerColumnForm> comparator =
                sortingType == ColumnSortingType.ALPHABETICAL ? alphabeticComparator :
                sortingType == ColumnSortingType.BY_INDEX ? indexedComparator : null;

        if (comparator != null) {
            Collections.sort(columnForms, comparator);
            columnsPanel.removeAll();
            for (RecordViewerColumnForm columnForm : columnForms) {
                columnsPanel.add(columnForm.getComponent());
            }
            columnsPanel.revalidate();
            columnsPanel.repaint();
        }
    }

    private static Comparator<RecordViewerColumnForm> alphabeticComparator = new Comparator<RecordViewerColumnForm>() {
        public int compare(RecordViewerColumnForm columnPanel1, RecordViewerColumnForm columnPanel2) {
            String name1 = columnPanel1.getColumn().getName();
            String name2 = columnPanel2.getColumn().getName();
            return name1.compareTo(name2);
        }
    };

    private static Comparator<RecordViewerColumnForm> indexedComparator = new Comparator<RecordViewerColumnForm>() {
        public int compare(RecordViewerColumnForm columnPanel1, RecordViewerColumnForm columnPanel2) {
            int index1 = columnPanel1.getColumn().getPosition();
            int index2 = columnPanel2.getColumn().getPosition();
            return index1-index2;
        }
    };

    public void focusNextColumnPanel(RecordViewerColumnForm source) {
        int index = columnForms.indexOf(source);
        if (index < columnForms.size() - 1) {
            RecordViewerColumnForm columnForm = columnForms.get(index + 1);
            columnForm.getViewComponent().requestFocus();
        }
    }

    public void focusPreviousColumnPanel(RecordViewerColumnForm source) {
        int index = columnForms.indexOf(source);
        if (index > 0) {
            RecordViewerColumnForm columnForm = columnForms.get(index - 1);
            columnForm.getViewComponent().requestFocus();
        }
    }

    /*********************************************************      
     *                       Actions                         *
     *********************************************************/
    private class SortAlphabeticallyAction extends ToggleAction {
        private SortAlphabeticallyAction() {
            super("Sort columns alphabetically", null, Icons.ACTION_SORT_ALPHA);
        }

        public boolean isSelected(AnActionEvent anActionEvent) {
            Project project = record.getDataset().getProject();
            ColumnSortingType columnSortingType = DatasetEditorManager.getInstance(project).getRecordViewColumnSortingType();
            return columnSortingType == ColumnSortingType.ALPHABETICAL;
        }

        public void setSelected(AnActionEvent anActionEvent, boolean selected) {
            Project project = record.getDataset().getProject();
            ColumnSortingType columnSorting = selected ? ColumnSortingType.ALPHABETICAL : ColumnSortingType.BY_INDEX;
            DatasetEditorManager.getInstance(project).setRecordViewColumnSortingType(columnSorting);
            sortColumns(columnSorting);
        }
    }

    public void dispose() {
        super.dispose();
        DisposerUtil.dispose(columnForms);
        record.dispose();
        record = null;
    }
}
