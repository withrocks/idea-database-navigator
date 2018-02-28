package com.dci.intellij.dbn.common.editor;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.thread.ReadActionRunner;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.intellij.codeInsight.folding.CodeFoldingManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.impl.text.CodeFoldingState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;

public class BasicTextEditorState implements FileEditorState {
    private int line;
    private int column;
    private int selectionStart;
    private int selectionEnd;
    private float verticalScrollProportion;
    private CodeFoldingState foldingState;

    public boolean canBeMergedWith(FileEditorState fileEditorState, FileEditorStateLevel fileEditorStateLevel) {
        return fileEditorState instanceof BasicTextEditorState;
    }

    public CodeFoldingState getFoldingState() {
        return foldingState;
    }

    public void setFoldingState(CodeFoldingState foldingState) {
        this.foldingState = foldingState;
    }

    public void readState(@NotNull Element sourceElement, final Project project, final VirtualFile virtualFile) {
        line = Integer.parseInt(sourceElement.getAttributeValue("line"));
        column = Integer.parseInt(sourceElement.getAttributeValue("column"));
        selectionStart = Integer.parseInt(sourceElement.getAttributeValue("selection-start"));
        selectionEnd = Integer.parseInt(sourceElement.getAttributeValue("selection-end"));
        verticalScrollProportion = Float.parseFloat(sourceElement.getAttributeValue("vertical-scroll-proportion"));

        final Element foldingElement = sourceElement.getChild("folding");
        if (foldingElement != null) {
            new ReadActionRunner<CodeFoldingState>() {
                @Override
                protected CodeFoldingState run() {
                    Document document = DocumentUtil.getDocument(virtualFile);
                    return CodeFoldingManager.getInstance(project).readFoldingState(foldingElement, document);
                }
            }.start();
        }

    }

    public void writeState(Element targetElement, Project project) {
        targetElement.setAttribute("line", Integer.toString(line));
        targetElement.setAttribute("column", Integer.toString(column));
        targetElement.setAttribute("selection-start", Integer.toString(selectionStart));
        targetElement.setAttribute("selection-end", Integer.toString(selectionEnd));
        targetElement.setAttribute("vertical-scroll-proportion", Float.toString(verticalScrollProportion));
        if (foldingState != null) {
            Element foldingElement = new Element("folding");
            targetElement.addContent(foldingElement);
            try {
                CodeFoldingManager.getInstance(project).writeFoldingState(foldingState, foldingElement);
            } catch (WriteExternalException e) {
            }
        }
    }

    public void loadFromEditor(@NotNull FileEditorStateLevel level, @NotNull TextEditor textEditor) {
        Editor editor = textEditor.getEditor();
        SelectionModel selectionModel = editor.getSelectionModel();
        LogicalPosition logicalPosition = editor.getCaretModel().getLogicalPosition();

        line = logicalPosition.line;
        column = logicalPosition.column;

        if(FileEditorStateLevel.FULL == level) {
            selectionStart = selectionModel.getSelectionStart();
            selectionEnd = selectionModel.getSelectionEnd();

            Project project = textEditor.getEditor().getProject();
            if(project != null){
                PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());
                CodeFoldingState foldingState = CodeFoldingManager.getInstance(project).saveFoldingState(editor);
                this.foldingState = foldingState;
            }
        }
        verticalScrollProportion = level != FileEditorStateLevel.UNDO ? EditorUtil.calcVerticalScrollProportion(editor) : -1F;
    }

    public void applyToEditor(@NotNull TextEditor textEditor) {
        final Editor editor = textEditor.getEditor();
        SelectionModel selectionModel = editor.getSelectionModel();

        LogicalPosition logicalPosition = new LogicalPosition(line, column);
        editor.getCaretModel().moveToLogicalPosition(logicalPosition);
        selectionModel.removeSelection();
        editor.getScrollingModel().scrollToCaret(ScrollType.RELATIVE);
        if (verticalScrollProportion != -1F)
            EditorUtil.setVerticalScrollProportion(editor, verticalScrollProportion);
        Document document = editor.getDocument();
        if (selectionStart == selectionEnd) {
            selectionModel.removeSelection();
        } else {
            int selectionStart = Math.min(this.selectionStart, document.getTextLength());
            int selectionEnd = Math.min(this.selectionEnd, document.getTextLength());
            selectionModel.setSelection(selectionStart, selectionEnd);
        }
        ((EditorEx) editor).stopOptimizedScrolling();
        editor.getScrollingModel().scrollToCaret(ScrollType.RELATIVE);

        final Project project = editor.getProject();
        if (project != null && foldingState != null) {
            PsiDocumentManager.getInstance(project).commitDocument(document);
            new SimpleLaterInvocator() {
                @Override
                protected void execute() {
                    CodeFoldingManager.getInstance(project).
                            restoreFoldingState(editor, getFoldingState());
                }
            }.start();
            //editor.getFoldingModel().runBatchFoldingOperation(runnable);
        }
    }

    /*****************************************************************
     *                     equals / hashCode                         *
     *****************************************************************/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BasicTextEditorState that = (BasicTextEditorState) o;

        if (line != that.line) return false;
        if (column != that.column) return false;
        if (selectionStart != that.selectionStart) return false;
        if (selectionEnd != that.selectionEnd) return false;
        if (Float.compare(that.verticalScrollProportion, verticalScrollProportion) != 0) return false;
        if (foldingState != null ? !foldingState.equals(that.foldingState) : that.foldingState != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = line;
        result = 31 * result + column;
        result = 31 * result + selectionStart;
        result = 31 * result + selectionEnd;
        result = 31 * result + (verticalScrollProportion != +0.0f ? Float.floatToIntBits(verticalScrollProportion) : 0);
        result = 31 * result + (foldingState != null ? foldingState.hashCode() : 0);
        return result;
    }
}
