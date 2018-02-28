package com.dci.intellij.dbn.connection;

public class ConnectionStatus {
    private boolean connected = false;
    private boolean valid = true;
    private boolean resolvingIdleStatus;
    private String statusMessage;
    private AuthenticationError authenticationError;

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public boolean isResolvingIdleStatus() {
        return resolvingIdleStatus;
    }

    public void setResolvingIdleStatus(boolean resolvingIdleStatus) {
        this.resolvingIdleStatus = resolvingIdleStatus;
    }

    public AuthenticationError getAuthenticationError() {
        return authenticationError;
    }

    public void setAuthenticationError(AuthenticationError authenticationError) {
        this.authenticationError = authenticationError;
    }
}
