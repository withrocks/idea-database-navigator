package com.dci.intellij.dbn.editor.data.filter.ui;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.compatibility.CompatibilityUtil;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.editor.data.filter.DatasetCustomFilter;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilterVirtualFile;
import com.dci.intellij.dbn.language.sql.SQLLanguage;
import com.dci.intellij.dbn.object.DBDataset;
import com.dci.intellij.dbn.vfs.DatabaseFileViewProvider;
import com.intellij.ide.highlighter.HighlighterFactory;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;

public class DatasetCustomFilterForm extends ConfigurationEditorForm<DatasetCustomFilter> {
    private JPanel mainPanel;
    private JPanel actionsPanel;
    private JPanel editorPanel;
    private JTextField nameTextField;
    private JLabel errorLabel;

    private Document document;
    private EditorEx editor;
    private int conditionStartOffset;
    private static final String COMMENT = "-- enter your custom conditions here";

    public DatasetCustomFilterForm(DBDataset dataset, DatasetCustomFilter filter) {
        super(filter);
        nameTextField.setText(filter.getDisplayName());
        Project project = dataset.getProject();

        StringBuilder selectStatement = new StringBuilder("select * from ");
        selectStatement.append(dataset.getSchema().getQuotedName(false)).append('.');
        selectStatement.append(dataset.getQuotedName(false));
        selectStatement.append(" where \n");
        conditionStartOffset = selectStatement.length();

        String condition = filter.getCondition();
        boolean isValidCondition = StringUtil.isNotEmptyOrSpaces(condition);
        selectStatement.append(isValidCondition ? condition : COMMENT);

        DatasetFilterVirtualFile filterFile = new DatasetFilterVirtualFile(dataset, selectStatement.toString());
        DatabaseFileViewProvider viewProvider = new DatabaseFileViewProvider(PsiManager.getInstance(project), filterFile, true);
        PsiFile selectStatementFile = filterFile.initializePsiFile(viewProvider, SQLLanguage.INSTANCE);

        document = DocumentUtil.getDocument(selectStatementFile);
        document.createGuardedBlock(0, conditionStartOffset);
        editor = (EditorEx) EditorFactory.getInstance().createEditor(document, project);
        editor.setEmbeddedIntoDialogWrapper(true);

        SyntaxHighlighter syntaxHighlighter = dataset.getLanguageDialect(SQLLanguage.INSTANCE).getSyntaxHighlighter();
        editor.setHighlighter(HighlighterFactory.createHighlighter(syntaxHighlighter, editor.getColorsScheme()));
        editor.getCaretModel().moveToOffset(conditionStartOffset);
        if (!isValidCondition) editor.getSelectionModel().setSelection(conditionStartOffset, document.getTextLength());

        JScrollPane editorScrollPane = editor.getScrollPane();
        editorScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        editorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        editorScrollPane.setViewportBorder(new LineBorder(CompatibilityUtil.getEditorBackgroundColor(editor), 3));

        //viewer.setBackgroundColor(viewer.getColorsScheme().getColor(ColorKey.find("CARET_ROW_COLOR")));
        //viewer.getScrollPane().setViewportBorder(new LineBorder(viewer.getBackroundColor(), 4, false));
        //editor.getScrollPane().setBorder(null);

        EditorSettings settings = editor.getSettings();
        settings.setFoldingOutlineShown(false);
        settings.setLineMarkerAreaShown(false);
        settings.setLineNumbersShown(false);
        settings.setVirtualSpace(false);
        settings.setDndEnabled(false);
        settings.setAdditionalLinesCount(2);
        settings.setRightMarginShown(false);
        settings.setUseTabCharacter(true);

        editorPanel.add(editor.getComponent(), BorderLayout.CENTER);
        if (filter.getError() == null) {
            errorLabel.setText("");
        } else {
            errorLabel.setText(filter.getError());
            errorLabel.setIcon(Icons.EXEC_MESSAGES_ERROR);
        }
    }

    public void focus() {
        editor.getContentComponent().requestFocus();
    }

    public String getFilterName() {
        return nameTextField.getText();
    }

   /*************************************************
    *                  SettingsEditor               *
    *************************************************/
    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyFormChanges() throws ConfigurationException {
        DatasetCustomFilter filter = getConfiguration();
        String condition = document.getText().substring(conditionStartOffset);
        if (condition.equals(COMMENT))
            filter.setCondition(""); else
            filter.setCondition(condition);
        filter.setName(nameTextField.getText());
    }

    public void resetFormChanges() {

    }

    public void dispose() {
        EditorFactory.getInstance().releaseEditor(editor);
        editor = null;
        document = null;
        super.dispose();
    }
}
