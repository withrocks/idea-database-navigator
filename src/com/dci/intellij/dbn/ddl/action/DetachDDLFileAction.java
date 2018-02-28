package com.dci.intellij.dbn.ddl.action;

import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.ddl.DDLFileAttachmentManager;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;

public class DetachDDLFileAction extends AnAction {
    private DBSchemaObject object;
    public DetachDDLFileAction(DBSchemaObject object) {
        super("Detach files");
        this.object = object;
    }

    public void actionPerformed(AnActionEvent e) {
        Project project = object.getProject();
        DDLFileAttachmentManager fileAttachmentManager = DDLFileAttachmentManager.getInstance(project);
        fileAttachmentManager.detachDDLFiles(object);
    }

    public void update(AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        if (project != null) {
            DDLFileAttachmentManager fileAttachmentManager = DDLFileAttachmentManager.getInstance(project);
            boolean hasAttachedDDLFiles = fileAttachmentManager.hasAttachedDDLFiles(object);
            Presentation presentation = e.getPresentation();
            presentation.setEnabled(hasAttachedDDLFiles);
        }
    }
}