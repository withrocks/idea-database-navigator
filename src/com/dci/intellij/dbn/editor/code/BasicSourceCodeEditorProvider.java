package com.dci.intellij.dbn.editor.code;

import javax.swing.Icon;
import javax.swing.JComponent;
import java.awt.BorderLayout;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.editor.BasicTextEditor;
import com.dci.intellij.dbn.common.editor.BasicTextEditorProvider;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.vfs.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;

public abstract class BasicSourceCodeEditorProvider extends BasicTextEditorProvider implements DumbAware {
    @NotNull
    public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        DBEditableObjectVirtualFile databaseFile;

        if (file instanceof DBSourceCodeVirtualFile) {
            DBSourceCodeVirtualFile sourceCodeFile = (DBSourceCodeVirtualFile) file;
            databaseFile = sourceCodeFile.getMainDatabaseFile();
        } else {
            databaseFile = (DBEditableObjectVirtualFile) file;
        }

        DBSourceCodeVirtualFile sourceCodeFile = getSourceCodeFile(databaseFile);

        boolean isMainEditor = sourceCodeFile.getContentType() == databaseFile.getMainContentType();

/*
        BasicTextEditor openEditor = lookupExistingEditor(project, databaseFile);
        if (openEditor != null) return openEditor;
*/

        String editorName = getName();
        BasicTextEditor textEditor = isMainEditor ?
                new SourceCodeMainEditor(project, sourceCodeFile, editorName, getEditorProviderId()) :
                new SourceCodeEditor(project, sourceCodeFile, editorName, getEditorProviderId());

        updateEditorActions(textEditor);
        Document document = textEditor.getEditor().getDocument();

        int documentTracking = document.hashCode();
        if (document.hashCode() != sourceCodeFile.getDocumentHashCode()) {
            document.addDocumentListener(sourceCodeFile);
            sourceCodeFile.setDocumentHashCode(documentTracking);
        }

        Icon icon = getIcon();
        if (icon != null) {
            updateTabIcon(databaseFile, textEditor, icon);
        }
        return textEditor;
    }

    @Override
    public VirtualFile getContentVirtualFile(VirtualFile virtualFile) {
        if (virtualFile instanceof DBEditableObjectVirtualFile) {
            DBEditableObjectVirtualFile objectVirtualFile = (DBEditableObjectVirtualFile) virtualFile;
            return objectVirtualFile.getContentFile(getContentType());
        }
        return super.getContentVirtualFile(virtualFile);
    }

    private BasicTextEditor lookupExistingEditor(Project project, DBEditableObjectVirtualFile databaseFile) {
        FileEditor[] fileEditors = FileEditorManager.getInstance(project).getEditors(databaseFile);
        if (fileEditors.length > 0) {
            for (FileEditor fileEditor : fileEditors) {
                if (fileEditor instanceof SourceCodeEditor) {
                    SourceCodeEditor sourceCodeEditor = (SourceCodeEditor) fileEditor;
                    if (sourceCodeEditor.getVirtualFile().getContentType() == getContentType()) {
                        return sourceCodeEditor;
                    }
                }
            }
        }
        return null;
    }

    private DBSourceCodeVirtualFile getSourceCodeFile(DBEditableObjectVirtualFile databaseFile) {
        return (DBSourceCodeVirtualFile) databaseFile.getContentFile(getContentType());
    }

    public abstract DBContentType getContentType();

    public abstract String getName();

    public abstract Icon getIcon();

    private static void updateEditorActions(BasicTextEditor fileEditor) {
        Editor editor = fileEditor.getEditor();
        ActionToolbar actionToolbar = ActionUtil.createActionToolbar("", true, "DBNavigator.ActionGroup.SourceEditor");
        JComponent editorComponent = editor.getComponent();
        actionToolbar.setTargetComponent(editorComponent);
        //FileEditorManager.getInstance(editor.getProject()).addTopComponent(fileEditor, actionToolbar.getComponent());
        editorComponent.getParent().add(actionToolbar.getComponent(), BorderLayout.NORTH);
    }

    public void disposeEditor(@NotNull FileEditor editor) {
        Disposer.dispose(editor);
    }
}
