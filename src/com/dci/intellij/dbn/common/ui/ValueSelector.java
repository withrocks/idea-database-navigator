package com.dci.intellij.dbn.common.ui;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.InsetsUIResource;
import javax.swing.plaf.UIResource;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.NamingUtil;
import com.intellij.ide.DataManager;
import com.intellij.ide.ui.laf.darcula.DarculaUIUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.ui.GraphicsConfig;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.util.Condition;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.RoundedLineBorder;
import com.intellij.util.IconUtil;
import com.intellij.util.ui.UIUtil;

public abstract class ValueSelector<T extends Presentable> extends JPanel{
    public static Color COMBO_BOX_BACKGROUND = UIUtil.getTextFieldBackground();
    private Set<ValueSelectorListener<T>> listeners = new HashSet<ValueSelectorListener<T>>();
    private T selectedValue;
    private JLabel label;
    private JPanel innerPanel;
    private Icon icon;
    private String text;
    private boolean isComboBox;
    private boolean isEnabled = true;
    private boolean isFocused = false;
    private boolean isShowingPopup = false;

    private Border focusBorder;
    private Border defaultBorder;
    private Border insideBorder;
    private Border insideBorderFocused;

    private List<T> values;


    public ValueSelector(@Nullable String text, @Nullable T preselectedValue, boolean isComboBox) {
        this(null, text, null, preselectedValue, isComboBox);
    }

    public ValueSelector(@Nullable Icon icon, @Nullable String text, @Nullable T preselectedValue, boolean isComboBox) {
        this(icon, text, null, preselectedValue, isComboBox);
    }

    public ValueSelector(@Nullable Icon icon, @Nullable String text, @Nullable List<T> values, @Nullable T preselectedValue, boolean isComboBox) {
        super(new BorderLayout());
        this.values = values;
        text = CommonUtil.nvl(text, "");
        this.icon = icon;
        this.text = text;
        this.isComboBox = isComboBox;

        setBorder(new EmptyBorder(0, 0, 0, 0));

        if (isComboBox) {
            defaultBorder = new ValueSelectorBorder(this);
            focusBorder = defaultBorder;
        } else {
            insideBorder = new EmptyBorder(3, 5, 3, 5);
            insideBorderFocused = new EmptyBorder(2, 4, 2, 4);

            defaultBorder = insideBorder;
            focusBorder = new CompoundBorder(new RoundedLineBorder(new JBColor(Gray._190, Gray._55), 3), insideBorderFocused);
        }

        label = new JLabel(text, cropIcon(icon), SwingConstants.LEFT);
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.addMouseListener(mouseListener);

        innerPanel = new JPanel(new BorderLayout());
        innerPanel.setBorder(defaultBorder);
        innerPanel.add(label, BorderLayout.WEST);
        innerPanel.addMouseListener(mouseListener);
        innerPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        add(innerPanel, BorderLayout.CENTER);

        if (isComboBox) {
            selectedValue = preselectedValue;
            if (selectedValue == null) {
                label.setIcon(cropIcon(icon));
                label.setText(text);
            } else {
                label.setIcon(cropIcon(selectedValue.getIcon()));
                label.setText(selectedValue.getName());
            }

            innerPanel.setBackground(COMBO_BOX_BACKGROUND);
            innerPanel.add(new JLabel(Icons.COMMON_ARROW_DOWN), BorderLayout.EAST);

            innerPanel.setFocusable(true);
            innerPanel.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    isFocused = true;
                    revalidate();
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    isFocused = false;
                    revalidate();
                    repaint();
                }
            });

            innerPanel.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == 38) {//UP
                        selectPrevious();
                        e.consume();
                    } else if (e.getKeyCode() == 40) { // DOWN
                        selectNext();
                        e.consume();
                    } else if (e.getKeyCode() == KeyEvent.VK_ENTER ) {
                        showPopup();
                        e.consume();
                    }

                }
            });

        }

        adjustSize();
    }

    private void adjustSize() {
        int minWidth = 0;
        FontMetrics fontMetrics = label.getFontMetrics(label.getFont());
        int height = fontMetrics.getHeight();
        for (T presentable : getAllPossibleValues()) {
            String name = CommonUtil.nvl(presentable.getName(), "");
            int width = fontMetrics.stringWidth(name);
            if (presentable.getIcon() != null) {
                width = width + 16;
            }
            minWidth = Math.max(minWidth, width);
        }
        int width = fontMetrics.stringWidth(text);
        if (icon != null) {
            width = width + 16;
        }
        minWidth = Math.max(minWidth, width);
        label.setPreferredSize(new Dimension(minWidth + 10, height));
        label.setMinimumSize(new Dimension(minWidth + 10, height));
        innerPanel.setMaximumSize(new Dimension(-1, height + 2));
        innerPanel.setPreferredSize(new Dimension((int) innerPanel.getPreferredSize().getWidth(), height + 2));
    }

    public void addListener(ValueSelectorListener<T> listener) {
        listeners.add(listener);
    }

    public void removeListener(ValueSelectorListener<T> listener) {
        listeners.remove(listener);
    }

    private static Icon cropIcon(Icon icon) {
        return icon == null ? null : IconUtil.cropIcon(icon, 16, 16);
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
        label.setCursor(isEnabled ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR): Cursor.getDefaultCursor());
        innerPanel.setCursor(isEnabled ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());

        innerPanel.setBackground(isComboBox && isEnabled ? COMBO_BOX_BACKGROUND : UIUtil.getPanelBackground());
        innerPanel.setFocusable(isEnabled);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
    }
    private MouseListener mouseListener = new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
            if (!isShowingPopup && !isComboBox) {
                innerPanel.setBorder(focusBorder);
                innerPanel.setBackground(new JBColor(Gray._210, Gray._75));

                revalidate();
                repaint();
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if (!isShowingPopup && !isComboBox) {
                innerPanel.setBorder(defaultBorder);
                //innerPanel.setBorder(new EmptyBorder(21 - icon.getIconHeight(), 6, 21 - icon.getIconHeight(), 6));
                innerPanel.setBackground(isComboBox ? COMBO_BOX_BACKGROUND : UIUtil.getPanelBackground());

                revalidate();
                repaint();
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (isEnabled && !isShowingPopup) {
                innerPanel.requestFocus();
                showPopup();
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            super.mouseMoved(e);
        }
    };

    private void showPopup() {
        isShowingPopup = true;
        innerPanel.setCursor(Cursor.getDefaultCursor());
        label.setCursor(Cursor.getDefaultCursor());
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        for (T value : getValues()) {
            actionGroup.add(new SelectValueAction(value));
        }
        ListPopup popup = JBPopupFactory.getInstance().createActionGroupPopup(
                null,
                actionGroup,
                DataManager.getInstance().getDataContext(this),
                false,
                false,
                false,
                new Runnable() {
                    @Override
                    public void run() {

                        innerPanel.setBorder(defaultBorder);
                        innerPanel.setBackground(isComboBox ? COMBO_BOX_BACKGROUND : UIUtil.getPanelBackground());
                        innerPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                        isShowingPopup = false;
                        innerPanel.requestFocus();
                        revalidate();
                        repaint();
                    }
                }, 10, new Condition<AnAction>() {
                    @Override
                    public boolean value(AnAction anAction) {
                        if (anAction instanceof ValueSelector.SelectValueAction) {
                            SelectValueAction action = (SelectValueAction) anAction;
                            return action.value.equals(selectedValue);
                        }
                        return false;
                    }
                });

        GUIUtil.showUnderneathOf(popup, this, 3, 200);
    }

    public void clearValues() {
        selectValue(null);
        values.clear();
    }


    public class SelectValueAction extends DumbAwareAction {
        private T value;

        public SelectValueAction(T value) {
            super(NamingUtil.enhanceUnderscoresForDisplay(value.getName()), null, value.getIcon());
            this.value = value;
        }

        public void actionPerformed(AnActionEvent e) {
            selectValue(value);
            innerPanel.requestFocus();
        }

        @Override
        public void update(AnActionEvent e) {
            e.getPresentation().setVisible(isVisible(value));
        }
    }

    public boolean isVisible(T value) {
        return true;
    }

    @Nullable
    public T getSelectedValue() {
        return selectedValue;
    }

    public void setSelectedValue(@Nullable T value) {
        selectValue(value);
    }

    public final List<T> getValues() {
        if (values == null) {
            values = loadValues();
        }
        return values;
    }

    protected List<T> loadValues() {
        return new ArrayList<T>();
    }

    protected List<T> getAllPossibleValues() {
        return getValues();
    }

    public void setValues(T ... values) {
        setValues(Arrays.asList(values));
    }

    public void setValues(List<T> values) {
        this.values = values;
        adjustSize();
    }

    public void resetValues() {
        this.values = null;
    }

    public final void valueSelected(T value) {
        for (ValueSelectorListener<T> listener : listeners) {
            listener.valueSelected(value);
        }

    }

    private void selectValue(T value) {
        value = values.contains(value) ? value : values.isEmpty() ? null : values.get(0);
        if (isComboBox) {
            selectedValue = value;
            if (selectedValue == null) {
                label.setIcon(cropIcon(icon));
                label.setText(text);
            } else {
                label.setIcon(cropIcon(selectedValue.getIcon()));
                label.setText(selectedValue.getName());
            }
        }

        valueSelected(value);
    }

    public void selectNext() {
        if (isComboBox && selectedValue != null) {
            List<T> values = getValues();
            int index = values.indexOf(selectedValue);
            if (index < values.size() - 1) {
                T nextValue = values.get(index + 1);
                selectValue(nextValue);
            }
        }
    }

    public void selectPrevious() {
        if (isComboBox && selectedValue != null) {
            List<T> values = getValues();
            int index = values.indexOf(selectedValue);
            if (index > 0) {
                T previousValue = values.get(index - 1);
                selectValue(previousValue);
            }
        }
    }

    public class ValueSelectorBorder implements Border, UIResource {
        ValueSelector<T> valueSelector;

        public ValueSelectorBorder(ValueSelector<T> valueSelector) {
            this.valueSelector = valueSelector;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new InsetsUIResource(4, 6, 4, 6);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }

        @Override
        public void paintBorder(Component c, Graphics g2, int x, int y, int width, int height) {
            Graphics2D g = ((Graphics2D)g2);
            final GraphicsConfig config = new GraphicsConfig(g);
            g.translate(x, y);

            if (UIUtil.isUnderDarcula() || UIUtil.isUnderIntelliJLaF()) {
                if (isFocused || isShowingPopup) {
                    DarculaUIUtil.paintFocusRing(g, 2, 2, width - 4, height - 4);
                } else {
                    boolean editable = valueSelector.isEnabled;
                    g.setColor(getBorderColor(c.isEnabled() && editable));
                    g.drawRect(1, 1, width-2, height-2);
                    g.setColor(UIUtil.getPanelBackground());
                    g.drawRect(0, 0, width, height);

                }
            } else {
                Border textFieldBorder = UIUtil.getTextFieldBorder();
                if (textFieldBorder instanceof LineBorder) {
                    LineBorder lineBorder = (LineBorder) textFieldBorder;
                    g.setColor(lineBorder.getLineColor());
                } else {
                    g.setColor(UIUtil.getBorderColor());
                }
                g.drawRect(1, 1, width - 3, height - 3);
                g.setColor(UIUtil.getPanelBackground());
                g.drawRect(0, 0, width-1, height-1);
            }
            g.translate(-x, -y);
            config.restore();

        }

        private Color getBorderColor(boolean enabled) {
            if (UIUtil.isUnderDarcula()) {
                return enabled ? Gray._100 : Gray._83;
            }
            return Gray._150;
        }
    }
}
