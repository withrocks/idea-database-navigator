package com.dci.intellij.dbn.ddl.action;

import com.dci.intellij.dbn.ddl.DDLFileAttachmentManager;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

public class AttachDDLFileAction extends AnAction {
    private DBSchemaObject object;
    public AttachDDLFileAction(DBSchemaObject object) {
        super("Attach files");
        this.object = object;
    }

    public void actionPerformed(AnActionEvent e) {
        Project project = object.getProject();
        DDLFileAttachmentManager fileAttachmentManager = DDLFileAttachmentManager.getInstance(project);
        fileAttachmentManager.bindDDLFiles(object);
    }
}
