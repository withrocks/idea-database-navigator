package com.dci.intellij.dbn.debugger.evaluation;

import com.dci.intellij.dbn.language.psql.PSQLFileType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.impl.DocumentImpl;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.EvaluationMode;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DBProgramDebuggerEditorsProvider extends XDebuggerEditorsProvider {
    public static final DBProgramDebuggerEditorsProvider INSTANCE = new DBProgramDebuggerEditorsProvider();

    private DBProgramDebuggerEditorsProvider(){}

    @NotNull
    @Override
    public FileType getFileType() {
        return PSQLFileType.INSTANCE;
    }

    @NotNull
    public Document createDocument(@NotNull Project project, @NotNull String text, @Nullable XSourcePosition sourcePosition, @NotNull EvaluationMode evaluationMode) {
        return new DocumentImpl(text);
    }

    @NotNull
    public Document createDocument(@NotNull Project project, @NotNull String text, @Nullable XSourcePosition sourcePosition) {
        return new DocumentImpl(text);
    }


}
