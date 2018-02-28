package com.dci.intellij.dbn.connection.config.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.util.ClipboardUtil;
import com.dci.intellij.dbn.connection.config.ConnectionBundleSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAwareAction;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jetbrains.annotations.NotNull;

import javax.swing.JList;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;

public class CopyConnectionsAction extends DumbAwareAction {
    private static final Logger LOGGER = LoggerFactory.createLogger();
    protected ConnectionBundleSettings connectionBundleSettings;
    protected JList list;

    public CopyConnectionsAction(JList list, ConnectionBundleSettings connectionBundleSettings) {
        super("Copy configurations to clipboard", null, Icons.CONNECTION_COPY);
        this.list = list;
        this.connectionBundleSettings = connectionBundleSettings;
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        Object[] configurations = list.getSelectedValues();
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Element rootElement = new Element("connection-configurations");
            for (Object o : configurations) {
                ConnectionSettings configuration = (ConnectionSettings) o;
                Element configElement = new Element("config");
                configuration.writeConfiguration(configElement);
                rootElement.addContent(configElement);
            }

            Document document = new Document(rootElement);
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            String xmlString = outputter.outputString(document);
            clipboard.setContents(ClipboardUtil.createXmlContent(xmlString), null);
        } catch (Exception ex) {
            LOGGER.error("Could not copy database configuration to clipboard", ex);
        }
    }

    public void update(@NotNull AnActionEvent e) {
        int length = list.getSelectedValues().length;
        e.getPresentation().setEnabled(length > 0);
    }
}
