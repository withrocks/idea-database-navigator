package com.dci.intellij.dbn.editor.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.editor.data.ui.DatasetLoadErrorNotificationPanel;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DBEditableObjectVirtualFile;
import com.intellij.ide.FrameStateManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotifications;

public class DatasetEditorNotificationProvider extends EditorNotifications.Provider<DatasetLoadErrorNotificationPanel> {
    private static final Key<DatasetLoadErrorNotificationPanel> KEY = Key.create("DBNavigator.DatasetLoadErrorNotificationPanel");
    private Project project;

    public DatasetEditorNotificationProvider(final Project project, @NotNull FrameStateManager frameStateManager) {
        this.project = project;

        EventManager.subscribe(project, DatasetLoadListener.TOPIC, datasetLoadListener);

    }

    DatasetLoadListener datasetLoadListener = new DatasetLoadListener() {
        @Override
        public void datasetLoaded(VirtualFile virtualFile) {
            if (virtualFile != null && !project.isDisposed()) {
                EditorNotifications notifications = EditorNotifications.getInstance(project);
                notifications.updateNotifications(virtualFile);
            }
        }
    };

    @NotNull
    @Override
    public Key<DatasetLoadErrorNotificationPanel> getKey() {
        return KEY;
    }

    @Nullable
    @Override
    public DatasetLoadErrorNotificationPanel createNotificationPanel(@NotNull VirtualFile virtualFile, @NotNull FileEditor fileEditor) {
        if (virtualFile instanceof DBEditableObjectVirtualFile) {
            if (fileEditor instanceof DatasetEditor) {
                DBEditableObjectVirtualFile editableObjectFile = (DBEditableObjectVirtualFile) virtualFile;
                DBSchemaObject editableObject = editableObjectFile.getObject();
                DatasetEditor datasetEditor = (DatasetEditor) fileEditor;
                String sourceLoadError = datasetEditor.getDataLoadError();
                if (StringUtil.isNotEmpty(sourceLoadError)) {
                    return createPanel(editableObject, sourceLoadError);
                }

            }
        }
        return null;
    }

    private DatasetLoadErrorNotificationPanel createPanel(final DBSchemaObject editableObject, String sourceLoadError) {
        DatasetLoadErrorNotificationPanel panel = new DatasetLoadErrorNotificationPanel();
        panel.setText("Could not load data for " + editableObject.getQualifiedNameWithType() + ". Error details: " + sourceLoadError.replace("\n", " "));
        return panel;
    }


}
