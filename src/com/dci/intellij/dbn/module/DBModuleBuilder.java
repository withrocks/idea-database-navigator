package com.dci.intellij.dbn.module;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleWithNameAlreadyExists;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//import com.intellij.ide.util.projectWizard.SourcePathsBuilder;

public class DBModuleBuilder extends ModuleBuilder /*implements SourcePathsBuilder */{
    private String contentEntryPath;
    private List<Pair<String,String>> sourcePaths;

    public DBModuleBuilder() {
    }

    public void setupRootModel(ModifiableRootModel rootModel) throws ConfigurationException {
        String moduleRootPath = getContentEntryPath();
        if (moduleRootPath != null) {
            LocalFileSystem localFileSystem = LocalFileSystem.getInstance();
            VirtualFile moduleContentRoot = localFileSystem.refreshAndFindFileByPath(FileUtil.toSystemIndependentName(moduleRootPath));
            if (moduleContentRoot != null) {
                ContentEntry contentEntry = rootModel.addContentEntry(moduleContentRoot);
                List<Pair<String, String>> sourcePaths = getSourcePaths();
                if (sourcePaths != null) {
                    for (Pair<String, String> sourcePath : sourcePaths) {
                        VirtualFile sourceRoot = localFileSystem.refreshAndFindFileByPath(FileUtil.toSystemIndependentName(sourcePath.first));
                        if (sourceRoot != null) {
                            contentEntry.addSourceFolder(sourceRoot, false, sourcePath.second);
                        }
                    }
                }
            }
        }
    }


    public void setContentEntryPath(String contentEntryPath) {
        this.contentEntryPath = contentEntryPath;
    }

    public void setSourcePaths(List<Pair<String, String>> sourcePaths) {
        this.sourcePaths = sourcePaths;
    }

    public void addSourcePath(Pair<String, String> sourcePathInfo) {
        if (sourcePaths == null) {
            sourcePaths = new ArrayList<Pair<String, String>>();
            sourcePaths.add(sourcePathInfo);
        }
    }

    @Nullable
    public final String getContentEntryPath() {
        if (contentEntryPath == null) {
            contentEntryPath = getModuleFileDirectory();
            new File(contentEntryPath).mkdirs();
        }
        return contentEntryPath;
    }

    public List<Pair<String, String>> getSourcePaths() {
        if (sourcePaths == null) {
            sourcePaths = new ArrayList<Pair<String, String>>();
            /*String path = getContentEntryPath();
            new File(path).mkdirs();
            sourcePaths.add(Pair.create(path, ""));
            return sourcePaths;*/
        }
        return sourcePaths;
    }

    public ModuleType getModuleType() {
        return DBModuleType.MODULE_TYPE;
    }

    @NotNull
    public Module createModule(@NotNull ModifiableModuleModel moduleModel) throws InvalidDataException, IOException, ModuleWithNameAlreadyExists, JDOMException, ConfigurationException {
        Module module = super.createModule(moduleModel);
        return module;
    }


}
