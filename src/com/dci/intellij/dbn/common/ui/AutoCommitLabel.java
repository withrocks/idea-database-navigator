package com.dci.intellij.dbn.common.ui;

import javax.swing.JLabel;
import java.awt.Color;

import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.thread.ConditionalLaterInvocator;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.ConnectionStatusListener;
import com.dci.intellij.dbn.connection.VirtualConnectionHandler;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;

public class AutoCommitLabel extends JLabel implements Disposable {
    private interface Colors {
        Color DISCONNECTED = new JBColor(new Color(0x454545), new Color(0x808080));
        Color AUTO_COMMIT_ON = new JBColor(new Color(0xFF0000), new Color(0xBC3F3C));
        Color AUTO_COMMIT_OFF = new JBColor(new Color(0x009600), new Color(0x629755));
    }
    private ConnectionHandlerRef connectionHandler;
    private boolean subscribed = false;

    public AutoCommitLabel() {
        super("");
        setFont(GUIUtil.BOLD_FONT);
    }

    public void setConnectionHandler(ConnectionHandler connectionHandler) {
        if (connectionHandler == null || connectionHandler instanceof VirtualConnectionHandler) {
            this.connectionHandler = null;
        } else {
            this.connectionHandler = connectionHandler.getRef();
            if (!subscribed) {
                subscribed = true;
                Project project = connectionHandler.getProject();
                EventManager.subscribe(project, ConnectionStatusListener.TOPIC, connectionStatusListener);
            }
        }
        update();
    }

    private void update() {
        new ConditionalLaterInvocator() {
            @Override
            public void execute() {
                ConnectionHandler connectionHandler = getConnectionHandler();
                if (connectionHandler != null) {
                    setVisible(true);
                    boolean disconnected = !connectionHandler.isConnected();
                    boolean autoCommit = connectionHandler.isAutoCommit();
                    setText(disconnected ? "Not connected to database" : autoCommit ? "Auto-Commit ON" : "Auto-Commit OFF");
                    setForeground(disconnected ?
                            Colors.DISCONNECTED : autoCommit ?
                            Colors.AUTO_COMMIT_ON :
                            Colors.AUTO_COMMIT_OFF);
                    setToolTipText(
                            disconnected ? "The connection to database has been closed. No editing possible" :
                                    autoCommit ?
                                            "Auto-Commit is enabled for connection \"" + connectionHandler + "\". Data changes will be automatically committed to the database." :
                                            "Auto-Commit is disabled for connection \"" + connectionHandler + "\". Data changes will need to be manually committed to the database.");
                } else {
                    setVisible(false);
                }
            }
        }.start();
    }

    private ConnectionHandler getConnectionHandler() {
        return this.connectionHandler == null ? null : this.connectionHandler.get();
    }

    private ConnectionStatusListener connectionStatusListener = new ConnectionStatusListener() {
        @Override
        public void statusChanged(String connectionId) {
            ConnectionHandler connectionHandler = getConnectionHandler();
            if (connectionHandler != null && connectionHandler.getId().equals(connectionId)) {
                update();
            }
        }

    };
    @Override
    public void dispose() {
        EventManager.unsubscribe(connectionStatusListener);
    }


}
