package com.dci.intellij.dbn.common.environment;

import java.util.Set;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.environment.options.listener.EnvironmentChangeListener;
import com.dci.intellij.dbn.common.event.EventManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.impl.EditorsSplitters;
import com.intellij.openapi.fileEditor.impl.FileEditorManagerImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

@State(
        name = "DBNavigator.Project.EnvironmentManager",
        storages = {
                @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/dbnavigator.xml", scheme = StorageScheme.DIRECTORY_BASED),
                @Storage(file = StoragePathMacros.PROJECT_FILE)}
)
public class EnvironmentManager extends AbstractProjectComponent implements PersistentStateComponent<Element>, Disposable {
    private EnvironmentManager(Project project) {
        super(project);
        EventManager.subscribe(project, EnvironmentChangeListener.TOPIC, environmentChangeListener);

    }

    public static EnvironmentManager getInstance(Project project) {
        return project.getComponent(EnvironmentManager.class);
    }
    
    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.Project.EnvironmentManager";
    }

    private EnvironmentChangeListener environmentChangeListener = new EnvironmentChangeListener() {
        @Override
        public void configurationChanged() {
            FileEditorManagerImpl fileEditorManager = (FileEditorManagerImpl) FileEditorManager.getInstance(getProject());
            VirtualFile[] openFiles = fileEditorManager.getOpenFiles();
            Set<EditorsSplitters> splitters = fileEditorManager.getAllSplitters();
            for (VirtualFile virtualFile : openFiles) {
                for (EditorsSplitters splitter : splitters) {
                    splitter.updateFileBackgroundColor(virtualFile);
                }
            }
        }
    };


    public void dispose() {
        EventManager.unsubscribe(environmentChangeListener);
    }

    /*********************************************
     *            PersistentStateComponent       *
     *********************************************/
    @Nullable
    @Override
    public Element getState() {
        return null;
    }

    @Override
    public void loadState(Element element) {
    }
}
