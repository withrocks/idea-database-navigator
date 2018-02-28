package com.dci.intellij.dbn.editor.console;

import org.jdom.CDATA;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Text;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.editor.BasicTextEditorState;
import com.dci.intellij.dbn.common.thread.ConditionalLaterInvocator;
import com.dci.intellij.dbn.common.thread.WriteActionRunner;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.vfs.DBConsoleVirtualFile;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;

public class SQLConsoleEditorState extends BasicTextEditorState {
    private String content = "";
    private String currentSchema = "";

    @Override
    public void writeState(Element targetElement, Project project) {
        super.writeState(targetElement, project);
        Element contentElement = new Element("content");
        contentElement.setAttribute("current-schema", currentSchema);
        targetElement.addContent(contentElement);
        String content = StringUtil.replace(this.content, "\n", "<br>");
        content = StringUtil.replace(content, "  ", "<sp>");
        CDATA cdata = new CDATA(content);
        contentElement.setContent(cdata);

    }

    @Override
    public void readState(@NotNull Element sourceElement, Project project, VirtualFile virtualFile) {
        super.readState(sourceElement, project, virtualFile);
        Element contentElement = sourceElement.getChild("content");
        if (contentElement != null) {
            currentSchema = contentElement.getAttributeValue("current-schema");
            if (contentElement.getContentSize() > 0) {

                Content content = contentElement.getContent(0);
                String textContent = "";
                if (content instanceof Text) {
                    Text cdata = (Text) content;
                    textContent = cdata.getText();
                }

                textContent = StringUtil.replace(textContent, "<br>", "\n");
                textContent = StringUtil.replace(textContent, "<sp>", "  ");
                this.content = textContent;
            }
        }
    }

    @Override
    public void loadFromEditor(@NotNull FileEditorStateLevel level, @NotNull TextEditor textEditor) {
        super.loadFromEditor(level, textEditor);
        content = textEditor.getEditor().getDocument().getText();
        DBConsoleVirtualFile file = (DBConsoleVirtualFile) DocumentUtil.getVirtualFile(textEditor.getEditor());
        DBSchema schema = file.getCurrentSchema();
        currentSchema = schema == null ? "" : schema.getName();
    }

    @Override
    public void applyToEditor(@NotNull final TextEditor textEditor) {
        new ConditionalLaterInvocator() {
            @Override
            public void execute() {
                new WriteActionRunner() {
                    public void run() {
                        textEditor.getEditor().getDocument().setText(content);
                        SQLConsoleEditorState.super.applyToEditor(textEditor);
                        DBConsoleVirtualFile file = (DBConsoleVirtualFile) DocumentUtil.getVirtualFile(textEditor.getEditor());
                        if (StringUtil.isNotEmpty(currentSchema)) {
                            file.setCurrentSchemaName(currentSchema);
                        }
                    }
                }.start();
            }
        }.start();
    }
}
