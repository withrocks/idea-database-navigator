package com.dci.intellij.dbn.execution.compiler;

import com.dci.intellij.dbn.common.editor.BasicTextEditor;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.EditorProviderId;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

public class CompilerAction {
    private CompilerActionSource source;
    private DBContentType contentType;
    private WeakReference<VirtualFile> virtualFileRef;
    private WeakReference<FileEditor> fileEditorRef;
    private EditorProviderId editorProviderId;
    private int startOffset;

    public CompilerAction(CompilerActionSource source, DBContentType contentType) {
        this.source = source;
        this.contentType = contentType;
    }

    public CompilerAction(CompilerActionSource source, DBContentType contentType, VirtualFile virtualFile, FileEditor fileEditor) {
        this.source = source;
        this.contentType = contentType;
        this.virtualFileRef = new WeakReference<VirtualFile>(virtualFile);
        this.fileEditorRef = new WeakReference<FileEditor>(fileEditor);
        initEditorProviderId(fileEditor);
    }

    private void initEditorProviderId(FileEditor fileEditor) {
        if (fileEditor instanceof BasicTextEditor) {
            BasicTextEditor basicTextEditor = (BasicTextEditor) fileEditor;
            editorProviderId = basicTextEditor.getEditorProviderId();
        }
    }

    @Nullable
    public EditorProviderId getEditorProviderId() {
        return editorProviderId;
    }

    public DBContentType getContentType() {
        return contentType;
    }

    public void setStartOffset(int startOffset) {
        this.startOffset = startOffset;
    }

    public CompilerActionSource getSource() {
        return source;
    }

    public boolean isDDL() {
        return source == CompilerActionSource.DDL;
    }

    public boolean isSave() {
        return source == CompilerActionSource.SAVE;
    }

    public boolean isCompile() {
        return source == CompilerActionSource.COMPILE;
    }

    public boolean isBulkCompile() {
        return source == CompilerActionSource.BULK_COMPILE;
    }

    @Nullable
    public VirtualFile getVirtualFile() {
        return virtualFileRef == null ? null : virtualFileRef.get();
    }

    @Nullable
    public FileEditor getFileEditor() {
        FileEditor fileEditor = this.fileEditorRef == null ? null : this.fileEditorRef.get();
        if (fileEditor != null) {
            Editor editor = EditorUtil.getEditor(fileEditor);
            if (editor != null && editor.isDisposed()) {
                this.fileEditorRef = null;
            }
        }
        return fileEditor;
    }

    public int getStartOffset() {
        return startOffset;
    }
}
