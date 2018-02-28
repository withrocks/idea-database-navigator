package com.dci.intellij.dbn.data.editor.ui;

import javax.swing.AbstractListModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.list.FiltrableList;
import com.dci.intellij.dbn.common.thread.ConditionalLaterInvocator;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupChooserBuilder;
import com.intellij.ui.JBColor;

@Deprecated
public class ValuesListPopupProviderForm extends TextFieldPopupProviderForm {
    public static final Color BACKGROUND_COLOR = new JBColor(
            new Color(0xEBF4FE),
            new Color(0x3c3f41));
    private ListPopupValuesProvider valuesProvider;
    private ListModel listModel;
    private JList list;
    private JPanel mainPanel;
    private boolean useDynamicFiltering;

    public ValuesListPopupProviderForm(TextFieldWithPopup textField, @NotNull ListPopupValuesProvider valuesProvider, boolean buttonVisible, boolean dynamicFiltering) {
        super(textField, false, buttonVisible);
        this.valuesProvider = valuesProvider;
        this.useDynamicFiltering = dynamicFiltering;
        list.setBackground(BACKGROUND_COLOR);
    }

    public JComponent getComponent() {
        return mainPanel;
    }

    @Override
    public void preparePopup() {
        // this may take long time so has to be executed in background
        valuesProvider.getValues();
    }

    public JBPopup createPopup() {
        List<String> possibleValues = valuesProvider.getValues();
        if (possibleValues.size() > 0) {
            Collections.sort(possibleValues);
            listModel = useDynamicFiltering ? new ListModel(new DynamicFilter(), possibleValues) : new ListModel(possibleValues);
            list.setModel(listModel);
            list.addMouseListener(mouseListener);
            PopupChooserBuilder popupBuilder = JBPopupFactory.getInstance().createListPopupBuilder(list);
            popupBuilder.setRequestFocus(false);

            String text = getTextField().getText();
            if (StringUtil.isEmptyOrSpaces(text)) {
                list.clearSelection();
                list.scrollRectToVisible(list.getCellBounds(0, 0));
            } else {
                list.setSelectedValue(text, true);
            }
            return popupBuilder.createPopup();
        } else {
            return null;
        }
    }

    public void handleKeyPressedEvent(KeyEvent e) {
        assert isShowingPopup();
        int keyCode = e.getKeyCode();
        if (keyCode == 38) { // UP
            int selectedIndex = list.getSelectedIndex();
            if (selectedIndex > 0) {
                scrollToIndex(selectedIndex - 1);
            }
            e.consume();
        } else if (keyCode == 40) { // DOWN
            int selectedIndex = list.getSelectedIndex();
            if (selectedIndex < list.getModel().getSize() - 1) {
                scrollToIndex(selectedIndex + 1);
            }
            e.consume();
        } else if (keyCode == 33) { // PAGE UP
            int selectedIndex = list.getSelectedIndex();
            if (selectedIndex > 0) {
                int scrollCount = list.getVisibleRowCount() == 0 ? list.getModel().getSize() : list.getVisibleRowCount();
                int newIndex = Math.max(selectedIndex - scrollCount, 0);
                scrollToIndex(newIndex);
            }
            e.consume();
        } else if (keyCode == 34) { // PAGE DOWN
            int selectedIndex = list.getSelectedIndex();
            if (selectedIndex < list.getModel().getSize() - 1) {
                int scrollCount = list.getVisibleRowCount() == 0 ? list.getModel().getSize() : list.getVisibleRowCount();
                int newIndex = Math.min(selectedIndex + scrollCount, list.getModel().getSize() - 1);
                scrollToIndex(newIndex);
            }
            e.consume();
        } else if (keyCode == 37 || keyCode == 39 || keyCode == 35 || keyCode == 36) { // RIGHT or LEFT or END or HOME
            updateList();
        } else if (keyCode == 10 || keyCode == 9) { // ENTER or TAB
            String selectedValue = (String) list.getSelectedValue();
            if (selectedValue != null) {
                hidePopup();
                JTextField textField = getTextField();
                textField.setText(selectedValue);
                textField.requestFocus();
            }
            e.consume();
        } else if (keyCode == 27) { //ESC
            hidePopup();
            e.consume();
        } else {
            updateList();
        }
    }

    public void handleKeyReleasedEvent(KeyEvent e) {
        if (e.getKeyCode() == 36 || e.getKeyCode() == 35) { // HOME or END
            updateList();
        }
    }

    public void handleFocusLostEvent(FocusEvent e) {
        if (getPopup().getContent().getParent().getParent().getParent().getParent() != e.getOppositeComponent()) {
            hidePopup();
        }
    }

    public String getKeyShortcutName() {
        return IdeActions.ACTION_CODE_COMPLETION;
    }

    public String getDescription() {
        return "Possible Values List";
    }

    @Nullable
    @Override
    public Icon getButtonIcon() {
        return Icons.DATA_EDITOR_LIST;
    }

    @Override
    public TextFieldPopupType getPopupType() {
        return TextFieldPopupType.VALUE_LIST;
    }

    private void scrollToIndex(int index) {
        list.setSelectedIndex(index);
        Rectangle rectangle = list.getCellBounds(index, index);
        if (rectangle != null) {
            list.scrollRectToVisible(rectangle);
        }
    }

    private void updateList() {
        new ConditionalLaterInvocator() {
            public void execute() {
                if (listModel.isFiltrable()) {
                    int index = list.getSelectedIndex();
                    listModel.notifyContentChanged();
                    if (index > listModel.getSize() - 1) {
                        scrollToIndex(listModel.getSize() - 1);
                    }
                } else {
                    String text = getTextField().getText();
                    for (int i=0; i<listModel.getElements().size(); i++ ) {
                        String element = listModel.getElements().get(i);
                        if (element.startsWith(text)) {
                            scrollToIndex(i);
                            break;
                        }
                    }

                }
            }
        }.start();
    }

    private class DynamicFilter extends Filter<String> {
        public boolean accepts(String string) {
            if (getEditorComponent().isSelected()) return true;

            JTextField textField = getTextField();
            int caretOffset = textField.getCaretPosition();
            if (caretOffset == 0) {
                return true;
            } else {
                if (caretOffset > string.length()) {
                    return false;
                } else {
                    String textFieldValue = textField.getText().substring(0, caretOffset).toLowerCase();
                    String listValue = string.substring(0, caretOffset).toLowerCase();
                    return textFieldValue.equals(listValue);
                }
            }
        }
    }


    /******************************************************
     *                  MouseListener                     *
     ******************************************************/
    private MouseListener mouseListener = new MouseAdapter() {

        @Override
        public void mousePressed(MouseEvent event) {
            if (!event.isConsumed() && event.getButton() == MouseEvent.BUTTON1) {
                String selectedValue = (String) list.getSelectedValue();
                if (selectedValue != null) {
                    hidePopup();
                    JTextField textField = getTextField();
                    textField.setText(selectedValue);
                    textField.requestFocus();
                    event.consume();
                }

            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            super.mouseReleased(e);
        }

        @Override
        public void mouseClicked(MouseEvent event) {
        }

    };

    /******************************************************
     *                    ListModel                       *
     ******************************************************/
    class ListModel extends AbstractListModel {
        List<String> elements;

        ListModel(Filter<String> filter, Collection<String> elements) {
            this.elements = new FiltrableList<String>(filter);
            this.elements.addAll(elements);
        }

        public List<String> getElements() {
            return elements;
        }

        ListModel(Collection<String> elements) {
            this.elements = new ArrayList<String>(elements);
        }

        public boolean isFiltrable() {
            return elements instanceof FiltrableList;
        }

        public int getSize() {
            return elements.size();
        }

        public Object getElementAt(int index) {
            return index == -1 ? null : elements.get(index);
        }

        void setVariants(Collection<String> collection) {
            elements.clear();
            elements.addAll(collection);
            notifyContentChanged();
        }

        void notifyContentChanged() {
            fireContentsChanged(this, 0, elements.size());
        }

        void clear() {
            elements.clear();
        }
    }

    public void dispose() {
        super.dispose();
    }

}
