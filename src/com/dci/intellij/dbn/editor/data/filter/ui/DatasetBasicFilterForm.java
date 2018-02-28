package com.dci.intellij.dbn.editor.data.filter.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.compatibility.CompatibilityUtil;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.thread.WriteActionRunner;
import com.dci.intellij.dbn.common.ui.Borders;
import com.dci.intellij.dbn.common.ui.ValueSelector;
import com.dci.intellij.dbn.common.ui.ValueSelectorListener;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.editor.data.filter.ConditionOperator;
import com.dci.intellij.dbn.editor.data.filter.DatasetBasicFilter;
import com.dci.intellij.dbn.editor.data.filter.DatasetBasicFilterCondition;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilterVirtualFile;
import com.dci.intellij.dbn.language.sql.SQLLanguage;
import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBDataset;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.vfs.DatabaseFileViewProvider;
import com.intellij.ide.highlighter.HighlighterFactory;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.colors.ColorKey;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.PlatformIcons;
import com.intellij.util.ui.UIUtil;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DatasetBasicFilterForm extends ConfigurationEditorForm<DatasetBasicFilter> {
    private JRadioButton joinAndRadioButton;
    private JRadioButton joinOrRadioButton;
    private JPanel conditionsPanel;
    private JPanel mainPanel;
    private JPanel actionsPanel;
    private JTextField nameTextField;
    private JLabel errorLabel;
    private JPanel previewPanel;
    private JPanel addConditionsPanel;
    private JPanel filterNamePanel;

    private DBObjectRef<DBDataset> datasetRef;
    private List<DatasetBasicFilterConditionForm> conditionForms = new ArrayList<DatasetBasicFilterConditionForm>();
    private Document previewDocument;
    private boolean isCustomNamed;
    private EditorEx viewer;


    public DatasetBasicFilterForm(DBDataset dataset, DatasetBasicFilter filter) {
        super(filter);
        conditionsPanel.setLayout(new BoxLayout(conditionsPanel, BoxLayout.Y_AXIS));
        datasetRef = DBObjectRef.from(dataset);
        nameTextField.setText(filter.getDisplayName());

        actionsPanel.add(new ColumnSelector(), BorderLayout.CENTER);
        addConditionsPanel.setBorder(Borders.BOTTOM_LINE_BORDER);
        filterNamePanel.setBorder(Borders.BOTTOM_LINE_BORDER);

        for (DatasetBasicFilterCondition condition : filter.getConditions()) {
            addConditionPanel(condition);
        }

        joinAndRadioButton.setSelected(filter.getJoinType() == DatasetBasicFilter.JOIN_TYPE_AND);
        joinOrRadioButton.setSelected(filter.getJoinType() == DatasetBasicFilter.JOIN_TYPE_OR);

        nameTextField.addKeyListener(createKeyListener());
        registerComponent(mainPanel);

        if (filter.getError() == null) {
            errorLabel.setText("");
        } else {
            errorLabel.setText(filter.getError());
            errorLabel.setIcon(Icons.EXEC_MESSAGES_ERROR);
        }
        updateNameAndPreview();
        isCustomNamed = filter.isCustomNamed();
    }

    private class ColumnSelector extends ValueSelector<DBColumn> {
        public ColumnSelector() {
            super(PlatformIcons.ADD_ICON, "Add Condition", null, false);
            addListener(new ValueSelectorListener<DBColumn>() {
                @Override
                public void valueSelected(DBColumn column) {
                    addConditionPanel(column);
                }
            });
        }

        @Override
        public List<DBColumn> loadValues() {
            DBDataset dataset = getDataset();
            List<DBColumn> columns = new ArrayList<DBColumn>(dataset.getColumns());
            Collections.sort(columns);
            return columns;
        }
    }

    public void focus() {
        if (conditionForms.size() > 0) {
            conditionForms.get(0).focus();
        }
    }

    protected ActionListener createActionListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateNameAndPreview();
            }
        };
    }

    private KeyListener createKeyListener() {
        return new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                isCustomNamed = true;
                nameTextField.setForeground(UIUtil.getTextFieldForeground());
            }
        };
    }


    public void updateGeneratedName() {
        if (!isDisposed() && (!isCustomNamed || nameTextField.getText().trim().length() == 0)) {
            getConfiguration().setCustomNamed(false);
            boolean addSeparator = false;
            StringBuilder buffer = new StringBuilder();
            for (DatasetBasicFilterConditionForm conditionForm : conditionForms) {
                if (conditionForm.isActive()) {
                    if (addSeparator) buffer.append(joinAndRadioButton.isSelected() ? " & " : " | ");
                    addSeparator = true;
                    buffer.append(conditionForm.getValue());
                    if (buffer.length() > 40) {
                        buffer.setLength(40);
                        buffer.append("...");
                        break;
                    }
                }
            }

            String name = buffer.length() > 0 ? buffer.toString() : getConfiguration().getFilterGroup().createFilterName("Filter");
            nameTextField.setText(name);
            nameTextField.setForeground(Color.GRAY);
        }
    }

    public void updateNameAndPreview() {
        DBDataset dataset = this.getDataset();
        if (dataset != null) {
            updateGeneratedName();
            final StringBuilder selectStatement = new StringBuilder("select * from ");
            selectStatement.append(dataset.getSchema().getQuotedName(false)).append('.');
            selectStatement.append(dataset.getQuotedName(false));
            selectStatement.append(" where\n    ");

            boolean addJoin = false;
            for (DatasetBasicFilterConditionForm conditionForm : conditionForms) {
                DatasetBasicFilterCondition condition = conditionForm.getCondition();
                if (conditionForm.isActive()) {
                    if (addJoin) {
                        selectStatement.append(joinAndRadioButton.isSelected() ? " and\n    " : " or\n    ");
                    }
                    addJoin = true;
                    condition.appendConditionString(selectStatement, dataset);
                }
            }

            if (previewDocument == null) {
                Project project = dataset.getProject();
                DatasetFilterVirtualFile filterFile = new DatasetFilterVirtualFile(dataset, selectStatement.toString());
                DatabaseFileViewProvider viewProvider = new DatabaseFileViewProvider(PsiManager.getInstance(project), filterFile, true);
                PsiFile selectStatementFile = filterFile.initializePsiFile(viewProvider, SQLLanguage.INSTANCE);

                previewDocument = DocumentUtil.getDocument(selectStatementFile);

                viewer = (EditorEx) EditorFactory.getInstance().createViewer(previewDocument, project);
                viewer.setEmbeddedIntoDialogWrapper(true);
                JScrollPane viewerScrollPane = viewer.getScrollPane();
                SyntaxHighlighter syntaxHighlighter = dataset.getLanguageDialect(SQLLanguage.INSTANCE).getSyntaxHighlighter();
                EditorColorsScheme colorsScheme = viewer.getColorsScheme();
                viewer.setHighlighter(HighlighterFactory.createHighlighter(syntaxHighlighter, colorsScheme));
                viewer.setBackgroundColor(colorsScheme.getColor(ColorKey.find("CARET_ROW_COLOR")));
                viewerScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                viewerScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                //viewerScrollPane.setBorder(null);
                viewerScrollPane.setViewportBorder(new LineBorder(CompatibilityUtil.getEditorBackgroundColor(viewer), 4, false));

                EditorSettings settings = viewer.getSettings();
                settings.setFoldingOutlineShown(false);
                settings.setLineMarkerAreaShown(false);
                settings.setLineNumbersShown(false);
                settings.setVirtualSpace(false);
                settings.setDndEnabled(false);
                settings.setAdditionalLinesCount(2);
                settings.setRightMarginShown(false);
                viewer.getComponent().setFocusable(false);
                previewPanel.add(viewer.getComponent(), BorderLayout.CENTER);

            } else {
                new WriteActionRunner() {
                    public void run() {
                        previewDocument.setText(selectStatement);
                    }
                }.start();
            }
        }

    }

    public String getFilterName() {
        return nameTextField.getText();
    }

    public DBDataset getDataset() {
        return datasetRef.get();
    }

    public void addConditionPanel(DatasetBasicFilterCondition condition) {
        condition.createComponent();
        DatasetBasicFilterConditionForm conditionForm = condition.getSettingsEditor();
        if (conditionForm != null) {
            conditionForm.setBasicFilterPanel(this);
            conditionForms.add(conditionForm);
            conditionsPanel.add(conditionForm.getComponent());

            conditionsPanel.revalidate();
            conditionsPanel.repaint();

            conditionForm.focus();
        }
    }

    public void addConditionPanel(DBColumn column) {
        DatasetBasicFilter filter = getConfiguration();
        DatasetBasicFilterCondition condition = new DatasetBasicFilterCondition(filter);
        condition.setColumnName(column == null ? null : column.getName());
        condition.setOperator(ConditionOperator.EQUAL.getName());
        addConditionPanel(condition);
        updateNameAndPreview();
    }

    public void removeConditionPanel(DatasetBasicFilterConditionForm conditionForm) {
        conditionForm.setBasicFilterPanel(null);
        conditionForms.remove(conditionForm);
        conditionsPanel.remove(conditionForm.getComponent());
        conditionsPanel.revalidate();
        conditionsPanel.repaint();
        updateNameAndPreview();
    }


    /*************************************************
     *                  SettingsEditor               *
     *************************************************/
    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyFormChanges() throws ConfigurationException {
        updateGeneratedName();
        DatasetBasicFilter filter = getConfiguration();
        filter.setJoinType(joinAndRadioButton.isSelected() ?
                DatasetBasicFilter.JOIN_TYPE_AND :
                DatasetBasicFilter.JOIN_TYPE_OR);
        filter.setCustomNamed(isCustomNamed);
        filter.getConditions().clear();
        for (DatasetBasicFilterConditionForm conditionForm : conditionForms) {
            conditionForm.applyFormChanges();
            filter.addCondition(conditionForm.getConfiguration());
        }
        filter.setName(nameTextField.getText());
    }

    public void resetFormChanges() {

    }

    @Override
    public void dispose() {
        super.dispose();
        EditorFactory.getInstance().releaseEditor(viewer);
        viewer = null;
        previewDocument = null;
        for (DatasetBasicFilterConditionForm conditionForm : conditionForms) {
            conditionForm.dispose();
        }
        conditionForms.clear();
    }
}
