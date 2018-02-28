package com.dci.intellij.dbn.execution.logging.ui;

import java.io.StringReader;
import java.util.Date;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.locale.Formatter;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.intellij.diagnostic.logging.DefaultLogFilterModel;
import com.intellij.diagnostic.logging.LogConsoleBase;
import com.intellij.diagnostic.logging.LogFilterModel;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.actionSystem.ActionGroup;

public class DatabaseLogOutputConsole extends LogConsoleBase{
    public static final StringReader EMPTY_READER = new StringReader("");
    private ConnectionHandlerRef connectionHandlerRef;
    public DatabaseLogOutputConsole(@NotNull ConnectionHandler connectionHandler, String title, boolean buildInActions) {
        super(connectionHandler.getProject(), EMPTY_READER, title, buildInActions, createFilterModel(connectionHandler));
        connectionHandlerRef = connectionHandler.getRef();
    }

    private static LogFilterModel createFilterModel(ConnectionHandler connectionHandler) {
        DefaultLogFilterModel defaultLogFilterModel = new DefaultLogFilterModel(connectionHandler.getProject());
        defaultLogFilterModel.setCheckStandartFilters(false);
        return defaultLogFilterModel;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    public ConnectionHandler getConnectionHandler() {
        return ConnectionHandlerRef.get(connectionHandlerRef);
    }

    public void writeToConsole(String text) {
        ConnectionHandler connectionHandler = getConnectionHandler();
        if (connectionHandler != null && !connectionHandler.isDisposed() && StringUtil.isNotEmptyOrSpaces(text)) {
            Formatter formatter = Formatter.getInstance(connectionHandler.getProject());
            String date = formatter.formatDateTime(new Date());

            String headline = connectionHandler.getName() + " - " + date + "\n";
            writeToConsole(headline, ProcessOutputTypes.SYSTEM);
            writeToConsole(text, ProcessOutputTypes.STDOUT);
        }
    }

    @Override
    public ActionGroup getOrCreateActions() {
        return super.getOrCreateActions();
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
