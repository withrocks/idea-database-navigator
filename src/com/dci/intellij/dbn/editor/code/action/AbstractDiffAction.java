package com.dci.intellij.dbn.editor.code.action;

import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.editor.code.diff.DBSourceFileContent;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diff.DiffManager;
import com.intellij.openapi.diff.SimpleContent;
import com.intellij.openapi.diff.SimpleDiffRequest;
import com.intellij.openapi.project.Project;

public abstract class AbstractDiffAction extends AbstractSourceCodeEditorAction {
    public AbstractDiffAction(String text, String description, javax.swing.Icon icon) {
        super(text, description, icon);
    }

    protected void openDiffWindow(AnActionEvent e, final String referenceText, final String referenceTitle, final String windowTitle) {
        final DBSourceCodeVirtualFile virtualFile = getSourcecodeFile(e);
        final Project project = ActionUtil.getProject(e);
        if (project!= null && virtualFile != null) {
            new SimpleLaterInvocator() {
                public void execute() {
                    SimpleContent originalContent = new SimpleContent(referenceText, virtualFile.getFileType());
                    DBSourceFileContent changedContent = new DBSourceFileContent(project, virtualFile);

                    DBSchemaObject object = virtualFile.getObject();
                    if (object != null) {
                        String title =
                                object.getSchema().getName() + "." +
                                        object.getName() + " " +
                                        object.getTypeName() + " - " + windowTitle;
                        SimpleDiffRequest diffRequest = new SimpleDiffRequest(project, title);
                        diffRequest.setContents(originalContent, changedContent);
                        diffRequest.setContentTitles(referenceTitle + " ", "Your version ");

                        DiffManager.getInstance().getIdeaDiffTool().show(diffRequest);
                    }
                }
            }.start();
        }
    }
}

