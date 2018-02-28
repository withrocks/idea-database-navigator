package com.dci.intellij.dbn.execution.statement.variables.ui;

import com.dci.intellij.dbn.common.compatibility.CompatibilityUtil;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.thread.WriteActionRunner;
import com.dci.intellij.dbn.common.ui.Borders;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.dci.intellij.dbn.execution.statement.variables.StatementExecutionVariable;
import com.dci.intellij.dbn.execution.statement.variables.StatementExecutionVariablesBundle;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.sql.SQLLanguage;
import com.dci.intellij.dbn.object.DBSchema;
import com.intellij.ide.highlighter.HighlighterFactory;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.colors.ColorKey;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.project.Project;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.GuiUtils;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StatementExecutionVariablesForm extends DBNFormImpl<StatementExecutionVariablesDialog> {
    private List<StatementExecutionVariableValueForm> variableValueForms = new ArrayList<StatementExecutionVariableValueForm>();
    private StatementExecutionProcessor executionProcessor;
    private JPanel mainPanel;
    private JPanel variablesPanel;
    private JPanel previewPanel;
    private JPanel headerSeparatorPanel;
    private Document previewDocument;
    private EditorEx viewer;
    private String statementText;

    public StatementExecutionVariablesForm(StatementExecutionVariablesDialog parentComponent, StatementExecutionProcessor executionProcessor, String statementText) {
        super(parentComponent);
        this.executionProcessor = executionProcessor;
        this.statementText = statementText;

        variablesPanel.setLayout(new BoxLayout(variablesPanel, BoxLayout.Y_AXIS));
        headerSeparatorPanel.setBorder(Borders.BOTTOM_LINE_BORDER);

        List<StatementExecutionVariable> variables = new ArrayList<StatementExecutionVariable>(executionProcessor.getExecutionVariables().getVariables());
        Collections.sort(variables, StatementExecutionVariablesBundle.OFFSET_COMPARATOR);

        for (StatementExecutionVariable variable: variables) {
            StatementExecutionVariableValueForm variableValueForm = new StatementExecutionVariableValueForm(this, variable);
            variableValueForms.add(variableValueForm);
            variablesPanel.add(variableValueForm.getComponent());
            variableValueForm.addDocumentListener(new DocumentAdapter() {
                protected void textChanged(DocumentEvent e) {
                    updatePreview();
                }
            });
            variableValueForm.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    updatePreview();
                }
            });
        }

        int[] metrics = new int[]{0, 0};
        for (StatementExecutionVariableValueForm variableValueForm : variableValueForms) {
            metrics = variableValueForm.getMetrics(metrics);
        }

        for (StatementExecutionVariableValueForm variableValueForm : variableValueForms) {
            variableValueForm.adjustMetrics(metrics);
        }
        updatePreview();
        GuiUtils.replaceJSplitPaneWithIDEASplitter(mainPanel);
    }

    public StatementExecutionProcessor getExecutionProcessor() {
        return executionProcessor;
    }

    public JComponent getComponent() {
        return mainPanel;
    }

    public void dispose() {
        super.dispose();
        for (StatementExecutionVariableValueForm variableValueForm : variableValueForms) {
            variableValueForm.dispose();
        }
        variableValueForms.clear();
        executionProcessor = null;
        EditorFactory.getInstance().releaseEditor(viewer);
    }

    public JComponent getPreferredFocusedComponent() {
        if (variableValueForms.size() > 0) {
            return variableValueForms.get(0).getEditorComponent();
        }
        return null;
    }

    public void saveValues() {
        for (StatementExecutionVariableValueForm variableValueForm : variableValueForms) {
            variableValueForm.saveValue();
        }
    }

    private void updatePreview() {
        ConnectionHandler connectionHandler = FailsafeUtil.get(executionProcessor.getConnectionHandler());
        DBSchema currentSchema = executionProcessor.getCurrentSchema();
        Project project = connectionHandler.getProject();

        StatementExecutionVariablesBundle executionVariables = executionProcessor.getExecutionVariables();
        String previewText = executionVariables.prepareStatementText(connectionHandler, this.statementText, true);

        for (StatementExecutionVariableValueForm variableValueForm : variableValueForms) {
            String errorText = executionVariables.getError(variableValueForm.getVariable());
            if (errorText == null)
                variableValueForm.hideErrorLabel(); else
                variableValueForm.showErrorLabel(errorText);
        }

        if (previewDocument == null) {
            DBLanguageDialect languageDialect = connectionHandler.getLanguageDialect(SQLLanguage.INSTANCE);
            DBLanguagePsiFile selectStatementFile = DBLanguagePsiFile.createFromText(project, "preview", languageDialect, previewText, connectionHandler, currentSchema);
            previewDocument = DocumentUtil.getDocument(selectStatementFile);

            viewer = (EditorEx) EditorFactory.getInstance().createViewer(previewDocument, project);
            viewer.setEmbeddedIntoDialogWrapper(true);
            JScrollPane viewerScrollPane = viewer.getScrollPane();
            SyntaxHighlighter syntaxHighlighter = languageDialect.getSyntaxHighlighter();
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
            previewPanel.add(viewer.getComponent(), BorderLayout.CENTER);

        } else {
            final String finalPreviewText = previewText;

            new WriteActionRunner() {
                public void run() {
                    previewDocument.setText(finalPreviewText);
                }
            }.start();
        }
    }
}
