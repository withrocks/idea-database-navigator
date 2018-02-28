package com.dci.intellij.dbn.data.editor.text.ui;

import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.data.editor.text.TextContentType;
import com.dci.intellij.dbn.data.editor.text.TextEditorAdapter;
import com.dci.intellij.dbn.data.editor.text.actions.TextContentTypeComboBoxAction;
import com.dci.intellij.dbn.data.editor.ui.UserValueHolder;
import com.dci.intellij.dbn.data.value.LargeObjectValue;
import com.intellij.ide.highlighter.HighlighterFactory;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.sql.SQLException;

public class TextEditorForm extends DBNFormImpl<TextEditorDialog> {
    private JPanel mainPanel;
    private JPanel editorPanel;
    private JPanel actionsPanel;

    private EditorEx editor;
    private UserValueHolder userValueHolder;
    private String error;

    private TextEditorAdapter textEditorAdapter;


    public JComponent getComponent() {
        return mainPanel;
    }

    public TextEditorForm(TextEditorDialog parent, DocumentListener documentListener, UserValueHolder userValueHolder, TextEditorAdapter textEditorAdapter) throws SQLException {
        super(parent);
        this.userValueHolder = userValueHolder;
        this.textEditorAdapter = textEditorAdapter;

        Project project = getProject();
        if (userValueHolder.getContentType() == null) {
            userValueHolder.setContentType(TextContentType.getPlainText(project));
        }

        ActionToolbar actionToolbar = ActionUtil.createActionToolbar(
                "DBNavigator.Place.DataEditor.LobContentTypeEditor", true,
                new TextContentTypeComboBoxAction(this));
        actionsPanel.add(actionToolbar.getComponent(), BorderLayout.WEST);

        String text = readUserValue();
        Document document = EditorFactory.getInstance().createDocument(text == null ? "" : StringUtil.removeCharacter(text, '\r'));
        document.addDocumentListener(documentListener);

        editor = (EditorEx) EditorFactory.getInstance().createEditor(document, project, userValueHolder.getContentType().getFileType(), false);
        editor.setEmbeddedIntoDialogWrapper(true);
        editor.getContentComponent().setFocusTraversalKeysEnabled(false);

        editorPanel.add(editor.getComponent(), BorderLayout.CENTER);
    }

    @Nullable
    public String readUserValue() throws SQLException {
        Object userValue = userValueHolder.getUserValue();
        if (userValue instanceof String) {
            return (String) userValue;
        } else if (userValue instanceof LargeObjectValue) {
            LargeObjectValue largeObjectValue = (LargeObjectValue) userValue;
            return largeObjectValue.read();
        }
        return null;
    }

    public void writeUserValue() throws SQLException {
        String text = editor.getDocument().getText();
        userValueHolder.updateUserValue(text, false);
        textEditorAdapter.afterUpdate();
    }

    public TextContentType getContentType() {
        return userValueHolder.getContentType();
    }

    public void setContentType(TextContentType contentType) {
        SyntaxHighlighter syntaxHighlighter = SyntaxHighlighterFactory.getSyntaxHighlighter(contentType.getFileType(), userValueHolder.getProject(), null);
        EditorColorsScheme colorsScheme = editor.getColorsScheme();
        editor.setHighlighter(HighlighterFactory.createHighlighter(syntaxHighlighter, colorsScheme));
        userValueHolder.setContentType(contentType);
    }

    public void dispose() {
        super.dispose();
        EditorFactory.getInstance().releaseEditor(editor);
        //editor = null;

    }

    public JComponent getEditorComponent() {
        return editor.getContentComponent();
    }
}
