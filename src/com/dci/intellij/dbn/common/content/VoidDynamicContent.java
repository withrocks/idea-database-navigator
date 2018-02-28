package com.dci.intellij.dbn.common.content;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.content.dependency.ContentDependencyAdapter;
import com.dci.intellij.dbn.common.content.dependency.VoidContentDependencyAdapter;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoader;
import com.dci.intellij.dbn.common.content.loader.VoidDynamicContentLoader;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.GenericDatabaseElement;
import com.intellij.openapi.project.Project;

public class VoidDynamicContent implements DynamicContent{
    List elements = new ArrayList();

    public static final VoidDynamicContent INSTANCE = new VoidDynamicContent();

    private VoidDynamicContent() {

    }

    @Override
    public boolean shouldLoad(boolean force) {
        return false;
    }

    @Override
    public void load(boolean force) {

    }

    @Override
    public void loadInBackground(boolean force) {

    }


    @Override
    public void reload() {

    }

    @Override
    public long getChangeTimestamp() {
        return 0;
    }

    @Override
    public boolean isLoaded() {
        return true;
    }

    @Override
    public boolean isSubContent() {
        return false;
    }

    @Override
    public boolean canLoadFast() {
        return true;
    }

    @Override
    public boolean isLoading() {
        return false;
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public boolean isDisposed() {
        return false;
    }

    @Override
    public void setDirty(boolean dirty) {

    }

    @Override
    public Project getProject() {
        return null;
    }

    @Override
    public String getContentDescription() {
        return "Empty Content";
    }

    @NotNull
    @Override
    public List getElements() {
        return elements;
    }

    @Override
    public List getElements(String name) {
        return null;
    }

    @Override
    public DynamicContentElement getElement(String name, int overload) {
        return null;
    }

    @Override
    public void setElements(@Nullable List elements) {

    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public GenericDatabaseElement getParent() {
        return null;
    }

    @Override
    public DynamicContentLoader getLoader() {
        return VoidDynamicContentLoader.INSTANCE;
    }

    @Override
    public ContentDependencyAdapter getDependencyAdapter() {
        return VoidContentDependencyAdapter.INSTANCE;
    }

    @Override
    public ConnectionHandler getConnectionHandler() {
        return null;
    }

    @Override
    public void updateChangeTimestamp() {

    }

    @Override
    public String getName() {
        return "Empty Content";
    }

    @Override
    public boolean accepts(DynamicContentElement element) {
        return false;
    }

    @Override
    public void checkDisposed() throws InterruptedException {

    }

    @Override
    public void dispose() {

    }
}
