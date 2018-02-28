package com.dci.intellij.dbn.database.postgres;

import java.lang.reflect.Method;
import java.sql.SQLException;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.database.DatabaseMessageParserInterface;
import com.dci.intellij.dbn.database.DatabaseObjectIdentifier;
import com.intellij.openapi.diagnostic.Logger;

public class PostgresMessageParserInterface implements DatabaseMessageParserInterface {
    private static final Logger LOGGER = LoggerFactory.createLogger();

    @Nullable
    public DatabaseObjectIdentifier identifyObject(SQLException exception) {
         return null;
    }

    @Override
    public boolean isTimeoutException(SQLException e) {
        return false;
    }

    @Override
    public boolean isModelException(SQLException e) {
        String sqlState = getSqlState(e);
        return StringUtil.isOneOfIgnoreCase(sqlState, "3D000", "3F000", "42P01", "42703", "42704");
    }

    @Override
    public boolean isAuthenticationException(SQLException e) {
        String sqlState = getSqlState(e);
        return StringUtil.isOneOfIgnoreCase(sqlState, "28P01");
    }

    private static String getSqlState(SQLException e) {
        try {
            Method method = e.getClass().getMethod("getSQLState");
            return (String) method.invoke(e);
        } catch (Exception ex) {
            LOGGER.error("Could not get exception SQLState", ex);
        }
        return "";
    }

}