package com.dci.intellij.dbn.debugger.evaluation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.debugger.frame.DBProgramDebugStackFrame;
import com.dci.intellij.dbn.debugger.frame.DBProgramDebugValue;
import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.dci.intellij.dbn.language.common.psi.QualifiedIdentifierPsiElement;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;

public class DBProgramDebuggerEvaluator extends XDebuggerEvaluator {
    private DBProgramDebugStackFrame frame;

    public DBProgramDebuggerEvaluator(DBProgramDebugStackFrame frame) {
        this.frame = frame;
    }

    public boolean evaluateCondition(@NotNull String expression) {
        return false;
    }

    public String evaluateMessage(@NotNull String expression) {
        return null;
    }

    @Override
    public void evaluate(@NotNull String expression, @NotNull XEvaluationCallback callback, @Nullable XSourcePosition expressionPosition) {
        evaluate(expression, callback);
    }

    public void evaluate(@NotNull String expression, XEvaluationCallback callback) {
        DBProgramDebugValue value = frame.getValue(expression);
        if (value == null) {
            value = new DBProgramDebugValue(frame.getDebugProcess(), null, expression, null, null, frame.getIndex());
            frame.setValue(expression, value);
        }

        String errorMessage = value.getErrorMessage();
        if (errorMessage != null) {
            callback.errorOccurred(errorMessage);
        } else {
            callback.evaluated(value);
        }

    }

    @Nullable
    public TextRange getExpressionRangeAtOffset(Project project, Document document, int offset) {
        PsiFile psiFile = PsiUtil.getPsiFile(project, document);
        if (psiFile != null) {
            PsiElement psiElement = psiFile.findElementAt(offset);
            if (psiElement != null && psiElement.getParent() instanceof IdentifierPsiElement) {
                return psiElement.getTextRange();
            }
        }
        return null;
    }

    @Nullable
    @Override
    public TextRange getExpressionRangeAtOffset(Project project, Document document, int offset, boolean sideEffectsAllowed) {
        PsiFile psiFile = PsiUtil.getPsiFile(project, document);
        if (psiFile != null) {
            PsiElement psiElement = psiFile.findElementAt(offset);
            if (psiElement != null && psiElement.getParent() instanceof IdentifierPsiElement) {
                IdentifierPsiElement identifierPsiElement = (IdentifierPsiElement) psiElement.getParent();
                QualifiedIdentifierPsiElement qualifiedIdentifier = identifierPsiElement.getParentQualifiedIdentifier();
                if (qualifiedIdentifier == null) {
                    return identifierPsiElement.getTextRange();
                } else {
                    int startOffset = qualifiedIdentifier.getTextRange().getStartOffset();
                    int endOffset = identifierPsiElement.getTextRange().getEndOffset();
                    return new TextRange(startOffset, endOffset);
                }

            }
        }
        return null;
    }
}
