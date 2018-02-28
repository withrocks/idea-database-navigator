package com.dci.intellij.dbn.connection;

import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.thread.SimpleTask;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;

public abstract class ConnectionAction extends SimpleTask {
    public static final String[] OPTIONS_CONNECT_CANCEL = new String[]{"Connect", "Cancel"};

    private ConnectionHandler connectionHandler;
    private ConnectionProvider connectionProvider;
    private boolean executeInBackground;

    public ConnectionAction(ConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
    }

    public ConnectionAction(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    public final void start() {
        Application application = ApplicationManager.getApplication();
        if (application.isDispatchThread()) {
            run();
        } else {
            application.invokeLater(this/*, ModalityState.NON_MODAL*/);
        }
    }

    public final void run() {
        final ConnectionHandler connectionHandler = getConnectionHandler();

        if (connectionHandler != null && !connectionHandler.isDisposed()) {

            if (connectionHandler.isVirtual() || connectionHandler.canConnect()) {
                execute();
            } else {
                MessageUtil.showInfoDialog(
                        connectionHandler.getProject(),
                        "Not Connected to Database",
                        "You are not connected to database \"" + connectionHandler.getName() + "\". \n" +
                                "If you want to continue with this operation, you need to connect.",
                        OPTIONS_CONNECT_CANCEL, 0,
                        new SimpleTask() {
                            @Override
                            public void execute() {
                                if (getResult() == 0) {
                                    connectionHandler.setAllowConnection(true);
                                    ConnectionAction.this.execute();
                                }
                            }
                        });
            }
        }
    }

    @Nullable
    public ConnectionHandler getConnectionHandler() {
        if (connectionHandler == null && connectionProvider != null) {
            connectionHandler = connectionProvider.getConnectionHandler();
        }
        return connectionHandler;
    }

    public abstract void execute();
}
