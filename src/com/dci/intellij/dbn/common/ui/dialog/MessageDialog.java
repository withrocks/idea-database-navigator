package com.dci.intellij.dbn.common.ui.dialog;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.Component;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.thread.ConditionalLaterInvocator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.uiDesigner.core.GridConstraints;

public class MessageDialog extends DialogBuilder {
    private JTextArea messageTextArea;
    private JLabel messageLabel;
    private JPanel mainPanel;
    private JPanel detailedMessagePanel;
    private JCheckBox ignoreCheckBox;
    private JPanel customComponentPanel;

    public MessageDialog(Project project) {
        super(project);
    }

    public static boolean showInfoDialog(Project project, String message, String extendedMessage, boolean allowIgnore) {
        return showDialog(project, Icons.DIALOG_INFORMATION, "Information", message, extendedMessage, allowIgnore);
    }

    public static boolean showInfoDialog(Project project, String message, Component component) {
        return showDialog(project, Icons.DIALOG_INFORMATION, "Information", message, component);
    }

    public static boolean showWarningDialog(Project project, String message, String extendedMessage, boolean allowIgnore) {
        return showDialog(project, Icons.DIALOG_WARNING, "Warning", message, extendedMessage, allowIgnore);
    }

    public static boolean showErrorDialog(Project project, String message, String extendedMessage, boolean allowIgnore) {
        return showDialog(project, Icons.DIALOG_ERROR, "Error", message, extendedMessage, allowIgnore);
    }


    private static boolean showDialog(
            final Project project,
            final Icon icon,
            final String title,
            final String message,
            final String extendedMessage,
            final boolean enableIgnore) {

        new ConditionalLaterInvocator() {
            public void execute() {
                createDialog(project, icon, title, message, extendedMessage, enableIgnore);
            }
        }.start();

        return false;
    }

    private static boolean showDialog(
            final Project project,
            final Icon icon,
            final String title,
            final String message,
            final Component component) {

        new ConditionalLaterInvocator() {
            public void execute() {
                createDialog(project, icon, title, message, component);
            }
        }.start();

        return false;
    }

    private static boolean createDialog(Project project, Icon icon, String title, String message, String extendedMessage, boolean enableIgnore) {
        MessageDialog messageDialog = new MessageDialog(project);
        messageDialog.setTitle(title);
        messageDialog.messageLabel.setIcon(icon);
        messageDialog.messageLabel.setText(message);
        messageDialog.customComponentPanel.setVisible(false);
        messageDialog.detailedMessagePanel.setVisible(extendedMessage != null);
        messageDialog.messageTextArea.setText(extendedMessage);
        messageDialog.ignoreCheckBox.setVisible(enableIgnore);
        messageDialog.messageTextArea.setBackground(messageDialog.mainPanel.getBackground());
        messageDialog.messageTextArea.setFont(messageDialog.mainPanel.getFont());
        messageDialog.setCenterPanel(messageDialog.mainPanel);
        messageDialog.removeAllActions();
        messageDialog.addOkAction();
        messageDialog.showModal(true);
        messageDialog.setButtonsAlignment(SwingUtilities.CENTER);
        return messageDialog.ignoreCheckBox.isSelected();
    }

    private static boolean createDialog(Project project, Icon icon, String title, String message, Component component) {
        MessageDialog messageDialog = new MessageDialog(project);
        messageDialog.setTitle(title);
        messageDialog.messageLabel.setIcon(icon);
        messageDialog.messageLabel.setText(message);
        messageDialog.customComponentPanel.setVisible(true);
        messageDialog.customComponentPanel.add(component, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        messageDialog.detailedMessagePanel.setVisible(false);
        messageDialog.ignoreCheckBox.setVisible(false);
        messageDialog.messageTextArea.setBackground(messageDialog.mainPanel.getBackground());
        messageDialog.messageTextArea.setFont(messageDialog.mainPanel.getFont());
        messageDialog.setCenterPanel(messageDialog.mainPanel);
        messageDialog.removeAllActions();
        messageDialog.addOkAction();
        messageDialog.showModal(true);
        messageDialog.setButtonsAlignment(SwingUtilities.CENTER);
        return messageDialog.ignoreCheckBox.isSelected();
    }

}
