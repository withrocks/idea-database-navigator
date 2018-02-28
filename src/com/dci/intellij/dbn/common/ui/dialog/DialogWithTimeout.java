package com.dci.intellij.dbn.common.ui.dialog;

import javax.swing.JComponent;
import java.util.Timer;
import java.util.TimerTask;

import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.intellij.openapi.project.Project;

public abstract class DialogWithTimeout extends DBNDialog<DialogWithTimeoutForm>{
    private Timer timeoutTimer;
    private int secondsLeft;

    protected DialogWithTimeout(Project project, String title, boolean canBeParent, int timeoutSeconds) {
        super(project, title, canBeParent);
        secondsLeft = timeoutSeconds;
        component = new DialogWithTimeoutForm(secondsLeft);
        timeoutTimer = new Timer("DBN Timeout dialog task [" + getProject().getName() + "]");
        timeoutTimer.schedule(new TimeoutTask(), TimeUtil.ONE_SECOND, TimeUtil.ONE_SECOND);
    }

    @Override
    protected void init() {
        component.setContentComponent(createContentComponent());
        super.init();
    }

    private class TimeoutTask extends TimerTask {
        public void run() {
            if (secondsLeft > 0) {
                secondsLeft = secondsLeft -1;
                component.updateTimeLeft(secondsLeft);
                if (secondsLeft == 0) {
                    new SimpleLaterInvocator() {
                        @Override
                        public void execute() {
                            doDefaultAction();
                        }
                    }.start();

                }
            }
        }
    }

    protected abstract JComponent createContentComponent();

    public abstract void doDefaultAction();

    @Override
    public void dispose() {
        if (!isDisposed()) {
            super.dispose();
            timeoutTimer.cancel();
            timeoutTimer.purge();
        }
    }

}
