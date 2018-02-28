package com.dci.intellij.dbn.language.editor.action;

import java.io.IOException;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Constants;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.vfs.DBConsoleVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileChooser.FileSaverDialog;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileWrapper;

public class SaveToFileEditorAction extends DumbAwareAction {
    public SaveToFileEditorAction() {
        super("Save to file", "Save console to file", Icons.CODE_EDITOR_SAVE_TO_FILE);
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (project != null && virtualFile instanceof DBConsoleVirtualFile) {
            DBConsoleVirtualFile consoleVirtualFile = (DBConsoleVirtualFile) virtualFile;

            FileSaverDescriptor fileSaverDescriptor = new FileSaverDescriptor(
                    Constants.DBN_TITLE_PREFIX + "Save Console to File",
                    "Save content of the console \"" + consoleVirtualFile.getName() + "\" to file", "sql");

            FileSaverDialog fileSaverDialog = FileChooserFactory.getInstance().createSaveFileDialog(fileSaverDescriptor, project);
            VirtualFileWrapper virtualFileWrapper = fileSaverDialog.save(null, consoleVirtualFile.getName());
            if (virtualFileWrapper != null) {
                Document document = DocumentUtil.getDocument(virtualFile);
                try {
                    VirtualFile newVirtualFile = virtualFileWrapper.getVirtualFile(true);
                    if (newVirtualFile != null) {
                        newVirtualFile.setBinaryContent(document.getCharsSequence().toString().getBytes());
                        FileConnectionMappingManager fileConnectionMappingManager = FileConnectionMappingManager.getInstance(project);
                        fileConnectionMappingManager.setActiveConnection(newVirtualFile, consoleVirtualFile.getConnectionHandler());
                        fileConnectionMappingManager.setCurrentSchema(newVirtualFile, consoleVirtualFile.getCurrentSchema());

                        FileEditorManager.getInstance(project).openFile(newVirtualFile, true);
                    }
                } catch (IOException e1) {
                    MessageUtil.showErrorDialog(project, "Error Saving To File", "Could not save console content to file \"" + virtualFileWrapper.getFile().getName() + "\"", e1);
                }
            }
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        Presentation presentation = e.getPresentation();
        presentation.setText("Save to File");
        VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        presentation.setVisible(virtualFile instanceof DBConsoleVirtualFile);
        presentation.setEnabled(true);
    }


}