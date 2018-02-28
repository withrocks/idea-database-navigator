package com.dci.intellij.dbn.connection;

import javax.swing.Icon;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.environment.EnvironmentType;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.dci.intellij.dbn.connection.console.DatabaseConsoleBundle;
import com.dci.intellij.dbn.connection.transaction.UncommittedChangeBundle;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.navigation.psi.NavigationPsiCache;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import com.dci.intellij.dbn.vfs.DBSessionBrowserVirtualFile;
import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class VirtualConnectionHandler implements ConnectionHandler {
    private String id;
    private String name;
    private DatabaseType databaseType;
    private double databaseVersion;
    private Project project;
    private DatabaseInterfaceProvider interfaceProvider;
    private Map<String, String> properties = new HashMap<String, String>();
    private NavigationPsiCache psiCache;
    private ConnectionHandlerRef ref;

    public VirtualConnectionHandler(String id, String name, DatabaseType databaseType, double databaseVersion, Project project){
        this.id = id;
        this.name = name;
        this.project = project;
        this.databaseType = databaseType;
        this.databaseVersion = databaseVersion;
        this.ref = new ConnectionHandlerRef(this);
    }

    public DatabaseType getDatabaseType() {return databaseType;}

    @Override
    public double getDatabaseVersion() {
        return databaseVersion;
    }

    public Filter<BrowserTreeNode> getObjectTypeFilter() {
        return null;
    }

    @Override
    public NavigationPsiCache getPsiCache() {
        if (psiCache == null) {
            psiCache = new NavigationPsiCache(this);
        }
        return psiCache;
    }

    @Override
    public EnvironmentType getEnvironmentType() {
        return null;
    }

    @Override
    @Nullable
    public DBLanguageDialect resolveLanguageDialect(Language language) {
        if (language instanceof DBLanguageDialect) {
            return (DBLanguageDialect) language;
        } else if (language instanceof DBLanguage) {
            return getLanguageDialect((DBLanguage) language);
        }
        return null;
    }

    public DBLanguageDialect getLanguageDialect(DBLanguage language) {
        return getInterfaceProvider().getLanguageDialect(language);
    }

    public Project getProject() {return project;}

    public boolean isActive() {
        return true;
    }

    @Override public String getId() {return id;}
    @Override public String getName() {return name;}
    @Override public String getPresentableText() {
        return name;
    }
    @Override public String getQualifiedName() {
        return name;
    }
    @Override public String getDescription() {return "Virtual database connection"; }
    @Override public Icon getIcon() { return Icons.CONNECTION_VIRTUAL; }
    @Override public boolean isVirtual() {return true;}
    @Override public boolean isAutoCommit() {return false;}

    @Override public boolean isLoggingEnabled() {return false;}
    @Override public void setAutoCommit(boolean autoCommit) throws SQLException {}
    @Override public void setLoggingEnabled(boolean loggingEnabled) {}

    @Override public UncommittedChangeBundle getUncommittedChanges() {return null;}
    @Override public boolean isConnected() {return false;}
    @Override public boolean isDisposed() {
        return false;
    }

    public Map<String, String> getProperties() {return properties;}

    public DatabaseInterfaceProvider getInterfaceProvider() {
        if (interfaceProvider == null) {
            try {
                interfaceProvider = DatabaseInterfaceProviderFactory.createInterfaceProvider(this);
            } catch (SQLException e) {
                // do not initialize
                return DatabaseInterfaceProviderFactory.GENERIC_INTERFACE_PROVIDER;
            }
        }
        return interfaceProvider;
    }

    public String getUser() {return "root";}
    public String getUserName() {return "root";}

    public Connection getPoolConnection() throws SQLException {return null;}
    public Connection getPoolConnection(DBSchema schema) throws SQLException {return null;}
    public Connection getStandaloneConnection() throws SQLException {return null;}
    public Connection getStandaloneConnection(DBSchema schema) throws SQLException {return null;}
    public void freePoolConnection(Connection connection) {}

    public ConnectionSettings getSettings() {return null;}
    public ConnectionStatus getConnectionStatus() {return null;}

    @Override
    public boolean isAllowConnection() {return false;}
    @Override
    public void setAllowConnection(boolean allowConnection) {}

    @Override
    public boolean canConnect() {
        return false;
    }

    public ConnectionBundle getConnectionBundle() {return null;}
    public ConnectionPool getConnectionPool() {return null;}

    @Override
    public ConnectionLoadMonitor getLoadMonitor() {
        return null;
    }

    public DBObjectBundle getObjectBundle() {return null;}
    public DBSchema getUserSchema() {return null;}

    @Override
    public DBSessionBrowserVirtualFile getSessionBrowserFile() {
        return null;
    }

    @Override
    public DatabaseConsoleBundle getConsoleBundle() {return null;}
    public boolean isValid(boolean check) {return true;}
    public boolean isValid() {return true;}
    public void disconnect() {}
    public void ping(boolean check) {}
    public int getIdleMinutes() {return 0;}

    @Override
    public ConnectionHandlerRef getRef() {
        return ref;
    }

    public ConnectionHandler clone() {return null;}
    public void notifyChanges(VirtualFile virtualFile) {}
    public void resetChanges() {}
    public boolean hasUncommittedChanges() {return false;}
    public void commit() throws SQLException {}
    public void rollback() throws SQLException {}
    public void dispose() {}
}
