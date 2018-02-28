package com.dci.intellij.dbn.common.util;

import java.util.ArrayList;

import com.dci.intellij.dbn.common.editor.document.OverrideReadonlyFragmentModificationHandler;
import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.thread.ConditionalReadActionRunner;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.DBLanguageSyntaxHighlighter;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInsight.folding.CodeFoldingManager;
import com.intellij.ide.highlighter.HighlighterFactory;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.editor.colors.ColorKey;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.ex.DocumentBulkUpdateListener;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.util.FileContentUtil;

public class DocumentUtil {
    private static final Key<Boolean> FOLDING_STATE_KEY = Key.create("FOLDING_STATE_KEY");

    public static void touchDocument(final Editor editor, boolean reparse) {
        final Document document = editor.getDocument();

        // restart highlighting
        Project project = editor.getProject();
        final PsiFile file = DocumentUtil.getFile(editor);
        if (project != null && !project.isDisposed() && file instanceof DBLanguagePsiFile) {
            DBLanguagePsiFile dbLanguageFile = (DBLanguagePsiFile) file;
            DBLanguage dbLanguage = dbLanguageFile.getDBLanguage();
            if (dbLanguage != null) {
                ConnectionHandler connectionHandler = dbLanguageFile.getActiveConnection();
                DBLanguageSyntaxHighlighter syntaxHighlighter = getSyntaxHighlighter(dbLanguage, connectionHandler);

                EditorHighlighter editorHighlighter = HighlighterFactory.createHighlighter(syntaxHighlighter, editor.getColorsScheme());
                ((EditorEx) editor).setHighlighter(editorHighlighter);
            }
            if (reparse) {
                EventManager.notify(project, DocumentBulkUpdateListener.TOPIC).updateStarted(document);
                ArrayList<VirtualFile> files = new ArrayList<VirtualFile>();
                files.add(file.getVirtualFile());
                FileContentUtil.reparseFiles(project, files, true);
                CodeFoldingManager codeFoldingManager = CodeFoldingManager.getInstance(project);
                codeFoldingManager.buildInitialFoldings(editor);
            }
            refreshEditorAnnotations(file);
        }
    }

    private static DBLanguageSyntaxHighlighter getSyntaxHighlighter(DBLanguage dbLanguage, ConnectionHandler connectionHandler) {
        if (connectionHandler != null) {
            DBLanguageDialect languageDialect = connectionHandler.getLanguageDialect(dbLanguage);
            if (languageDialect != null) {
                return languageDialect.getSyntaxHighlighter();
            }
        }
        return dbLanguage.getMainLanguageDialect().getSyntaxHighlighter();
    }


    public static void refreshEditorAnnotations(Editor editor) {
        refreshEditorAnnotations(DocumentUtil.getFile(editor));
    }

    public static void refreshEditorAnnotations(PsiFile psiFile) {
        DaemonCodeAnalyzer.getInstance(psiFile.getProject()).restart(psiFile);
    }

    public static Document getDocument(PsiFile file) {
        return PsiDocumentManager.getInstance(file.getProject()).getDocument(file);
    }

    public static PsiFile getFile(Editor editor) {
        Project project = editor == null ? null : editor.getProject();
        return project == null ? null : PsiUtil.getPsiFile(project, editor.getDocument());
    }

    public static void createGuardedBlock(Document document, String reason, boolean highlight) {
        createGuardedBlock(document, 0, document.getTextLength(), reason);
        if (!highlight) {
            Editor[] editors = EditorFactory.getInstance().getEditors(document);
            for (Editor editor : editors) {
                ColorKey key = ColorKey.find("READONLY_FRAGMENT_BACKGROUND");
                EditorColorsScheme scheme = editor.getColorsScheme();
                scheme.setColor(key, scheme.getDefaultBackground());
            }
        }
    }

    public static void createGuardedBlock(Document document, int startOffset, int endOffset, String reason) {
        RangeMarker rangeMarker = document.createGuardedBlock(startOffset, endOffset);
        rangeMarker.setGreedyToLeft(true);
        rangeMarker.setGreedyToRight(false);
        document.putUserData(OverrideReadonlyFragmentModificationHandler.GUARDED_BLOCK_REASON, reason);
    }

    public static void removeGuardedBlock(Document document) {
        removeGuardedBlock(document, 0, document.getTextLength());
    }

    public static void removeGuardedBlock(Document document, int startOffset, int endOffset) {
        RangeMarker rangeMarker = document.getRangeGuard(startOffset, endOffset);
        document.removeGuardedBlock(rangeMarker);
        document.putUserData(OverrideReadonlyFragmentModificationHandler.GUARDED_BLOCK_REASON, null);
    }

    public static VirtualFile getVirtualFile(Editor editor) {
        return FileDocumentManager.getInstance().getFile(editor.getDocument());
    }

    public static Document getDocument(final VirtualFile virtualFile) {
        return new ConditionalReadActionRunner<Document>() {
            @Override
            protected Document run() {
                return FileDocumentManager.getInstance().getDocument(virtualFile);
            }
        }.start();
    }

    public static PsiFile getPsiFile(Project project, VirtualFile virtualFile) {
        Document document = getDocument(virtualFile);
        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        return psiDocumentManager.getPsiFile(document);
    }
}
