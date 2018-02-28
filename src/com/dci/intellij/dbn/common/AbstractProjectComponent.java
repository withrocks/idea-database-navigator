package com.dci.intellij.dbn.common;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;

public abstract class AbstractProjectComponent implements ProjectComponent, ProjectManagerListener{
    private Project project;
    private boolean isDisposed = false;

    protected AbstractProjectComponent(Project project) {
        this.project = project;
        ProjectManager projectManager = ProjectManager.getInstance();
        projectManager.addProjectManagerListener(project, this);
    }

    public Project getProject() {
        return project;
    }

    public void projectOpened() {
    }

    public void projectClosed() {
    }

    public void initComponent() {
    }

    /***********************************************
     *            ProjectManagerListener           *
     ***********************************************/
    @Override
    public void projectOpened(Project project) {

    }

    @Override
    public boolean canCloseProject(Project project) {
        return true;
    }

    @Override
    public void projectClosed(Project project) {

    }

    @Override
    public void projectClosing(Project project) {

    }


    /********************************************* *
     *                Disposable                   *
     ***********************************************/
    public boolean isDisposed() {
        return isDisposed;
    }

    public void disposeComponent() {
        isDisposed = true;
        project = null;
    }
}
