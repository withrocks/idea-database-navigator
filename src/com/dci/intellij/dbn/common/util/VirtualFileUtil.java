package com.dci.intellij.dbn.common.util;

import javax.swing.Icon;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.vfs.DBVirtualFile;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.io.ReadOnlyAttributeUtil;

public class VirtualFileUtil {

    public static Icon getIcon(VirtualFile virtualFile) {
        if (virtualFile instanceof DBVirtualFile) {
            DBVirtualFile file = (DBVirtualFile) virtualFile;
            return file.getIcon();
        }
        return virtualFile.getFileType().getIcon();
    }

    public static boolean isDatabaseFileSystem(@NotNull VirtualFile file) {
        return file.getFileSystem() == DatabaseFileSystem.getInstance();
    }

    public static boolean isLocalFileSystem(@NotNull VirtualFile file) {
        return file.isInLocalFileSystem();
    }

    public static boolean isVirtualFileSystem(@NotNull VirtualFile file) {
        return !isDatabaseFileSystem(file) && !isLocalFileSystem(file);
    }    

    public static VirtualFile ioFileToVirtualFile(File file) {
        return LocalFileSystem.getInstance().findFileByIoFile(file);
    }

    public static void setReadOnlyAttribute(VirtualFile file, boolean readonly) {
        try {
            ReadOnlyAttributeUtil.setReadOnlyAttribute(file, readonly);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setReadOnlyAttribute(String path, boolean readonly) {
        try {
            ReadOnlyAttributeUtil.setReadOnlyAttribute(path, readonly);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static VirtualFile[] lookupFilesForName(Project project, String name) {
        ProjectRootManager rootManager = ProjectRootManager.getInstance(project);
        VirtualFile[] contentRoots = rootManager.getContentRoots();
        return lookupFilesForName(contentRoots, name);
    }

    public static VirtualFile[] lookupFilesForName(Module module, String name) {
        ProjectRootManager rootManager = ProjectRootManager.getInstance(module.getProject());
        VirtualFile[] contentRoots = rootManager.getContentRoots();
        return lookupFilesForName(contentRoots, name);
    }

    public static VirtualFile[] lookupFilesForName(VirtualFile[] roots, String name) {
        List<VirtualFile> bucket = new ArrayList<VirtualFile>();
        for (VirtualFile root: roots) {
            collectFilesForName(root, name, bucket);
        }
        return bucket.toArray(new VirtualFile[bucket.size()]);
    }

    private static void collectFilesForName(VirtualFile root, String name, List<VirtualFile> bucket) {
        for (VirtualFile virtualFile: root.getChildren()) {
            boolean fileIgnored = FileTypeManager.getInstance().isFileIgnored(virtualFile.getName());
            if (!fileIgnored) {
                if (virtualFile.isDirectory() ) {
                    collectFilesForName(virtualFile, name, bucket);
                } else {
                    if (virtualFile.getName().equalsIgnoreCase(name)) {
                        bucket.add(virtualFile);
                    }
                }
            }
        }
    }
}

