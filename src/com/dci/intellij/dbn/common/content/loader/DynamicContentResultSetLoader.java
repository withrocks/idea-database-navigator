package com.dci.intellij.dbn.common.content.loader;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentElement;
import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionUtil;
import com.dci.intellij.dbn.database.DatabaseInterface;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.object.common.DBObject;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;

public abstract class DynamicContentResultSetLoader<T extends DynamicContentElement> implements DynamicContentLoader<T> {
    private static final Logger LOGGER = LoggerFactory.createLogger();

    public abstract ResultSet createResultSet(DynamicContent<T> dynamicContent, Connection connection) throws SQLException;
    public abstract T createElement(DynamicContent<T> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException;

    private class DebugInfo {                                                         
        private String id = UUID.randomUUID().toString();
        private long startTimestamp = System.currentTimeMillis();
    }

    private DebugInfo preLoadContent(DynamicContent dynamicContent) {
        if (SettingsUtil.isDebugEnabled) {
            DebugInfo debugInfo = new DebugInfo();
            LOGGER.info(
                    "[DBN-INFO] Loading " + dynamicContent.getContentDescription() +
                    " (id = " + debugInfo.id + ")");
            return debugInfo;
        }
        return null;
    }

    private void postLoadContent(DynamicContent dynamicContent, DebugInfo debugInfo) {
        if (debugInfo != null) {
            LOGGER.info(
                    "[DBN-INFO] Done loading " + dynamicContent.getContentDescription() +
                    " (id = " + debugInfo.id + ") - " +
                    (System.currentTimeMillis() - debugInfo.startTimestamp) + "ms"   );
        }
    }

    public void loadContent(DynamicContent<T> dynamicContent, boolean forceReload) throws DynamicContentLoadException, InterruptedException {
        boolean addDelay = DatabaseNavigator.getInstance().isSlowDatabaseModeEnabled();
        ProgressMonitor.setTaskDescription("Loading " + dynamicContent.getContentDescription());

        DebugInfo debugInfo = preLoadContent(dynamicContent);

        dynamicContent.checkDisposed();
        ConnectionHandler connectionHandler = dynamicContent.getConnectionHandler();
        LoaderCache loaderCache = new LoaderCache();
        Connection connection = null;
        ResultSet resultSet = null;
        int count = 0;
        try {
            dynamicContent.checkDisposed();
            connectionHandler.getLoadMonitor().incrementLoaderCount();
            connection = connectionHandler.getPoolConnection();
            dynamicContent.checkDisposed();
            resultSet = createResultSet(dynamicContent, connection);
            if (addDelay) Thread.sleep(500);
            List<T> list = null;
            while (resultSet != null && resultSet.next()) {
                if (addDelay) Thread.sleep(10);
                dynamicContent.checkDisposed();
                
                T element = null;
                try {
                    element = createElement(dynamicContent, resultSet, loaderCache);
                } catch (ProcessCanceledException e){
                    return;
                } catch (RuntimeException e) {
                    System.out.println("RuntimeException: " + e.getMessage());
                }

                dynamicContent.checkDisposed();
                if (element != null && dynamicContent.accepts(element)) {
                    if (list == null) list = new ArrayList<T>();
                    list.add(element);
                    if (count%10 == 0) {
                        String description = element.getDescription();
                        if (description != null)
                            ProgressMonitor.setSubtaskDescription(description);
                    }
                    count++;
                }
            }
            dynamicContent.checkDisposed();
            dynamicContent.setElements(list);
            postLoadContent(dynamicContent, debugInfo);
        } catch (Exception e) {
            if (e instanceof InterruptedException) throw (InterruptedException) e;
            if (e == DatabaseInterface.DBN_INTERRUPTED_EXCEPTION) throw new InterruptedException();
            if (e == DatabaseInterface.DBN_NOT_CONNECTED_EXCEPTION) throw new InterruptedException();

            String message = StringUtil.trim(e.getMessage()).replace("\n", " ");
            LOGGER.warn("Error loading database content (" + dynamicContent.getContentDescription() + "): " + message);

            boolean modelException = false;
            if (e instanceof SQLException) {
                SQLException sqlException = (SQLException) e;
                if (!dynamicContent.isDisposed()) {
                    DatabaseInterfaceProvider interfaceProvider = dynamicContent.getConnectionHandler().getInterfaceProvider();
                    modelException = interfaceProvider.getMessageParserInterface().isModelException(sqlException);
                }
            }
            throw new DynamicContentLoadException(e, modelException);
        } finally {
            connectionHandler.getLoadMonitor().decrementLoaderCount();
            ConnectionUtil.closeResultSet(resultSet);
            connectionHandler.freePoolConnection(connection);
        }
    }

    public void reloadContent(DynamicContent<T> dynamicContent) throws DynamicContentLoadException, InterruptedException {
        loadContent(dynamicContent, true);
    }

    public class LoaderCache {
        private String name;
        private DBObject object;
        public DBObject getObject(String name) {
            if (name.equals(this.name)) {
                return object;
            }
            return null;
        }

        public void setObject(String name, DBObject object) {
            this.name = name;
            this.object = object;
        }
    }
}
