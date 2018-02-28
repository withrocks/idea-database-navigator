package com.dci.intellij.dbn.debugger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.debugger.breakpoint.BreakpointUpdaterFileEditorListener;
import com.dci.intellij.dbn.debugger.execution.DBProgramRunConfiguration;
import com.dci.intellij.dbn.debugger.execution.DBProgramRunConfigurationFactory;
import com.dci.intellij.dbn.debugger.execution.DBProgramRunConfigurationType;
import com.dci.intellij.dbn.debugger.execution.DBProgramRunner;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.DBSystemPrivilege;
import com.dci.intellij.dbn.object.DBUser;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.common.status.DBObjectStatus;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.RunManagerEx;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.RunnerRegistry;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.util.containers.ContainerUtil;
import gnu.trove.THashSet;

@State(
    name = "DBNavigator.Project.DebuggerManager",
    storages = {
        @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/dbnavigator.xml", scheme = StorageScheme.DIRECTORY_BASED),
        @Storage(file = StoragePathMacros.PROJECT_FILE)}
)
public class DatabaseDebuggerManager extends AbstractProjectComponent implements PersistentStateComponent<Element> {
    private Set<ConnectionHandler> activeDebugSessions = new THashSet<ConnectionHandler>();

    private DatabaseDebuggerManager(Project project) {
        super(project);
        FileEditorManager.getInstance(project).addFileEditorManagerListener(new BreakpointUpdaterFileEditorListener());
    }

    public void registerDebugSession(ConnectionHandler connectionHandler) {
        activeDebugSessions.add(connectionHandler);
    }

    public void unregisterDebugSession(ConnectionHandler connectionHandler) {
        activeDebugSessions.remove(connectionHandler);
    }

    public boolean checkForbiddenOperation(ConnectionHandler connectionHandler) {
        return checkForbiddenOperation(connectionHandler, null);
    }
    public boolean checkForbiddenOperation(ConnectionHandler connectionHandler, String message) {
        if (activeDebugSessions.contains(connectionHandler)) {
            MessageUtil.showErrorDialog(getProject(), message == null ? "Operation not supported during active debug session." : message);
            return false;
        }
        return true;
    }

    public static DBProgramRunConfigurationType getConfigurationType() {
        ConfigurationType[] configurationTypes = Extensions.getExtensions(ConfigurationType.CONFIGURATION_TYPE_EP);
        return ContainerUtil.findInstance(configurationTypes, DBProgramRunConfigurationType.class);
    }

    public static String createConfigurationName(DBMethod method) {
        DBProgramRunConfigurationType configurationType = getConfigurationType();
        RunManagerEx runManager = (RunManagerEx) RunManagerEx.getInstance(method.getProject());
        RunnerAndConfigurationSettings[] configurationSettings = runManager.getConfigurationSettings(configurationType);

        String name = method.getName();
        while (nameExists(configurationSettings, name)) {
            name = NamingUtil.getNextNumberedName(name, true);
        }
        return name;
    }

    private static boolean nameExists(RunnerAndConfigurationSettings[] configurationSettings, String name) {
        for (RunnerAndConfigurationSettings configurationSetting : configurationSettings) {
            if (configurationSetting.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public void createDebugConfiguration(DBMethod method) {
        RunManagerEx runManager = (RunManagerEx) RunManagerEx.getInstance(method.getProject());
        DBProgramRunConfigurationType configurationType = getConfigurationType();

        RunnerAndConfigurationSettings runConfigurationSetting = null;
        RunnerAndConfigurationSettings[] configurationSettings = runManager.getConfigurationSettings(configurationType);
        for (RunnerAndConfigurationSettings configurationSetting : configurationSettings) {
            DBProgramRunConfiguration availableRunConfiguration = (DBProgramRunConfiguration) configurationSetting.getConfiguration();
            if (method.equals(availableRunConfiguration.getMethod())) {
                runConfigurationSetting = configurationSetting;
                break;
            }
        }

        // check whether a configuration already exists for the given method
        if (runConfigurationSetting == null) {
            DBProgramRunConfigurationFactory configurationFactory = configurationType.getConfigurationFactory();
            DBProgramRunConfiguration runConfiguration = configurationFactory.createConfiguration(method);
            runConfigurationSetting = runManager.createConfiguration(runConfiguration, configurationFactory);
            runManager.addConfiguration(runConfigurationSetting, false);
            runManager.setTemporaryConfiguration(runConfigurationSetting);

        }

        runManager.setSelectedConfiguration(runConfigurationSetting);
        ProgramRunner programRunner = RunnerRegistry.getInstance().findRunnerById(DBProgramRunner.RUNNER_ID);
        if (programRunner != null) {
            try {
                Executor executorInstance = DefaultDebugExecutor.getDebugExecutorInstance();
                if (executorInstance == null) {
                    throw new ExecutionException("Could not resolve debug executor");
                }

                ExecutionEnvironment executionEnvironment = new ExecutionEnvironment(executorInstance, programRunner, runConfigurationSetting, getProject());
                programRunner.execute(executionEnvironment);
            } catch (ExecutionException e) {
                MessageUtil.showErrorDialog(
                        getProject(), "Could not start debugger for " + method.getQualifiedName() + ". \n" +
                                "Reason: " + e.getMessage());
            }
        }
    }

    public List<DBSchemaObject> loadCompileDependencies(DBMethod method, ProgressIndicator progressIndicator) {
        DBSchemaObject executable = method.getProgram() == null ? method : method.getProgram();
        List<DBSchemaObject> compileList = new ArrayList<DBSchemaObject>();
        if (!executable.getStatus().is(DBObjectStatus.DEBUG)) {
            compileList.add(executable);
        }

        for (DBObject object : executable.getReferencedObjects()) {
            if (object instanceof DBSchemaObject && object != executable) {
                if (!progressIndicator.isCanceled()) {
                    DBSchemaObject schemaObject = (DBSchemaObject) object;
                    DBSchema schema = schemaObject.getSchema();
                    if (!schema.isPublicSchema() && !schema.isSystemSchema() && schemaObject.getStatus().has(DBObjectStatus.DEBUG)) {
                        if (!schemaObject.getStatus().is(DBObjectStatus.DEBUG)) {
                            compileList.add(schemaObject);
                            progressIndicator.setText("Loading dependencies of " + schemaObject.getQualifiedNameWithType());
                            schemaObject.getReferencedObjects();
                        }
                    }
                }
            }
        }

        Collections.sort(compileList, DEPENDENCY_COMPARATOR);
        return compileList;
    }

    public List<String> getMissingDebugPrivileges(ConnectionHandler connectionHandler) {
        String userName = connectionHandler.getUserName();
        DBUser user = connectionHandler.getObjectBundle().getUser(userName);
        String[] privilegeNames = connectionHandler.getInterfaceProvider().getDebuggerInterface().getRequiredPrivilegeNames();
        List<String> missingPrivileges = new ArrayList<String>();
        for (String privilegeName : privilegeNames) {
            DBSystemPrivilege systemPrivilege = connectionHandler.getObjectBundle().getSystemPrivilege(privilegeName);
            if (systemPrivilege == null || !user.hasSystemPrivilege(systemPrivilege))  {
                missingPrivileges.add(privilegeName);
            }
        }

        return missingPrivileges;
    }

    private static final Comparator<DBSchemaObject> DEPENDENCY_COMPARATOR = new Comparator<DBSchemaObject>() {
        public int compare(DBSchemaObject schemaObject1, DBSchemaObject schemaObject2) {
            if (schemaObject1.getReferencedObjects().contains(schemaObject2)) return 1;
            if (schemaObject2.getReferencedObjects().contains(schemaObject1)) return -1;
            return 0;
        }
    };



    /***************************************
     *            ProjectComponent         *
     ***************************************/
    public static DatabaseDebuggerManager getInstance(Project project) {
        return project.getComponent(DatabaseDebuggerManager.class);
    }

    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.Project.DebuggerManager";
    }
    public void disposeComponent() {
        super.disposeComponent();
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