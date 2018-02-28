package com.dci.intellij.dbn.editor.session.action;

import javax.swing.Icon;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.action.DBNDataKeys;
import com.dci.intellij.dbn.editor.session.SessionBrowser;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.DumbAwareAction;

public abstract class AbstractSessionBrowserAction extends DumbAwareAction {
    public AbstractSessionBrowserAction(String text) {
        super(text);
    }

    public AbstractSessionBrowserAction(String text, Icon icon) {
        super(text, null, icon);
    }

    @Nullable
    public static SessionBrowser getSessionBrowser(AnActionEvent e) {
        SessionBrowser sessionBrowser = e.getData((DBNDataKeys.SESSION_BROWSER));
        if (sessionBrowser == null) {
            FileEditor fileEditor = e.getData(PlatformDataKeys.FILE_EDITOR);
            if (fileEditor instanceof SessionBrowser) {
                return (SessionBrowser) fileEditor;
            }
        } else {
            return sessionBrowser;
        }
        return null;
    }
}
