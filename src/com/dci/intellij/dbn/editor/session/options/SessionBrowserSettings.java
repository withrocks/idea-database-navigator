package com.dci.intellij.dbn.editor.session.options;

import org.jdom.Element;

import com.dci.intellij.dbn.common.option.InteractiveOptionHandler;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.editor.session.options.ui.SessionBrowserSettingsForm;

public class SessionBrowserSettings extends Configuration<SessionBrowserSettingsForm> {
    public static final String REMEMBER_OPTION_HINT = ""; //"\n\n(you can remember your option and change it at any time in Settings > Operations > Session Manager)";

    private boolean reloadOnFilterChange = false;
    private InteractiveOptionHandler<SessionInterruptionOption> disconnectSessionOptionHandler =
            new InteractiveOptionHandler<SessionInterruptionOption>(
                    "disconnect-session",
                    "Disconnect Sessions",
                    "Are you sure you want to disconnect the {0} from connection {1}?\nPlease select your disconnect option." +
                            REMEMBER_OPTION_HINT,
                    SessionInterruptionOption.ASK,
                    SessionInterruptionOption.IMMEDIATE,
                    SessionInterruptionOption.POST_TRANSACTION,
                    SessionInterruptionOption.CANCEL);

    private InteractiveOptionHandler<SessionInterruptionOption> killSessionOptionHandler =
            new InteractiveOptionHandler<SessionInterruptionOption>(
                    "kill-session",
                    "Kill Sessions",
                    "Are you sure you want to kill the {0} from connection {1}?\nPlease select your kill option." +
                            REMEMBER_OPTION_HINT,
                    SessionInterruptionOption.ASK,
                    SessionInterruptionOption.NORMAL,
                    SessionInterruptionOption.IMMEDIATE,
                    SessionInterruptionOption.CANCEL);

    public String getDisplayName() {
        return "Session Browser Settings";
    }

    public String getHelpTopic() {
        return "sessionBrowser";
    }


    /*********************************************************
     *                       Settings                        *
     *********************************************************/

    public InteractiveOptionHandler<SessionInterruptionOption> getDisconnectSessionOptionHandler() {
        return disconnectSessionOptionHandler;
    }

    public InteractiveOptionHandler<SessionInterruptionOption> getKillSessionOptionHandler() {
        return killSessionOptionHandler;
    }

    public boolean isReloadOnFilterChange() {
        return reloadOnFilterChange;
    }

    public void setReloadOnFilterChange(boolean reloadOnFilterChange) {
        this.reloadOnFilterChange = reloadOnFilterChange;
    }

    /****************************************************
     *                   Configuration                  *
     ****************************************************/
    public SessionBrowserSettingsForm createConfigurationEditor() {
        return new SessionBrowserSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "session-browser";
    }

    public void readConfiguration(Element element) {
        disconnectSessionOptionHandler.readConfiguration(element);
        killSessionOptionHandler.readConfiguration(element);
        reloadOnFilterChange = SettingsUtil.getBoolean(element, "reload-on-filter-change", reloadOnFilterChange);
    }

    public void writeConfiguration(Element element) {
        disconnectSessionOptionHandler.writeConfiguration(element);
        killSessionOptionHandler.writeConfiguration(element);
        SettingsUtil.setBoolean(element, "reload-on-filter-change", reloadOnFilterChange);
    }
}
