package com.dci.intellij.dbn.editor.code.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.action.GroupPopupAction;
import com.dci.intellij.dbn.ddl.action.AttachDDLFileAction;
import com.dci.intellij.dbn.ddl.action.CreateDDLFileAction;
import com.dci.intellij.dbn.ddl.action.DetachDDLFileAction;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.vfs.VirtualFile;

public class DDLFileAction extends GroupPopupAction {
    public DDLFileAction() {
        super("DDL File", "DDL File", Icons.CODE_EDITOR_DDL_FILE);
    }

    public void update(@NotNull AnActionEvent e) {
        DBSourceCodeVirtualFile sourcecodeFile = getSourcecodeFile(e);
        Presentation presentation = e.getPresentation();
        presentation.setIcon(Icons.CODE_EDITOR_DDL_FILE);
        presentation.setText("DDL Files");
        presentation.setEnabled(sourcecodeFile != null);
    }

    @Override
    protected AnAction[] getActions(AnActionEvent e) {
        DBSourceCodeVirtualFile sourcecodeFile = getSourcecodeFile(e);
        if (sourcecodeFile != null) {
            DBSchemaObject object = sourcecodeFile.getObject();
            if (object != null) {
                return new AnAction[]{
                        new CreateDDLFileAction(object),
                        new AttachDDLFileAction(object),
                        new DetachDDLFileAction(object)
                };
            }
        }
        return new AnAction[0];
    }

    protected static DBSourceCodeVirtualFile getSourcecodeFile(AnActionEvent e) {
        VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        return virtualFile instanceof DBSourceCodeVirtualFile ? (DBSourceCodeVirtualFile) virtualFile : null;
    }

}
