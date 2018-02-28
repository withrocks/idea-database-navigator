package com.dci.intellij.dbn.generator.action;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.CommandWriteActionRunner;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionProvider;
import com.dci.intellij.dbn.generator.StatementGeneratorResult;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.dci.intellij.dbn.language.sql.SQLFileType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;

public abstract class GenerateStatementAction extends AnAction implements ConnectionProvider {
    public GenerateStatementAction(String text) {
        super(text);
    }

    public final void actionPerformed(AnActionEvent e) {
        final Project project = ActionUtil.getProject(e);
        if (project != null) {
            new ConnectionAction(this) {
                @Override
                public void execute() {
                    new BackgroundTask(project, "Extracting select statement", false, true) {
                        protected void execute(@NotNull ProgressIndicator progressIndicator) {
                            StatementGeneratorResult result = generateStatement(project);
                            if (result.getMessages().hasErrors()) {
                                MessageUtil.showErrorDialog(project, "Error generating statement", result.getMessages());
                            } else {
                                pasteStatement(result, project);
                            }
                        }
                    }.start();
                }
            }.start();
        }
    }

    private void pasteStatement(final StatementGeneratorResult result, final Project project) {
        new SimpleLaterInvocator() {
            @Override
            public void execute() {
                Editor editor = EditorUtil.getSelectedEditor(project, SQLFileType.INSTANCE);
                if (editor != null)
                    pasteToEditor(editor, result); else
                    pasteToClipboard(result, project);
            }
        }.start();
    }

    private static void pasteToClipboard(StatementGeneratorResult result, Project project) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(result.getStatement()), null);
        MessageUtil.showInfoDialog(project, "Statement extracted", "SQL statement exported to clipboard.");
    }

    private static void pasteToEditor(final Editor editor, final StatementGeneratorResult generatorResult) {
        new CommandWriteActionRunner(editor.getProject(), "Extract statement") {
            @Override
            public void run() {
                String statement = generatorResult.getStatement();
                PsiUtil.moveCaretOutsideExecutable(editor);
                int offset = EditorModificationUtil.insertStringAtCaret(editor, statement + "\n\n", false, true);
                offset = offset - statement.length() - 2;
                /*editor.getMarkupModel().addRangeHighlighter(offset, offset + statement.length(),
                        HighlighterLayer.SELECTION,
                        EditorColorsManager.getInstance().getGlobalScheme().getAttributes(EditorColors.SEARCH_RESULT_ATTRIBUTES),
                        HighlighterTargetArea.EXACT_RANGE);*/
                editor.getSelectionModel().setSelection(offset, offset + statement.length());
                editor.getCaretModel().moveToOffset(offset);
            }
        }.start();
    }

    protected abstract StatementGeneratorResult generateStatement(Project project);
}
