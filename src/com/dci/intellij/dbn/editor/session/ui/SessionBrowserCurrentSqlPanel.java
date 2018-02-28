package com.dci.intellij.dbn.editor.session.ui;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.compatibility.CompatibilityUtil;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.WriteActionRunner;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.editor.session.SessionBrowser;
import com.dci.intellij.dbn.editor.session.SessionBrowserManager;
import com.dci.intellij.dbn.editor.session.SessionBrowserStatementVirtualFile;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserModelRow;
import com.dci.intellij.dbn.editor.session.ui.table.SessionBrowserTable;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.sql.SQLLanguage;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.vfs.DatabaseFileViewProvider;
import com.intellij.ide.highlighter.HighlighterFactory;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiManager;

public class SessionBrowserCurrentSqlPanel extends DBNFormImpl{
    private JPanel actionsPanel;
    private JPanel viewerPanel;
    private JPanel mainPanel;


    private SessionBrowserStatementVirtualFile virtualFile;
    private DBLanguagePsiFile psiFile;
    private Document document;
    private EditorEx viewer;
    private SessionBrowser sessionBrowser;

    public SessionBrowserCurrentSqlPanel(SessionBrowser sessionBrowser) {
        this.sessionBrowser = sessionBrowser;
        createStatementViewer();

        ActionToolbar actionToolbar = ActionUtil.createActionToolbar("", true, new RefreshAction(), new WrapUnwrapContentAction());
        actionsPanel.add(actionToolbar.getComponent(),BorderLayout.WEST);

    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    public void setPreviewText(final String text) {
        new WriteActionRunner() {
            @Override
            public void run() {
                document.setText(text);
            }
        }.start();
    }

    public void setCurrentSchema(DBSchema currentSchema) {
        virtualFile.setCurrentSchema(currentSchema);
    }

    public void loadCurrentStatement() {
        SessionBrowserTable editorTable = sessionBrowser.getEditorTable();
        if (editorTable != null && editorTable.getSelectedRowCount() == 1) {
            SessionBrowserModelRow selectedRow = editorTable.getModel().getRowAtIndex(editorTable.getSelectedRow());
            final Object sessionId = selectedRow.getSessionId();
            final String schemaName = selectedRow.getSchema();
            final Project project = sessionBrowser.getProject();
            new BackgroundTask(project, "Loading session current SQL", true) {
                @Override
                protected void execute(@NotNull ProgressIndicator progressIndicator) throws InterruptedException {
                    ConnectionHandler connectionHandler = getConnectionHandler();
                    DBSchema schema = null;
                    if (StringUtil.isNotEmpty(schemaName)) {
                        schema = connectionHandler.getObjectBundle().getSchema(schemaName);
                    }

                    SessionBrowserManager sessionBrowserManager = SessionBrowserManager.getInstance(project);
                    String sql = sessionBrowserManager.loadSessionCurrentSql(connectionHandler, sessionId);
                    if (sessionId.equals(sessionBrowser.getSelectedSessionId())) {
                        setCurrentSchema(schema);
                        setPreviewText(sql);
                    }
                }
            }.start();
        } else {
            setPreviewText("");
        }
    }

    @NotNull
    private ConnectionHandler getConnectionHandler() {
        return FailsafeUtil.get(sessionBrowser.getConnectionHandler());
    }

    public DBLanguagePsiFile getPsiFile() {
        return psiFile;
    }

    private void createStatementViewer() {
        Project project = sessionBrowser.getProject();
        ConnectionHandler connectionHandler = getConnectionHandler();
        virtualFile = new SessionBrowserStatementVirtualFile(sessionBrowser, "");
        DatabaseFileViewProvider viewProvider = new DatabaseFileViewProvider(PsiManager.getInstance(project), virtualFile, true);
        psiFile = (DBLanguagePsiFile) virtualFile.initializePsiFile(viewProvider, SQLLanguage.INSTANCE);

        document = DocumentUtil.getDocument(psiFile);


        viewer = (EditorEx) EditorFactory.getInstance().createViewer(document, project);
        viewer.setEmbeddedIntoDialogWrapper(true);
        JScrollPane viewerScrollPane = viewer.getScrollPane();
        SyntaxHighlighter syntaxHighlighter = connectionHandler.getLanguageDialect(SQLLanguage.INSTANCE).getSyntaxHighlighter();
        EditorColorsScheme colorsScheme = viewer.getColorsScheme();
        viewer.setHighlighter(HighlighterFactory.createHighlighter(syntaxHighlighter, colorsScheme));
        //statementViewer.setBackgroundColor(colorsScheme.getColor(ColorKey.find("CARET_ROW_COLOR")));
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
        settings.setUseSoftWraps(true);
        viewer.getComponent().setFocusable(false);

        viewerPanel.add(viewer.getComponent(), BorderLayout.CENTER);
    }


    public class WrapUnwrapContentAction extends ToggleAction {
        public WrapUnwrapContentAction() {
            super("Wrap/Unwrap", "", Icons.ACTION_WRAP_TEXT);
        }

        public boolean isSelected(AnActionEvent e) {
            return viewer != null && viewer.getSettings().isUseSoftWraps();
        }

        public void setSelected(AnActionEvent e, boolean state) {
            viewer.getSettings().setUseSoftWraps(state);
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            super.update(e);
            boolean isWrapped = viewer != null && viewer.getSettings().isUseSoftWraps();
            e.getPresentation().setText(isWrapped ? "Unwrap Content" : "Wrap Content");

        }
    }

    public class RefreshAction extends AnAction {
        public RefreshAction() {
            super("Reload", "", Icons.ACTION_REFRESH);
        }

        @Override
        public void actionPerformed(AnActionEvent anActionEvent) {
            loadCurrentStatement();
        }
    }

    public EditorEx getViewer() {
        return viewer;
    }

    @Override
    public void dispose() {
        if (!isDisposed()) {
            super.dispose();
            EditorFactory.getInstance().releaseEditor(viewer);
            viewer = null;
            virtualFile = null;
            document = null;
        }
    }
}
