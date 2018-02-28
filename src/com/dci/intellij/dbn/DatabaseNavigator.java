package com.dci.intellij.dbn;

import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.dci.intellij.dbn.execution.ExecutionManager;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.ide.plugins.RepositoryHelper;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.util.proxy.CommonProxy;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.ProxySelector;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@State(
    name = "DBNavigator.Application.Settings",
    storages = {@Storage(file = StoragePathMacros.APP_CONFIG + "/other.xml")}
)
public class DatabaseNavigator implements ApplicationComponent, PersistentStateComponent<Element> {
    private static final String SQL_PLUGIN_ID = "com.intellij.sql";
    public static final String DBN_PLUGIN_ID = "DBN";
    /*static {
        Extensions.getRootArea().
                getExtensionPoint(CodeStyleSettingsProvider.EXTENSION_POINT_NAME).
                registerExtension(new SQLCodeStyleSettingsProvider());
    }*/

    @NotNull
    public String getComponentName() {
        return "DBNavigator.Application.Settings";
    }

    private boolean debugModeEnabled;
    private boolean developerModeEnabled;
    private boolean slowDatabaseModeEnabled;
    private boolean showPluginConflictDialog;
    private String repositoryPluginVersion;

    public void initComponent() {
        //ModuleTypeManager.getInstance().registerModuleType(DBModuleType.MODULE_TYPE);

        //FileTypeManager.getInstance().registerFileType(SQLFileType.INSTANCE, "sql");
        //FileTypeManager.getInstance().registerFileType(PSQLFileType.INSTANCE, "psql");
        //resolvePluginConflict();

        FileTemplateManager templateManager = FileTemplateManager.getInstance();
        if (templateManager.getTemplate("SQL Script") == null) {
            templateManager.addTemplate("SQL Script", "sql");
        }

        NotificationGroup notificationGroup = new NotificationGroup("Database Navigator", NotificationDisplayType.TOOL_WINDOW, true, ExecutionManager.TOOL_WINDOW_ID);

        Timer updateChecker = new Timer("DBN Plugin Update check task");
        updateChecker.schedule(new PluginUpdateChecker(), TimeUtil.ONE_SECOND, TimeUtil.ONE_HOUR);
    }

    private static boolean sqlPluginActive() {
        for (IdeaPluginDescriptor pluginDescriptor : PluginManager.getPlugins()) {
            if (pluginDescriptor.getPluginId().getIdString().equals(SQL_PLUGIN_ID)) {
                return !PluginManager.getDisabledPlugins().contains(SQL_PLUGIN_ID);
            }
        }
        return false;
    }

    public static DatabaseNavigator getInstance() {
        return ApplicationManager.getApplication().getComponent(DatabaseNavigator.class);
    }

    public boolean isDebugModeEnabled() {
        return debugModeEnabled;
    }

    public void setDebugModeEnabled(boolean debugModeEnabled) {
        this.debugModeEnabled = debugModeEnabled;
        SettingsUtil.isDebugEnabled = debugModeEnabled;
    }

    public boolean isDeveloperModeEnabled() {
        return developerModeEnabled;
    }

    public void setDeveloperModeEnabled(boolean developerModeEnabled) {
        this.developerModeEnabled = developerModeEnabled;
    }

    public boolean isSlowDatabaseModeEnabled() {
        return developerModeEnabled && slowDatabaseModeEnabled;
    }

    public void setSlowDatabaseModeEnabled(boolean slowDatabaseModeEnabled) {
        this.slowDatabaseModeEnabled = slowDatabaseModeEnabled;
    }

    public void disposeComponent() {
    }

    public String getName() {
        return null;
    }

    public String getPluginVersion() {
        IdeaPluginDescriptor pluginDescriptor = PluginManager.getPlugin(PluginId.getId(DatabaseNavigator.DBN_PLUGIN_ID));
        return pluginDescriptor.getVersion();
    }

    public String getRepositoryPluginVersion() {
        return repositoryPluginVersion;
    }

    private class PluginUpdateChecker extends TimerTask {
        public void run() {
            ProxySelector initialProxySelector = ProxySelector.getDefault();
            CommonProxy defaultProxy = CommonProxy.getInstance();
            boolean changeProxy = defaultProxy != initialProxySelector;
            try {
                if (changeProxy) {
                    ProxySelector.setDefault(defaultProxy);
                }

                List<IdeaPluginDescriptor> descriptors = RepositoryHelper.loadCachedPlugins();
                if (descriptors != null) {
                    for (IdeaPluginDescriptor descriptor : descriptors) {
                        if (descriptor.getPluginId().toString().equals(DatabaseNavigator.DBN_PLUGIN_ID)) {
                            repositoryPluginVersion = descriptor.getVersion();
                            break;
                        }
                    }
                }
            } catch (Exception e) {
            } finally {
                if (changeProxy) {
                    ProxySelector.setDefault(initialProxySelector);
                }
            }
        }
    }

    /*********************************************
     *            PersistentStateComponent       *
     *********************************************/
    @Nullable
    @Override
    public Element getState() {
        Element element = new Element("state");
        SettingsUtil.setBoolean(element, "enable-debug-mode", debugModeEnabled);
        SettingsUtil.setBoolean(element, "enable-developer-mode", developerModeEnabled);
        SettingsUtil.setBoolean(element, "show-plugin-conflict-dialog", showPluginConflictDialog);
        return element;
    }

    @Override
    public void loadState(Element element) {
        debugModeEnabled = SettingsUtil.getBoolean(element, "enable-debug-mode", false);
        developerModeEnabled = SettingsUtil.getBoolean(element, "enable-developer-mode", false);
        showPluginConflictDialog = SettingsUtil.getBoolean(element, "show-plugin-conflict-dialog", true);
        SettingsUtil.isDebugEnabled = debugModeEnabled;
    }
}

