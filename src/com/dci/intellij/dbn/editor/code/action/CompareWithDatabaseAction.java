package com.dci.intellij.dbn.editor.code.action;

import java.sql.SQLException;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.editor.code.SourceCodeManager;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;

public class CompareWithDatabaseAction extends AbstractDiffAction {
    public CompareWithDatabaseAction() {
        super("Compare with database", null, Icons.CODE_EDITOR_DIFF_DB);
    }

    public void actionPerformed(@NotNull final AnActionEvent e) {
        final DBSourceCodeVirtualFile sourcecodeFile = getSourcecodeFile(e);
        new ConnectionAction(sourcecodeFile) {
            @Override
            public void execute() {
                final Project project = ActionUtil.getProject(e);
                if (project != null) {
                    new BackgroundTask(project, "Loading database source code", false, true) {
                        @Override
                        protected void execute(@NotNull ProgressIndicator progressIndicator) throws InterruptedException {
                            Editor editor = getEditor(e);
                            if (sourcecodeFile != null && editor != null) {
                                String content = editor.getDocument().getText();
                                sourcecodeFile.setContent(content);
                                DBSchemaObject object = sourcecodeFile.getObject();
                                if (object != null) {
                                    try {
                                        SourceCodeManager sourceCodeManager = SourceCodeManager.getInstance(project);
                                        String referenceText = sourceCodeManager.loadSourceCodeFromDatabase(object, sourcecodeFile.getContentType());
                                        if (!progressIndicator.isCanceled()) {
                                            openDiffWindow(e, referenceText, "Database version", "Local version vs. database version");
                                        }

                                    } catch (SQLException e1) {
                                        MessageUtil.showErrorDialog(
                                                project, "Could not load sourcecode for " +
                                                        object.getQualifiedNameWithType() + " from database.", e1);
                                    }
                                }
                            }
                        }
                    }.start();
                }
            }
        }.start();
    }

    public void update(@NotNull AnActionEvent e) {
        Editor editor = getEditor(e);
        e.getPresentation().setText("Compare with Database");
        e.getPresentation().setEnabled(editor != null);
    }
}
