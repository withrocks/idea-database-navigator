package com.dci.intellij.dbn.common.notification;

import javax.swing.event.HyperlinkEvent;
import java.text.MessageFormat;
import org.jetbrains.annotations.NotNull;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;

public class NotificationUtil {

    public static void sendInfoNotification(Project project, String title, String message, String ... args) {
        sendNotification(project, NotificationType.INFORMATION, title, message, args);
    }

    public static void sendWarningNotification(Project project, String title, String message, String ... args) {
        sendNotification(project, NotificationType.WARNING, title, message, args);
    }

    public static void sendErrorNotification(Project project, String title, String message, String ... args) {
        sendNotification(project, NotificationType.ERROR, title, message, args);
    }

    public static void sendNotification(Project project, NotificationType type, String title, String message, String ... args) {
        if (project != null && !project.isDisposed()) {
            final NotificationListener listener = new NotificationListener() {
                public void hyperlinkUpdate(@NotNull Notification notification, @NotNull HyperlinkEvent event) {
                    notification.expire();
                }
            };

            message = MessageFormat.format(message, args);
            Notification notification = new Notification("Database Navigator", title, message, type);
            Notifications.Bus.notify(notification, project);
        }
    }
}
