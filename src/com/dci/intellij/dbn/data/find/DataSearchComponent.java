package com.dci.intellij.dbn.data.find;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.regex.Pattern;

import com.dci.intellij.dbn.common.compatibility.CompatibilityUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.data.find.action.CloseOnESCAction;
import com.dci.intellij.dbn.data.find.action.NextOccurrenceAction;
import com.dci.intellij.dbn.data.find.action.PrevOccurrenceAction;
import com.dci.intellij.dbn.data.find.action.ShowHistoryAction;
import com.dci.intellij.dbn.data.find.action.ToggleMatchCase;
import com.dci.intellij.dbn.data.find.action.ToggleRegex;
import com.dci.intellij.dbn.data.find.action.ToggleWholeWordsOnlyAction;
import com.dci.intellij.dbn.data.grid.ui.table.basic.BasicTable;
import com.dci.intellij.dbn.data.model.DataModel;
import com.dci.intellij.dbn.data.model.DataModelListener;
import com.dci.intellij.dbn.data.model.basic.BasicDataModel;
import com.intellij.featureStatistics.FeatureUsageTracker;
import com.intellij.find.FindManager;
import com.intellij.find.FindModel;
import com.intellij.find.FindSettings;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.event.SelectionEvent;
import com.intellij.openapi.editor.event.SelectionListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.LightColors;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.panels.NonOpaquePanel;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ui.UIUtil;

public class DataSearchComponent extends JPanel implements Disposable, SelectionListener, DataSearchResultListener, DataModelListener {
    private static final int MATCHES_LIMIT = 10000;
    private final Color BACKGROUND;
    private final Color GRADIENT_C1;
    private final Color GRADIENT_C2;

    private static final Color BORDER_COLOR = new Color(0x87, 0x87, 0x87);
    public static final Color COMPLETION_BACKGROUND_COLOR = new Color(235, 244, 254);
    private static final Color FOCUS_CATCHER_COLOR = new Color(0x9999ff);

    private JComponent toolbarComponent;
    private JLabel matchInfoLabel;
    private JTextField searchField;
    private ActionToolbar actionsToolbar;
    private SearchableDataComponent searchableComponent;

    private boolean myListeningSelection = false;
    private DataFindModel findModel;
    private DataSearchResultController searchResultController;

    public JTextField getSearchField() {
        return searchField;
    }

    public DataSearchComponent(final SearchableDataComponent searchableComponent) {
        super(new BorderLayout(0, 0));
        this.searchableComponent = searchableComponent;
        BasicTable<? extends BasicDataModel> table = searchableComponent.getTable();
        DataModel dataModel = table.getModel();
        dataModel.addDataModelListener(this);
        initializeFindModel();

        findModel = new DataFindModel();
        DataSearchResult searchResult = dataModel.getSearchResult();
        searchResult.setMatchesLimit(MATCHES_LIMIT);
        searchResultController = new DataSearchResultController(searchableComponent);
        searchResult.addListener(this);
        searchResultController.updateResult(findModel);

        Disposer.register(this, searchResultController);


        GRADIENT_C1 = getBackground();
        GRADIENT_C2 = new Color(Math.max(0, GRADIENT_C1.getRed() - 0x18), Math.max(0, GRADIENT_C1.getGreen() - 0x18), Math.max(0, GRADIENT_C1.getBlue() - 0x18));
        BACKGROUND = UIUtil.getTextFieldBackground();

        configureLeadPanel();

        findModel.addObserver(new DataFindModel.FindModelObserver() {
            @Override
            public void findModelChanged(FindModel findModel) {
                String stringToFind = findModel.getStringToFind();
                if (!wholeWordsApplicable(stringToFind)) {
                    findModel.setWholeWordsOnly(false);
                }
                updateUIWithFindModel();
                updateResults(true);
                FindManager findManager = getFindManager();
                syncFindModels(findManager.getFindInFileModel(), DataSearchComponent.this.findModel);
            }
        });

        updateUIWithFindModel();
        //new CloseOnESCAction(this, table);
        new PrevOccurrenceAction(this, table, false);
        new NextOccurrenceAction(this, table, false);
        searchableComponent.getTable().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!e.isConsumed()) {
                    int keyChar = e.getKeyChar();
                    if (keyChar == 27) { // ESCAPE
                        searchableComponent.hideSearchHeader();
                    }
                }
            }
        });
    }

    @Override
    public void modelChanged() {
        searchResultController.updateResult(findModel);
    }

    @Override
    public void searchResultUpdated(DataSearchResult searchResult) {
        int count = searchResult.size();
        if (searchField.getText().isEmpty()) {
            updateUIWithEmptyResults();
        } else {
            if (count <= searchResult.getMatchesLimit()) {
                if (count > 0) {
                    setRegularBackground();
                    if (count > 1) {
                        matchInfoLabel.setText(count + " matches");
                    } else {
                        matchInfoLabel.setText("1 match");
                    }
                } else {
                    setNotFoundBackground();
                    matchInfoLabel.setText("No matches");
                }
            } else {
                setRegularBackground();
                matchInfoLabel.setText("More than " + searchResult.getMatchesLimit() + " matches");
                boldMatchInfo();
            }
        }
    }

    public void initializeFindModel() {
        if (findModel == null) {
            findModel = new DataFindModel();
            FindManager findManager = getFindManager();
            findModel.copyFrom(findManager.getFindInFileModel());
            findModel.setPromptOnReplace(false);
        }
/*
        String stringToFind = searchableComponent.getSelectedText();
        findModel.setStringToFind(StringUtil.isEmpty(stringToFind) ? "" : stringToFind);
*/
    }
    
    public void resetFindModel() {
        if (findModel != null) {
            findModel.setStringToFind("");
        }
    }

    private void configureLeadPanel() {
        JPanel myLeadPanel = createLeadPane();
        add(myLeadPanel, BorderLayout.WEST);
        searchField = createTextField(myLeadPanel);
        setupSearchFieldListener();

        DefaultActionGroup myActionsGroup = new DefaultActionGroup("search bar", false);
        myActionsGroup.add(new ShowHistoryAction(searchField, this));
        myActionsGroup.add(new PrevOccurrenceAction(this, searchField, true));
        myActionsGroup.add(new NextOccurrenceAction(this, searchField, true));
        //myActionsGroup.add(new FindAllAction(this));
        myActionsGroup.add(new ToggleMatchCase(this));
        myActionsGroup.add(new ToggleRegex(this));

        actionsToolbar = ActionManager.getInstance().createActionToolbar("SearchBar", myActionsGroup, true);

        myActionsGroup.addAction(new ToggleWholeWordsOnlyAction(this));

        actionsToolbar.setLayoutPolicy(ActionToolbar.AUTO_LAYOUT_POLICY);
        toolbarComponent = actionsToolbar.getComponent();
        toolbarComponent.setBorder(null);
        toolbarComponent.setOpaque(false);

        myLeadPanel.add(toolbarComponent);

        JPanel tailPanel = new NonOpaquePanel(new BorderLayout(5, 0));
        JPanel tailContainer = new NonOpaquePanel(new BorderLayout(5, 0));
        tailContainer.add(tailPanel, BorderLayout.EAST);
        add(tailContainer, BorderLayout.CENTER);

        matchInfoLabel = new JLabel();
        setSmallerFontAndOpaque(matchInfoLabel);


        JLabel closeLabel = new JLabel(" ", IconLoader.getIcon("/actions/cross.png"), SwingConstants.RIGHT);
        closeLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                close();
            }
        });

        closeLabel.setToolTipText("Close search bar (Escape)");

        JPanel labelsPanel = new NonOpaquePanel(new FlowLayout());

        labelsPanel.add(matchInfoLabel);
        tailPanel.add(labelsPanel, BorderLayout.CENTER);
        tailPanel.add(closeLabel, BorderLayout.EAST);

        CompatibilityUtil.setSmallerFont(searchField);
        searchField.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (StringUtil.isEmptyOrSpaces(searchField.getText())) {
                    close();
                } else {
                    // TODO
                    //requestFocus(myEditor.getContentComponent());
                    addTextToRecent(DataSearchComponent.this.searchField);
                }
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, SystemInfo.isMac ? InputEvent.META_DOWN_MASK : InputEvent.CTRL_DOWN_MASK),
                JComponent.WHEN_FOCUSED);

        final String initialText = findModel.getStringToFind();

        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                setInitialText(initialText);
            }
        });

        CompatibilityUtil.setSmallerFontForChildren(toolbarComponent);
    }

    private void setupSearchFieldListener() {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent documentEvent) {
                searchFieldDocumentChanged();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent documentEvent) {
                searchFieldDocumentChanged();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent documentEvent) {
                searchFieldDocumentChanged();
            }
        });
    }

    private void searchFieldDocumentChanged() {
        String text = searchField.getText();
        findModel.setStringToFind(text);
        if (!StringUtil.isEmpty(text)) {
            updateResults(true);
        } else {
            nothingToSearchFor();
        }
    }

    public boolean isRegexp() {
        return findModel.isRegularExpressions();
    }

    public void setRegexp(boolean val) {
        findModel.setRegularExpressions(val);
    }

    public FindModel getFindModel() {
        return findModel;
    }

    private static void syncFindModels(FindModel to, FindModel from) {
        to.setCaseSensitive(from.isCaseSensitive());
        to.setWholeWordsOnly(from.isWholeWordsOnly());
        to.setRegularExpressions(from.isRegularExpressions());
        to.setInCommentsOnly(from.isInCommentsOnly());
        to.setInStringLiteralsOnly(from.isInStringLiteralsOnly());
    }

    private void updateUIWithFindModel() {

        actionsToolbar.updateActionsImmediately();

        String stringToFind = findModel.getStringToFind();

        if (!StringUtil.equals(stringToFind, searchField.getText())) {
            searchField.setText(stringToFind);
        }

        setTrackingSelection(!findModel.isGlobal());
        CompatibilityUtil.setSmallerFontForChildren(toolbarComponent);
    }

    private static boolean wholeWordsApplicable(String stringToFind) {
        return !stringToFind.startsWith(" ") &&
                !stringToFind.startsWith("\t") &&
                !stringToFind.endsWith(" ") &&
                !stringToFind.endsWith("\t");
    }

    private void setTrackingSelection(boolean b) {
        if (b) {
            if (!myListeningSelection) {
                // TODO
                //myEditor.getSelectionModel().addSelectionListener(this);
            }
        } else {
            if (myListeningSelection) {
                // TODO
                //myEditor.getSelectionModel().removeSelectionListener(this);
            }
        }
        myListeningSelection = b;
    }

    private static JPanel createLeadPane() {
        return new NonOpaquePanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
    }

    public void showHistory(final boolean byClickingToolbarButton, JTextField textField) {
        FeatureUsageTracker.getInstance().triggerFeatureUsed("find.recent.search");
        FindSettings settings = FindSettings.getInstance();
        String[] recent = textField == searchField ? settings.getRecentFindStrings() : settings.getRecentReplaceStrings();
        JBList list = new JBList((Object[]) ArrayUtil.reverseArray(recent));
        CompatibilityUtil.showSearchCompletionPopup(byClickingToolbarButton, toolbarComponent, list, "Recent Searches", textField);
    }

    private void paintBorderOfTextField(Graphics g) {
        if (!(UIUtil.isUnderAquaLookAndFeel() || CompatibilityUtil.isUnderGTKLookAndFeel() || UIUtil.isUnderNimbusLookAndFeel()) &&
                isFocusOwner()) {
            final Rectangle bounds = getBounds();
            g.setColor(FOCUS_CATCHER_COLOR);
            g.drawRect(0, 0, bounds.width - 1, bounds.height - 1);
        }
    }

    private JTextField createTextField(JPanel leadPanel) {
        final JTextField editorTextField = new JTextField("") {
            @Override
            protected void paintBorder(final Graphics g) {
                super.paintBorder(g);
                paintBorderOfTextField(g);
            }
        };
        editorTextField.setColumns(25);
        if (CompatibilityUtil.isUnderGTKLookAndFeel()) {
            editorTextField.setOpaque(false);
        }
        leadPanel.add(editorTextField);
        editorTextField.putClientProperty("AuxEditorComponent", Boolean.TRUE);

        editorTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(final FocusEvent e) {
                editorTextField.revalidate();
                editorTextField.repaint();
            }

            @Override
            public void focusLost(final FocusEvent e) {
                editorTextField.revalidate();
                editorTextField.repaint();
            }
        });
        new CloseOnESCAction(this, editorTextField);
        return editorTextField;
    }


    public void setInitialText(final String initialText) {
        String text = initialText != null ? initialText : "";
        setTextInField(text);
        searchField.selectAll();
    }

    private void requestFocus(Component component) {
        Project project = searchableComponent.getTable().getProject();
        IdeFocusManager.getInstance(project).requestFocus(component, true);
    }

    public void searchBackward() {
        moveCursor(DataSearchDirection.UP);
        addTextToRecent(searchField);
    }

    public void searchForward() {
        moveCursor(DataSearchDirection.DOWN);
        addTextToRecent(searchField);
    }

    private void addTextToRecent(JTextComponent textField) {
        final String text = textField.getText();
        if (text.length() > 0) {
            if (textField == searchField) {
                FindSettings.getInstance().addStringToFind(text);
            } else {
                FindSettings.getInstance().addStringToReplace(text);
            }
        }
    }

    @Override
    public void selectionChanged(SelectionEvent e) {
        updateResults(true);
    }

    private void moveCursor(DataSearchDirection direction) {
        searchResultController.moveCursor(direction);
    }

    private static void setSmallerFontAndOpaque(final JComponent component) {
        CompatibilityUtil.setSmallerFont(component);
        component.setOpaque(false);
    }

    @Override
    public void requestFocus() {
        searchField.setSelectionStart(0);
        searchField.setSelectionEnd(searchField.getText().length());
        requestFocus(searchField);
    }

    public void close() {
        getSearchResult().clear();
        searchableComponent.hideSearchHeader();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
/*
        // TODO
        myLivePreview.cleanUp();
        myLivePreview.dispose();
*/
        setTrackingSelection(false);
        addTextToRecent(searchField);
    }

    private void updateResults(final boolean allowedToChangedEditorSelection) {
        matchInfoLabel.setFont(matchInfoLabel.getFont().deriveFont(Font.PLAIN));
        final String text = searchField.getText();
        if (text.length() == 0) {
            nothingToSearchFor();
            searchableComponent.cancelEditActions();
            searchableComponent.getTable().clearSelection();
            searchableComponent.getTable().revalidate();
            searchableComponent.getTable().repaint();
        } else {

            if (findModel.isRegularExpressions()) {
                try {
                    Pattern.compile(text);
                } catch (Exception e) {
                    setNotFoundBackground();
                    matchInfoLabel.setText("Incorrect regular expression");
                    boldMatchInfo();
                    getSearchResult().clear();
                    return;
                }
            }

            FindManager findManager = getFindManager();
            if (allowedToChangedEditorSelection) {
                findManager.setFindWasPerformed();
                FindModel copy = new FindModel();
                copy.copyFrom(findModel);
                copy.setReplaceState(false);
                findManager.setFindNextModel(copy);
            }

            searchResultController.updateResult(findModel);
        }
    }

    private FindManager getFindManager() {
        Project project = searchableComponent.getTable().getProject();
        return FindManager.getInstance(project);
    }

    private void nothingToSearchFor() {
        updateUIWithEmptyResults();
        getSearchResult().clear();
    }

    private void updateUIWithEmptyResults() {
        setRegularBackground();
        matchInfoLabel.setText("");
    }

    private void boldMatchInfo() {
        matchInfoLabel.setFont(matchInfoLabel.getFont().deriveFont(Font.BOLD));
    }

    private void setRegularBackground() {
        searchField.setBackground(BACKGROUND);
    }

    private void setNotFoundBackground() {
        searchField.setBackground(LightColors.RED);
    }

    public String getTextInField() {
        return searchField.getText();
    }

    public void setTextInField(final String text) {
        searchField.setText(text);
        findModel.setStringToFind(text);
    }

    public boolean hasMatches() {
        return !getSearchResult().isEmpty();
    }

    private DataSearchResult getSearchResult() {
        return searchableComponent.getTable().getModel().getSearchResult();
    }

    @Override
    public Insets getInsets() {
        Insets insets = super.getInsets();
        if (CompatibilityUtil.isUnderGTKLookAndFeel() || UIUtil.isUnderNimbusLookAndFeel()) {
            insets.top += 1;
            insets.bottom += 2;
        }
        return insets;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        final Graphics2D g2d = (Graphics2D) g;

        if (!CompatibilityUtil.isUnderGTKLookAndFeel()) {
            g2d.setPaint(new GradientPaint(0, 0, GRADIENT_C1, 0, getHeight(), GRADIENT_C2));
            g2d.fillRect(1, 1, getWidth(), getHeight() - 1);
            g2d.setPaint(null);
        }

        g.setColor(BORDER_COLOR);
        g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
    }

    @Override
    public void dispose() {
        DataModel dataModel = searchableComponent.getTable().getModel();
        dataModel.removeDataModelListener(this);
        dataModel.getSearchResult().clear();
        searchableComponent = null;
        findModel = null;
    }
}
