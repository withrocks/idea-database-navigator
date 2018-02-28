package com.dci.intellij.dbn.connection.config.action;

import javax.swing.JList;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.ArrayUtils;
import org.apache.xmlbeans.impl.common.ReaderInputStream;
import org.jdom.Document;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.ClipboardUtil;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.connection.config.ConnectionBundleSettings;
import com.dci.intellij.dbn.connection.config.ConnectionDatabaseSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.dci.intellij.dbn.connection.config.ui.ConnectionListModel;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;

public class PasteConnectionAction extends DumbAwareAction {
    protected ConnectionBundleSettings connectionBundleSettings;
    protected JList list;

    public PasteConnectionAction(JList list, ConnectionBundleSettings connectionBundleSettings) {
        super("Paste configuration from clipboard", null, Icons.CONNECTION_PASTE);
        this.list = list;
        this.connectionBundleSettings = connectionBundleSettings;
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        try {
            String clipboardData = ClipboardUtil.getStringContent();
            if (clipboardData != null) {
                Document xmlDocument = CommonUtil.createXMLDocument(new ReaderInputStream(new StringReader(clipboardData), "UTF-8"));
                if (xmlDocument != null) {
                    Element rootElement = xmlDocument.getRootElement();
                    List<Element> configElements = rootElement.getChildren();
                    ConnectionListModel model = (ConnectionListModel) list.getModel();
                    int selectedIndex = list.getSelectedIndex();
                    List<Integer> selectedIndexes = new ArrayList<Integer>();
                    for (Element configElement : configElements) {
                        selectedIndex++;
                        ConnectionSettings clone = new ConnectionSettings(connectionBundleSettings);
                        clone.readConfiguration(configElement);
                        clone.setNew(true);
                        clone.generateNewId();

                        ConnectionDatabaseSettings databaseSettings = clone.getDatabaseSettings();
                        String name = databaseSettings.getName();
                        while (model.getConnectionConfig(name) != null) {
                            name = NamingUtil.getNextNumberedName(name, true);
                        }
                        databaseSettings.setName(name);
                        model.add(selectedIndex, clone);
                        selectedIndexes.add(selectedIndex);
                        connectionBundleSettings.setModified(true);
                    }

                    list.setSelectedIndices(ArrayUtils.toPrimitive(selectedIndexes.toArray(new Integer[selectedIndexes.size()])));
                }
            }
        } catch (Exception ex) {

        }
    }

    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Object clipboardData = clipboard.getData(DataFlavor.stringFlavor);
            if (clipboardData instanceof String) {
                String clipboardString = (String) clipboardData;
                presentation.setEnabled(clipboardString.contains("connection-configurations"));
            } else {
                presentation.setEnabled(false);
            }
        } catch (Exception ex) {
            presentation.setEnabled(false);
        }

    }
}
