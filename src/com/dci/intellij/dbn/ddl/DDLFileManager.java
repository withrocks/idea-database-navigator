package com.dci.intellij.dbn.ddl;

import java.util.ArrayList;
import java.util.List;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.thread.WriteActionRunner;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseDDLInterface;
import com.dci.intellij.dbn.ddl.options.DDLFileExtensionSettings;
import com.dci.intellij.dbn.ddl.options.DDLFileSettings;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.language.common.DBLanguageFileType;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.fileTypes.ExtensionFileNameMatcher;
import com.intellij.openapi.fileTypes.FileNameMatcher;
import com.intellij.openapi.fileTypes.FileTypeEvent;
import com.intellij.openapi.fileTypes.FileTypeListener;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;

@State(
    name = "DBNavigator.Project.DDLFileManager",
    storages = {
        @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/dbnavigator.xml", scheme = StorageScheme.DIRECTORY_BASED),
        @Storage(file = StoragePathMacros.PROJECT_FILE)}
)
public class DDLFileManager extends AbstractProjectComponent implements PersistentStateComponent<Element>{
    private DDLFileManager(Project project) {
        super(project);
    }

    public void registerExtensions() {
        new WriteActionRunner() {
            public void run() {
                if (!isDisposed()) {
                    EventManager.unsubscribe(fileTypeListener);
                    FileTypeManager fileTypeManager = FileTypeManager.getInstance();
                    List<DDLFileType> ddlFileTypeList = getExtensionSettings().getDDLFileTypes();
                    for (DDLFileType ddlFileType : ddlFileTypeList) {
                        for (String extension : ddlFileType.getExtensions()) {
                            fileTypeManager.associateExtension(ddlFileType.getLanguageFileType(), extension);
                        }
                    }
                    EventManager.subscribe(FileTypeManager.TOPIC, fileTypeListener);
                }
            }
        }.start();
    }

    public static DDLFileManager getInstance(Project project) {
        return project.getComponent(DDLFileManager.class);
    }

    public DDLFileExtensionSettings getExtensionSettings() {
        return DDLFileSettings.getInstance(getProject()).getExtensionSettings();
    }

    public DDLFileType getDDLFileType(String ddlFileTypeId) {
        return getExtensionSettings().getDDLFileType(ddlFileTypeId);
    }

    public DDLFileType getDDLFileTypeForExtension(String extension) {
        return getExtensionSettings().getDDLFileTypeForExtension(extension);
    }

    public String createDDLStatement(DBSourceCodeVirtualFile virtualFile, DBContentType contentType) {
        DBSchemaObject object = virtualFile.getObject();
        if (object != null) {
            String content = virtualFile.getContent().trim();
            if (content.length() > 0) {
                Project project = getProject();

                ConnectionHandler connectionHandler = object.getConnectionHandler();
                if(connectionHandler != null) {
                    String alternativeStatementDelimiter = connectionHandler.getSettings().getDetailSettings().getAlternativeStatementDelimiter();
                    DatabaseDDLInterface ddlInterface = connectionHandler.getInterfaceProvider().getDDLInterface();
                    return ddlInterface.createDDLStatement(project,
                            object.getObjectType().getTypeId(),
                            connectionHandler.getUserName(),
                            object.getSchema().getName(),
                            object.getName(),
                            contentType,
                            content,
                            alternativeStatementDelimiter);

                }
                return "";
            }
        }
        return "";
    }


    /***************************************
     *            FileTypeListener         *
     ***************************************/

    private FileTypeListener fileTypeListener = new FileTypeListener() {
        @Override
        public void beforeFileTypesChanged(FileTypeEvent event) {

        }

        @Override
        public void fileTypesChanged(FileTypeEvent event) {
            StringBuilder restoredAssociations = null;
            FileTypeManager fileTypeManager = FileTypeManager.getInstance();
            List<DDLFileType> ddlFileTypeList = getExtensionSettings().getDDLFileTypes();
            for (DDLFileType ddlFileType : ddlFileTypeList) {
                DBLanguageFileType fileType = ddlFileType.getLanguageFileType();
                List<FileNameMatcher> associations = fileTypeManager.getAssociations(fileType);
                List<String> registeredExtension = new ArrayList<String>();
                for (FileNameMatcher association : associations) {
                    if (association instanceof ExtensionFileNameMatcher) {
                        ExtensionFileNameMatcher extensionMatcher = (ExtensionFileNameMatcher) association;
                        registeredExtension.add(extensionMatcher.getExtension());
                    }
                }

                for (String extension : ddlFileType.getExtensions()) {
                    if (!registeredExtension.contains(extension)) {
                        fileTypeManager.associateExtension(fileType, extension);
                        if (restoredAssociations == null) {
                            restoredAssociations = new StringBuilder();
                        } else {
                            restoredAssociations.append(", ");
                        }
                        restoredAssociations.append(extension);

                    }
                }
            }
            if (restoredAssociations != null) {
                String message =
                        "Following file associations have been restored: \"" + restoredAssociations + "\". " +
                                "They are registered as DDL file types in project \"" + getProject().getName() + "\".\n" +
                                "Please remove them from project DDL configuration first (Project Settings > DB Navigator > DDL File Settings).";
                MessageUtil.showWarningDialog(getProject(), "Restored file extensions", message);
            }
        }    };

    /***************************************
     *            ProjectComponent         *
     ***************************************/
    @NotNull
    public String getComponentName() {
        return "DBNavigator.Project.DDLFileManager";
    }

    public void projectOpened() {
        new SimpleLaterInvocator() {
            public void execute() {
                registerExtensions();
            }
        }.start();
    }

    public void projectClosed() {
        EventManager.unsubscribe(fileTypeListener);
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
