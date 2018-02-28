package com.dci.intellij.dbn.connection.config.ui;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.lang.ArrayUtils;
import org.apache.xmlbeans.impl.common.ReaderInputStream;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.options.SettingsChangeNotifier;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.common.util.ClipboardUtil;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerImpl;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.config.ConnectionBundleSettings;
import com.dci.intellij.dbn.connection.config.ConnectionBundleSettingsListener;
import com.dci.intellij.dbn.connection.config.ConnectionConfigListCellRenderer;
import com.dci.intellij.dbn.connection.config.ConnectionDatabaseSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.dci.intellij.dbn.connection.config.GenericConnectionDatabaseSettings;
import com.dci.intellij.dbn.data.sorting.SortDirection;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.ui.GuiUtils;
import com.intellij.ui.ListUtil;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;

public class ConnectionBundleSettingsForm extends ConfigurationEditorForm<ConnectionBundleSettings> implements ListSelectionListener {
    private static final Logger LOGGER = LoggerFactory.createLogger();
    private static final String BLANK_PANEL_ID = "BLANK_PANEL";

    private JPanel mainPanel;
    private JPanel connectionSetupPanel;
    private JPanel connectionListPanel;
    private JPanel actionsPanel;
    private JList connectionsList;

    private String currentPanelId;

    
    private Map<String, ConnectionSettingsForm> cachedForms = new HashMap<String, ConnectionSettingsForm>();

    public JList getList() {
        return connectionsList;
    }

    public ConnectionBundleSettingsForm(ConnectionBundleSettings configuration) {
        super(configuration);
        ConnectionBundle connectionBundle = configuration.getConnectionBundle();
        connectionsList = new JBList(new ConnectionListModel(connectionBundle));
        connectionsList.addListSelectionListener(this);
        connectionsList.setCellRenderer(new ConnectionConfigListCellRenderer());
        connectionsList.setFont(com.intellij.util.ui.UIUtil.getLabelFont());


        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(connectionsList);
        decorator.setAddAction(addAction);
        decorator.setRemoveAction(removeAction);
        decorator.addExtraAction(duplicateAction);
        decorator.setMoveUpAction(moveUpAction);
        decorator.setMoveDownAction(moveDownAction);
        //decorator.addExtraAction(sortAction);
        decorator.addExtraAction(copyAction);
        decorator.addExtraAction(pasteAction);

        this.connectionListPanel.add(decorator.createPanel(), BorderLayout.CENTER);

        if (connectionBundle.getConnectionHandlers().size() > 0) {
            selectConnection(connectionBundle.getConnectionHandlers().get(0));
        }
        JPanel emptyPanel = new JPanel();
        connectionSetupPanel.setPreferredSize(new Dimension(500, -1));
        connectionSetupPanel.add(emptyPanel, BLANK_PANEL_ID);
        GuiUtils.replaceJSplitPaneWithIDEASplitter(mainPanel);
        GUIUtil.updateSplitterProportion(mainPanel, (float) 0.3);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyFormChanges() throws ConfigurationException {
        ConnectionBundleSettings connectionBundleSettings = getConfiguration();
        final ConnectionBundle connectionBundle = connectionBundleSettings.getConnectionBundle();

        List<ConnectionHandler> oldConnections = new ArrayList<ConnectionHandler>(connectionBundle.getConnectionHandlers().getFullList());
        List<ConnectionHandler> newConnections = new ArrayList<ConnectionHandler>();

        final AtomicBoolean listChanged = new AtomicBoolean(false);
        ConnectionListModel listModel = (ConnectionListModel) connectionsList.getModel();
        if (oldConnections.size() == listModel.getSize()) {
            for (int i=0; i<oldConnections.size(); i++) {
                ConnectionSettings oldConfig = oldConnections.get(i).getSettings();
                ConnectionSettings newConfig = ((ConnectionSettings) listModel.get(i));
                if (!oldConfig.getConnectionId().equals(newConfig.getConnectionId()) ||
                        (newConfig.getSettingsEditor() != null && newConfig.getDatabaseSettings().getSettingsEditor().isConnectionActive() != oldConfig.getDatabaseSettings().isActive())) {
                    listChanged.set(true);
                    break;
                }
            }
        } else {
            listChanged.set(true);
        }

        for (int i=0; i< listModel.getSize(); i++) {
            ConnectionSettings connectionSettings = (ConnectionSettings) listModel.getElementAt(i);
            connectionSettings.apply();

            ConnectionHandler connectionHandler = connectionBundle.getConnection(connectionSettings.getConnectionId());
            if (connectionHandler == null) {
                connectionHandler = new ConnectionHandlerImpl(connectionBundle, connectionSettings);
                connectionSettings.setNew(false);
            } else {
                oldConnections.remove(connectionHandler);
                ((ConnectionHandlerImpl)connectionHandler).setConnectionConfig(connectionSettings);
            }

            newConnections.add(connectionHandler);

        }
        connectionBundle.setConnectionHandlers(newConnections);


        // dispose old list
        if (oldConnections.size() > 0) {
            ConnectionManager connectionManager = ConnectionManager.getInstance(connectionBundle.getProject());
            connectionManager.disposeConnections(oldConnections);
        }

         new SettingsChangeNotifier() {
            @Override
            public void notifyChanges() {
                if (listChanged.get()) {
                    Project project = connectionBundle.getProject();
                    ConnectionBundleSettingsListener listener = EventManager.notify(project, ConnectionBundleSettingsListener.TOPIC);
                    if (listener != null) listener.settingsChanged();
                }
            }
        };
    }

    public void resetFormChanges() {
        ConnectionListModel listModel = (ConnectionListModel) connectionsList.getModel();
        for (int i=0; i< listModel.getSize(); i++) {
            ConnectionSettings connectionSettings = (ConnectionSettings) listModel.getElementAt(i);
            connectionSettings.reset();
        }
    }

    public void selectConnection(@Nullable ConnectionHandler connectionHandler) {
        if (connectionHandler != null) {
            connectionsList.setSelectedValue(connectionHandler.getSettings(), true);
        }
    }

    public void valueChanged(ListSelectionEvent listSelectionEvent) {
        try {
            Object[] selectedValues = connectionsList.getSelectedValues();
            if (selectedValues.length == 1) {
                ConnectionSettings connectionSettings = (ConnectionSettings) selectedValues[0];
                switchSettingsPanel(connectionSettings);
            } else {
                switchSettingsPanel(null);
            }
        } catch (IndexOutOfBoundsException e) {
            // fixme find out why
        }
    }

    @Override
    public void dispose() {
        for (ConnectionSettingsForm settingsForm : cachedForms.values()) {
            Disposer.dispose(settingsForm);
        }
        cachedForms.clear();
        super.dispose();
    }

    private void switchSettingsPanel(ConnectionSettings connectionSettings) {
        CardLayout cardLayout = (CardLayout) connectionSetupPanel.getLayout();
        if (connectionSettings == null) {
            cardLayout.show(connectionSetupPanel, BLANK_PANEL_ID);
        } else {

            ConnectionSettingsForm currentForm = cachedForms.get(currentPanelId);
            String selectedTabName = currentForm == null ? null : currentForm.getSelectedTabName();

            currentPanelId = connectionSettings.getConnectionId();
            if (!cachedForms.keySet().contains(currentPanelId)) {
                JComponent setupPanel = connectionSettings.createComponent();
                this.connectionSetupPanel.add(setupPanel, currentPanelId);
                cachedForms.put(currentPanelId, connectionSettings.getSettingsEditor());
            }

            ConnectionSettingsForm settingsEditor = connectionSettings.getSettingsEditor();
            if (settingsEditor != null) {
                settingsEditor.selectTab(selectedTabName);
            }

            cardLayout.show(connectionSetupPanel, currentPanelId);
        }
    }


    private AnActionButtonRunnable addAction = new AnActionButtonRunnable() {
        @Override
        public void run(AnActionButton anActionButton) {
            ConnectionBundleSettings connectionBundleSettings = getConfiguration();
            connectionBundleSettings.setModified(true);
            ConnectionSettings connectionSettings = new ConnectionSettings(connectionBundleSettings);
            connectionSettings.setNew(true);
            connectionSettings.generateNewId();

            String name = "Connection";
            ConnectionListModel model = (ConnectionListModel) connectionsList.getModel();
            while (model.getConnectionConfig(name) != null) {
                name = NamingUtil.getNextNumberedName(name, true);
            }
            GenericConnectionDatabaseSettings connectionConfig = (GenericConnectionDatabaseSettings) connectionSettings.getDatabaseSettings();
            connectionConfig.setName(name);
            int selectedIndex = connectionsList.getSelectedIndex() + 1;
            model.add(selectedIndex, connectionSettings);
            connectionsList.setSelectedIndex(selectedIndex);
        }
    };

    private AnActionButton duplicateAction = new AnActionButton("Duplicate connection", Icons.ACTION_COPY) {
        @Override
        public void actionPerformed(AnActionEvent anActionEvent) {
            getConfiguration().setModified(true);
            ConnectionSettings connectionSettings = (ConnectionSettings) connectionsList.getSelectedValue();
            ConnectionListModel model = (ConnectionListModel) connectionsList.getModel();
            ConnectionSettings clone = connectionSettings.clone();
            clone.setNew(true);
            String name = clone.getDatabaseSettings().getName();
            while (model.getConnectionConfig(name) != null) {
                name = NamingUtil.getNextNumberedName(name, true);
            }
            clone.getDatabaseSettings().setName(name);
            int selectedIndex = connectionsList.getSelectedIndex() + 1;
            model.add(selectedIndex, clone);
            connectionsList.setSelectedIndex(selectedIndex);
        }
    };

    private AnActionButtonRunnable removeAction = new AnActionButtonRunnable() {
        @Override
        public void run(AnActionButton anActionButton) {
            getConfiguration().setModified(true);
            ListUtil.removeSelectedItems(connectionsList);
        }
    };

    private AnActionButtonRunnable moveUpAction = new AnActionButtonRunnable() {
        @Override
        public void run(AnActionButton anActionButton) {
            getConfiguration().setModified(true);
            ListUtil.moveSelectedItemsUp(connectionsList);
        }
    };

    private AnActionButtonRunnable moveDownAction = new AnActionButtonRunnable() {
        @Override
        public void run(AnActionButton anActionButton) {
            getConfiguration().setModified(true);
            ListUtil.moveSelectedItemsDown(connectionsList);
        }
    };

    private AnActionButton sortAction = new AnActionButton() {
        private SortDirection currentSortDirection = SortDirection.ASCENDING;

        @Override
        public void actionPerformed(AnActionEvent anActionEvent) {
            currentSortDirection = currentSortDirection == SortDirection.ASCENDING ?
                    SortDirection.DESCENDING :
                    SortDirection.ASCENDING;

            if (connectionsList.getModel().getSize() > 0) {
                Object selectedValue = connectionsList.getSelectedValue();
                ConnectionListModel model = (ConnectionListModel) connectionsList.getModel();
                model.sort(currentSortDirection);
                connectionsList.setSelectedValue(selectedValue, true);
                getConfiguration().setModified(true);
            }
        }

        @Override
        public void updateButton(AnActionEvent e) {
            Icon icon;
            String text;
            if (currentSortDirection != SortDirection.ASCENDING) {
                icon = Icons.ACTION_SORT_ASC;
                text = "Sort list ascending";
            } else {
                icon = Icons.ACTION_SORT_DESC;
                text = "Sort list descending";
            }
            Presentation presentation = e.getPresentation();
            presentation.setIcon(icon);
            presentation.setText(text);
        }
    };


    private AnActionButton copyAction = new AnActionButton("Copy configurations to clipboard", Icons.CONNECTION_COPY) {
        @Override
        public void actionPerformed(AnActionEvent anActionEvent) {
            Object[] configurations = connectionsList.getSelectedValues();
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
    };

    AnActionButton pasteAction = new AnActionButton("Paste configuration from clipboard", Icons.CONNECTION_PASTE) {
        @Override
        public void actionPerformed(AnActionEvent anActionEvent) {
            try {
                String clipboardData = ClipboardUtil.getStringContent();
                if (clipboardData != null) {
                    Document xmlDocument = CommonUtil.createXMLDocument(new ReaderInputStream(new StringReader(clipboardData), "UTF-8"));
                    if (xmlDocument != null) {
                        Element rootElement = xmlDocument.getRootElement();
                        List<Element> configElements = rootElement.getChildren();
                        ConnectionListModel model = (ConnectionListModel) connectionsList.getModel();
                        int selectedIndex = connectionsList.getSelectedIndex();
                        List<Integer> selectedIndexes = new ArrayList<Integer>();
                        ConnectionBundleSettings configuration = getConfiguration();
                        for (Element configElement : configElements) {
                            selectedIndex++;
                            ConnectionSettings clone = new ConnectionSettings(configuration);
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
                            configuration.setModified(true);
                        }

                        connectionsList.setSelectedIndices(ArrayUtils.toPrimitive(selectedIndexes.toArray(new Integer[selectedIndexes.size()])));

                    }
                }
            } catch (Exception ex) {
                LOGGER.error("Could not paste database configuration from clipboard", ex);
            }
        }

        @Override
        public void updateButton(AnActionEvent e) {
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
    };
}
