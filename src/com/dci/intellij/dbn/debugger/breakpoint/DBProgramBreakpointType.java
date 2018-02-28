package com.dci.intellij.dbn.debugger.breakpoint;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.debugger.evaluation.DBProgramDebuggerEditorsProvider;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.dci.intellij.dbn.language.psql.PSQLFileType;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.xdebugger.XDebuggerBundle;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.breakpoints.XLineBreakpointType;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;

public class DBProgramBreakpointType extends XLineBreakpointType<DBProgramBreakpointProperties> {
    public DBProgramBreakpointType() {
        super("db-program", "DB-Program Breakpoint");
    }

    @Override
    public boolean canPutAt(@NotNull VirtualFile file, int line, @NotNull Project project) {
        if (file.getFileType().equals(PSQLFileType.INSTANCE)) {
            if (file instanceof DBSourceCodeVirtualFile) {
                DBSourceCodeVirtualFile sourceCodeFile = (DBSourceCodeVirtualFile) file;
                DBContentType contentType = sourceCodeFile.getContentType();
                if (contentType == DBContentType.CODE || contentType == DBContentType.CODE_BODY) {
                    Document document = DocumentUtil.getDocument(file);
                    int lineOffset = document.getLineStartOffset(line);
                    PsiFile psiFile = PsiUtil.getPsiFile(project, file);
                    PsiElement element = psiFile.findElementAt(lineOffset);
                    while (element != null && !(element instanceof BasePsiElement)) {
                        element = element.getNextSibling();
                    }
                    if (element != null) {
                        BasePsiElement basePsiElement = (BasePsiElement) element;
                        int elementLine = document.getLineNumber(basePsiElement.getTextOffset());
                        return elementLine == line;
                    }

                    /*if (element != null) {
                        BasePsiElement basePsiElement = (BasePsiElement) element;
                        BasePsiElement executableCodePsiElement = basePsiElement.lookupPsiElementByAttribute(ElementTypeAttribute.EXECUTABLE_CODE);
                        if (executableCodePsiElement != null){
                            int executableLine = document.getLineNumber(executableCodePsiElement.getTextOffset());
                            return executableLine == line;
                        }
                    }*/
                }
            }
        }
        return false;
    }

    @Override
    public DBProgramBreakpointProperties createBreakpointProperties(@NotNull VirtualFile file, int line) {
        return new DBProgramBreakpointProperties(file, line);
    }

    @Override
    public XDebuggerEditorsProvider getEditorsProvider() {
        return DBProgramDebuggerEditorsProvider.INSTANCE;
    }

    @Nullable
    @Override
    public XDebuggerEditorsProvider getEditorsProvider(@NotNull XLineBreakpoint<DBProgramBreakpointProperties> breakpoint, @NotNull Project project) {
        return DBProgramDebuggerEditorsProvider.INSTANCE;
    }

    @Override
    public String getDisplayText(XLineBreakpoint breakpoint) {
        XSourcePosition sourcePosition = breakpoint.getSourcePosition();
        if (sourcePosition != null ){
        VirtualFile file = sourcePosition.getFile();
        return XDebuggerBundle.message("xbreakpoint.default.display.text",
                breakpoint.getLine() + 1,
                file.getPresentableUrl());
        }
        return "unknown";
    }
}
