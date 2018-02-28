package com.dci.intellij.dbn.connection;

import java.sql.SQLException;

import com.dci.intellij.dbn.connection.config.ConnectionDatabaseSettings;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.generic.GenericInterfaceProvider;
import com.dci.intellij.dbn.database.mysql.MySqlInterfaceProvider;
import com.dci.intellij.dbn.database.oracle.OracleInterfaceProvider;
import com.dci.intellij.dbn.database.postgres.PostgresInterfaceProvider;

public class DatabaseInterfaceProviderFactory {
    // fixme replace with generic data dictionary
    public static final DatabaseInterfaceProvider GENERIC_INTERFACE_PROVIDER = new GenericInterfaceProvider();
    public static final DatabaseInterfaceProvider ORACLE_INTERFACE_PROVIDER = new OracleInterfaceProvider();
    public static final DatabaseInterfaceProvider MYSQL_INTERFACE_PROVIDER = new MySqlInterfaceProvider();
    public static final DatabaseInterfaceProvider POSTGRES_INTERFACE_PROVIDER = new PostgresInterfaceProvider();

    public static DatabaseInterfaceProvider createInterfaceProvider(ConnectionHandler connectionHandler) throws SQLException {
        DatabaseType databaseType;
        if (connectionHandler != null && connectionHandler.isVirtual()) {
            databaseType = connectionHandler.getDatabaseType();
        } else {
            ConnectionDatabaseSettings databaseSettings = connectionHandler.getSettings().getDatabaseSettings();
            databaseType = databaseSettings.getDatabaseType();
            if (databaseType == null || databaseType == DatabaseType.UNKNOWN) {
                try {
                    databaseType = ConnectionUtil.getDatabaseType(connectionHandler.getStandaloneConnection());
                    databaseSettings.setDatabaseType(databaseType);
                } catch (SQLException e) {
                    if (databaseSettings.getDatabaseType() == null) {
                        databaseSettings.setDatabaseType(DatabaseType.UNKNOWN);
                    }
                    throw e;
                }
            }
        }


        if (databaseType == DatabaseType.ORACLE) {
            return ORACLE_INTERFACE_PROVIDER;
        } else if (databaseType == DatabaseType.MYSQL) {
            return MYSQL_INTERFACE_PROVIDER;
        } else if (databaseType == DatabaseType.POSTGRES) {
            return POSTGRES_INTERFACE_PROVIDER;
        }
        return GENERIC_INTERFACE_PROVIDER;
    }

    public static void reset() {
        GENERIC_INTERFACE_PROVIDER.reset();
        ORACLE_INTERFACE_PROVIDER.reset();
        MYSQL_INTERFACE_PROVIDER.reset();
        POSTGRES_INTERFACE_PROVIDER.reset();
    }
}
