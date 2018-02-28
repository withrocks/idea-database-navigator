package com.dci.intellij.dbn.connection;

import java.sql.SQLException;

import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.TimeUtil;

public class AuthenticationError {
    public static final int THREE_MINUTES = TimeUtil.ONE_MINUTE * 3;
    private boolean osAuthentication;
    private String user;
    private String password;
    private SQLException exception;
    private long timestamp;

    public AuthenticationError(boolean osAuthentication, String user, String password, SQLException exception) {
        this.osAuthentication = osAuthentication;
        this.user = user;
        this.password = password;
        this.exception = exception;
        timestamp = System.currentTimeMillis();
    }

    public SQLException getException() {
        return exception;
    }

    public boolean isSame(boolean osAuthentication, String user, String password) {
        return
            this.osAuthentication == osAuthentication &&
            CommonUtil.safeEqual(this.user, user) &&
            CommonUtil.safeEqual(this.password, password);
    }

    public boolean isExpired() {
        return System.currentTimeMillis()- timestamp > THREE_MINUTES;
    }
}
